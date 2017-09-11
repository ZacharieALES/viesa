//Copyright (C) 2012 Zacharie ALES and Rick MORITZ
//
//This file is part of Viesa.
//
//Viesa is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Viesa is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with Viesa.  If not, see <http://www.gnu.org/licenses/>.
package model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.SwingWorker;

import clustering.AbstractClusteringMethod;
import clustering.Cluster;
import clustering.ClusterSet;
import clustering.ClusteringSolution;
import clustering.HardClusteringSolution;
import exception.AbstractException;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.ExtractionAlignments;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import main.Main;
import model.AAColumnFormat.ColumnType;

/**
 * Contains a corpus of annotated arrays.
 * 
 * @author zach
 * 
 */
public class Corpus implements Observable, Serializable {

	// TODO Enable to select if we want to filter or not the patterns and if we
	// want to compute the similarity or not (or compute the similarity only if
	// we cluster the patterns
	private static final long serialVersionUID = -3657405496238415664L;

	/**
	 * Private constructor since Corpus is a singleton
	 */
	private Corpus() {

		// The empty annotation is always the first element in <annotations>
		annotations.add("empty annotation");
		arrays = new ArrayList<AnnotatedArray>();
	};

	/**
	 * Class used as a holder to implement the singleton design pattern (also
	 * deal with synchronization)
	 * 
	 * @author zach
	 *
	 */
	private static class CorpusHolder {

		/** Unique and non pre-initialized instance */
		private final static Corpus c = new Corpus();
	}

	public static Corpus getCorpus() {
		return CorpusHolder.c;
	}

	private List<AnnotatedArray> arrays;

	private List<Pattern> patterns = new ArrayList<Pattern>();

	/**
	 * Parameter true if the extraction process is completly performed If it is
	 * equal to false, the clustering step will not be performed
	 */
	private boolean extractionCompleted = false;

	public AAColumnFormat aacf;

	/*
	 * Solutions of clustering algorithm (can be hard or hierarchical)
	 */
	private List<ClusteringSolution> al_clusteringSolution = new ArrayList<ClusteringSolution>();

	/* Annotations which occurs in the corpus */
	private List<String> annotations = new ArrayList<String>();

	private transient List<CorpusObserver> listObserver = new ArrayList<CorpusObserver>();

	private double[][] patternsSimilarity;

	/**
	 * Enable to compute for each pattern the patterns which are close to him
	 */
	public boolean computeClosePatterns = false;

	public enum Clustering_algorithm {
		SINGLE_LINK, ROCK
	}

	private void add(AnnotatedArray aa) {

		if (arrays == null) {
			arrays = new ArrayList<AnnotatedArray>();
		}

		/* If it's not the first element of the Corpus */
		if (arrays.size() != 0) {

			/* If the aa is compatible with the column format of the corpus */
			if (!aa.isCompatibleWith(aacf))
				System.err.println("The column number (" + aa.getNumberOfAnnotationColumns()
						+ ") of the AnnotatedArray " + aa.getFileName()
						+ " is not compatible with the first of the corpus (which is : " + arrays.get(0).getFileName()
						+ "(" + arrays.get(0).getNumberOfAnnotationColumns() + "))");
			else {
				arrays.add(aa);
				notifyObserverAddAA(aa);
			}
		}

		else {
			arrays.add(aa);
			notifyObserverAddAA(aa);
		}

	}

	/**
	 * Return the index associated to a given annotation
	 * 
	 * @param s
	 * @return
	 */

	public short getAnnotationIndex(String s) {
		int i = annotations.indexOf(s);
		int result;

		if (i == -1) {
			result = annotations.size();
			annotations.add(s);
		} else
			result = i;
		return (short) result;
	}

	/**
	 * Get the annotation associated to an index
	 * 
	 * @param i
	 * @return
	 */
	public String getAnnotation(int i) {
		return annotations.get(i);
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	/**
	 * Set the column format of the array in the corpus
	 * 
	 * @param commentColumns
	 *            Index of the comment columns in the input files (null or empty
	 *            if their is no comment column)
	 * @param annotationColumns
	 *            Index of the annotation columns in the input files (null or
	 *            empty if their is no annotation column)
	 * @param numericalColumns
	 *            Index of the numerical annotation columns in the input files
	 *            null or empty if their is no numerical annotation column)
	 * @param annotation_similarities
	 *            Similarity between annotations in annotation columns
	 * @throws InvalidArgumentsToCreateAnAAColumnFormat
	 */
	public void setColumnFormat(ArrayList<Integer> commentColumns, ArrayList<Integer> annotationColumns,
			ArrayList<Integer> numericalColumns) throws InvalidArgumentsToCreateAnAAColumnFormat {

		ArrayList<PositionedColumn> al_pc = new ArrayList<PositionedColumn>();

		if (commentColumns != null)
			for (Integer i : commentColumns)
				al_pc.add(new PositionedColumn(new CommentColumn(), i));

		if (annotationColumns != null)
			for (Integer i : annotationColumns)
				al_pc.add(new PositionedColumn(new AnnotationColumn(), i));

		aacf = new AAColumnFormat(al_pc);

	}

	/**
	 * Create AnnotatedArrays from one or several csv files (1 csv files <-> 1
	 * annotated array) and add them in the corpus.
	 * 
	 * @param sPath
	 *            Path of one csv file or path of a folder which contains csv
	 *            files
	 * @throws UndefinedAnnotationInSMException
	 */
	public void add(String sPath, boolean h_header) throws UndefinedColumnFormatException {

		if (aacf != null) {

			File directory = new File(sPath);

			/* If the file exist */
			if (!directory.exists())
				System.err.println("File/folder " + directory + " doesn't exist");
			else {

				File[] subFiles = null;

				if (!directory.isDirectory()) {
					subFiles = new File[1];
					subFiles[0] = new File(sPath);
				} else
					subFiles = directory.listFiles();

				/* Regexp to test that the end of the file end by ".csv" */
				java.util.regex.Pattern csvRegexp = java.util.regex.Pattern.compile(".*csv");

				/*
				 * For each file at <sPath>
				 */
				for (int j = 0; j < subFiles.length; j++) {

					Matcher fileName = csvRegexp.matcher(subFiles[j].getName());
					// System.out.println("file name : " +
					// subFiles[j].getName());

					/* If the file is a csv file */
					if (subFiles[j].isFile() && fileName.matches()) {

						try {
							AnnotatedArray aa = new AnnotatedArray(subFiles[j].getPath(), h_header, aacf);
							this.add(aa);
						} catch (AbstractException e) {
							notifyObserverAbstractException(e);
						}
					}
				}
			}
		}

		/* If aacf == null */
		else {
			throw new UndefinedColumnFormatException();
		}

	}

	public void remove(AnnotatedArray aa) {
		int id = arrays.indexOf(aa);
		arrays.remove(id);
		notifyObserverRemoveAA(id);

	}

	public void remove(int id) {
		arrays.remove(id);

		notifyObserverRemoveAA(id);
	}

	public void initPatternSimilarity() {
		patternsSimilarity = new double[patterns.size()][patterns.size()];
		for (int i = 0; i < patterns.size(); i++) {
			patternsSimilarity[i][i] = 0;
		}
	}

	public void calculateSimilarity(Pattern p1, Pattern p2) {
		double similarity = p1.similarity(p2);
		// int similarity = -1;

		patternsSimilarity[p1.getIndex()][p2.getIndex()] = similarity;
		patternsSimilarity[p2.getIndex()][p1.getIndex()] = similarity;

	}

	public void displaySimilarities() {

		for (int i = 1; i < patterns.size(); ++i) {
			for (int j = 0; j < i; ++j) {
				double k = (-patternsSimilarity[patterns.get(i).getIndex()][patterns.get(j).getIndex()]);
				System.out.print(k + " ");
			}
			System.out.println();
		}

	}

	public void clearPatternsAndClusteringSolutions() {
		patterns.clear();
		al_clusteringSolution.clear();

		notifyObserverClearClusters();
		notifyObserverPatterns();
	}

	public void aaAlignment(AnnotatedArray aa1, AnnotatedArray aa2, ExtractionAlignments extractedAlignments) {

		/* Extract the alignments between aa1 and aa2 */
		extractedAlignments
				.addAlignments(SABRE.getInstance().align(aa1, aa2, extractedAlignments.getMinimumScore(), false));
	}

	public ClusteringSolution getClusteringSolution(int i) {
		return al_clusteringSolution.get(i);
	}

	public int getClusteringSolutionSize() {
		return al_clusteringSolution.size();
	}

	public void add(ClusteringSolution cs) {
		al_clusteringSolution.add(cs);
		notifyObserverClusters(cs);
	}

	public Double safeSimilarity(Pattern p1, Pattern p2) {

		if (p1 == null || p2 == null)
			return null;

		int id1 = patterns.indexOf(p1);
		int id2 = patterns.indexOf(p2);

		if (id1 >= 0 && id2 >= 0 && patternsSimilarity != null && patternsSimilarity.length > id1
				&& patternsSimilarity[id1] != null && patternsSimilarity[id1].length > id2)
			return patternsSimilarity[patterns.indexOf(p1)][patterns.indexOf(p2)];
		else
			return null;

	}

	public double similarity(Pattern p1, Pattern p2) {

		return patternsSimilarity[patterns.indexOf(p1)][patterns.indexOf(p2)];
	}

	public double similarity(int p1, int p2) {

		return patternsSimilarity[p1][p2];
	}

	public void addObserver(Observer obs) {

		if (listObserver == null) {
			listObserver = new ArrayList<CorpusObserver>();
		}

		CorpusObserver cobs = (CorpusObserver) obs;
		listObserver.add(cobs);

		if (patterns != null && patterns.size() > 0)
			cobs.updatePatterns();

		if (al_clusteringSolution != null && al_clusteringSolution.size() > 0)
			for (ClusteringSolution cs : al_clusteringSolution)
				cobs.updateClusters(cs);

		cobs.updateSABREParameters();

		if (AnnotationColumn.pst != null)
			cobs.updateScoreSimilarities(AnnotationColumn.pst);

		// updateMaxSimilarity(NumericalColumn.maxSim);

		if (arrays != null)
			for (AnnotatedArray aa : arrays)
				cobs.updateAddAA(aa);

	}

	public void removeAllObserver() {
		if (listObserver != null)
			listObserver.clear();
	}

	public void removeObserver() {
		listObserver = new ArrayList<CorpusObserver>();
	}

	private void notifyObserverAddAA(AnnotatedArray aa) {
		for (CorpusObserver obs : listObserver) {
			obs.updateAddAA(aa);
		}
	}

	private void notifyObserverRemoveAA(int id) {
		for (CorpusObserver obs : listObserver) {
			obs.updateRemoveAA(id);
		}
	}

	private void notifyObserverExtractionEndOfProcess() {
		for (CorpusObserver obs : listObserver) {
			obs.endOfExtractionProcess();
		}
	}

	private void notifyObserverSwingWorker(SwingWorker<?, ?> sw) {
		for (CorpusObserver obs : listObserver) {
			obs.updateSwingWorker(sw);
		}
	}

	public void notifyObserverEndOfClusteringProcess() {
		for (CorpusObserver obs : listObserver)
			obs.endOfClusteringProcess();
	}

	private void notifyObserverDesiredNumberOfAlignments(int v) {
		for (CorpusObserver obs : listObserver)
			obs.updateSABREParameters();
	}

	private void notifyObserverPatterns() {
		for (CorpusObserver obs : listObserver)
			obs.updatePatterns();
	}

	private void notifyObserverRemoveClusteringSolution(int i) {
		for (CorpusObserver obs : listObserver)
			obs.removeClusteringSolution(i);
	}

	private void notifyObserverClearClusters() {
		for (CorpusObserver obs : listObserver)
			obs.clearClusters();
	}

	private void notifyObserverClusters(ClusteringSolution cs) {
		for (CorpusObserver obs : listObserver)
			obs.updateClusters(cs);
	}

	private void notifyObserverAbstractException(AbstractException e) {
		for (CorpusObserver obs : listObserver)
			obs.abstractException(e);
	}

	private void notifyObserverScoreSimilarities(PositiveScoreTable p) {
		for (CorpusObserver obs : listObserver)
			obs.updateScoreSimilarities(p);
	}

	private void notifyObserverMaxSimilarity(Double d) {
		for (CorpusObserver obs : listObserver) {
			obs.updateMaxSimilarity(d);
		}
	}

	public AnnotatedArray getAA(int i) {
		return arrays.get(i);
	}

	public int getAASize() {
		return arrays.size();
	}

	public void setDesiredNumberOfAlignments(int value) {
		SABRE.getInstance().getParam().desired_number_of_alignments = value;
		notifyObserverDesiredNumberOfAlignments(value);
	}

	public void removeClusteringSolution(int i) {
		al_clusteringSolution.remove(i);
		notifyObserverRemoveClusteringSolution(i);
	}

	public double compareToP(Pattern p, Pattern p1, Pattern p2) {
		return similarity(p, p1) - similarity(p, p2);
	}

	public double[][] getPatternsSimilarity() {
		return patternsSimilarity;
	}

	public void setAnnotationSimilarities(PositiveScoreTable p) {
		AnnotationColumn.pst = p;
		notifyObserverScoreSimilarities(p);
	}

	public void setMaxDistance(Double max) {
		NumericalColumn.maxSim = max;
		notifyObserverMaxSimilarity(max);
	}

	public PositiveScoreTable getAnnotationSimilarities() {
		return AnnotationColumn.pst;
	}

	public int getAnnotationColumnNb() {
		return arrays.size() == 0 ? 0 : arrays.get(0).getNumberOfAnnotationColumns();
	}

	public Pattern getPattern(int i) {
		return patterns.get(i);
	}

	public int getPatternSize() {
		return patterns.size();
	}

	public void extractPatterns() {
		executeSwingWorker(new CorpusExtraction());
	}

	public void clusterPatterns(ArrayList<AbstractClusteringMethod> clusteringToPerform) {

		if (extractionCompleted) {

			AbstractClusteringMethod.remainingClusteringMethodToProcess = clusteringToPerform.size();

			for (AbstractClusteringMethod method : clusteringToPerform)
				executeSwingWorker(method);
		}

	}

	public void executeSwingWorker(SwingWorker<Void, Void> sw) {
		notifyObserverSwingWorker(sw);
		sw.execute();
	}

	public List<AnnotatedArray> getAA() {
		return Collections.unmodifiableList(arrays);
	}

	public List<ClusteringSolution> getClusteringSolutions() {
		return Collections.unmodifiableList(al_clusteringSolution);
	}

	public List<Pattern> getPatterns() {
		return Collections.unmodifiableList(patterns);
	}

	/**
	 * Class used to extract patterns. It is a swing worker in order not to
	 * freeze the GUI while performing the compuation. The class also create a
	 * pattern partition in which a cluster contain two patterns which have been
	 * extracted together. The similarity between all pairs of patterns is
	 * computed.
	 * 
	 * @author zach
	 *
	 */
	public class CorpusExtraction extends SwingWorker<Void, Void> {

		/*
		 * This value tracks the progress of the processing, for feedback to
		 * connected Views
		 */
		private double progress;

		@Override
		protected Void doInBackground() throws Exception {

			// System.out.println("Corpus Extraction : do in background");
			firePropertyChange("description", "", "Extract patterns alignements");

			if (al_clusteringSolution != null)
				while (al_clusteringSolution.size() > 0)
					removeClusteringSolution(0);

			extractionCompleted = false;
			ExtractionAlignments extractedAlignments = new ExtractionAlignments();

			try {

				double invMaxProgress = 1.0 / (((getAASize() - 1) * (getAASize() - 2)) >> 1);
				double step = 100.0 * invMaxProgress;

				this.resetProgress();

				Corpus.getCorpus().clearPatternsAndClusteringSolutions();

				int i = 0;

				/* For each couple of AnnotatedArray in the corpus */
				while (i < getAASize() && !isCancelled()) {
					for (int j = 0; j < i; j++) {
						if (!isCancelled()) {

							/* Extract Alignments */
							aaAlignment(getAA(i), getAA(j), extractedAlignments);

							progress(step);

						} else {
							this.done();
						}
					}

					i++;

				}

				patterns = extractedAlignments.getPatterns();

				/* If patterns have been found */
				if (getPatternSize() > 0) {

					/*
					 * Remove the duplicated patterns and create a clustering
					 * solution which corresponds to the alignments extracted (1
					 * cluster <-> 2 patterns)
					 */
					HardClusteringSolution cs = removeDuplicate();
					al_clusteringSolution.add(cs);

					initPatternSimilarity();
					notifyObserverClusters(cs);

					if (!isCancelled()) {

						firePropertyChange("description", "", "Compute pattern similarities");
						// System.out.println("Corpus : after duplicate : " +
						// patterns.size());

						/* Begin *** Compute corpus.getPatterns() similarity */

						invMaxProgress = 1.0 / (((patterns.size() - 1) * (patterns.size() - 2)) >> 1);
						step = 100.0 * invMaxProgress;
						this.resetProgress();

						i = 0;
						/* For each pattern */
						while (i < patterns.size() && !isCancelled()) {

							Pattern p1 = patterns.get(i);

							/* For each couple of patterns */
							for (int j = 0; j < i; j++) {
								// System.out.println("\tCorpus j = " + j);
								if (!isCancelled()) {
									Pattern p2 = patterns.get(j);

									// Compute all the similarities
									calculateSimilarity(p1, p2);

									if (computeClosePatterns) {
										int v1, v2, v3;
										v1 = (int) Math.round(patternsSimilarity[p1.getIndex()][p2.getIndex()]);
										v2 = (int) Math.round(p1.maximalScoreOfPattern());
										v3 = (int) Math.round(p2.maximalScoreOfPattern());

										double d1 = v1 / ((double) v2), d2 = v1 / ((double) v3);
										double t = 0.6;

										if (d2 >= t) {
											p2.closePatterns++;
										}

										if (d1 >= t) {
											p1.closePatterns++;
										}

										// System.out.print( (d2 >= t) + "/" +
										// (d1 >= t) + "\t");
									}
									this.progress(step);
								}
							}

							i++;

						}

					}
				}

				System.out.println("Corpus : " + Corpus.getCorpus().getPatternSize() + " extracted patterns");

				if (isCancelled()) {
					this.done();
					System.out.println("Corpus: isCancelled, this.done()");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!isCancelled()) {
				extractionCompleted = true;
			}

			return null;
		}

		public void resetProgress() {
			this.progress = 0.0;
			this.setProgress(0);
		}

		/*
		 * Increase the progress indicator bar by (double) step percent.
		 * 
		 */
		private void progress(double step) {
			this.progress += step;
			this.setProgress((int) Math.min(progress, 99));
		}

		@Override
		public void done() {
			notifyObserverExtractionEndOfProcess();
		}

	} // End : class CorpusExtraction

	/**
	 * 1 - Remove all the patterns which are included in another pattern (if 2
	 * patterns are identical, the one with the lower index in <patterns> is
	 * said to be included in the other one) 2 - Return a clustering solution in
	 * which each cluster correspond to one alignment (i.e.: it contains exactly
	 * 2 patterns)
	 * 
	 * Let (p1,p'1) and (p2, p'2) be two alignments. Several cases are possible:
	 * 
	 * - if p1 and p2 are not included in any pattern : create a cluster with p1
	 * and p2 - if p1 and p2 are both included in another pattern : don't create
	 * a cluster for this alignment - if p1 is included in another pattern p3
	 * but p2 is not included in any other pattern : create a cluster with p2
	 * and p3 - if p2 is included in another pattern p3 but p1 is not included
	 * in any other pattern : create a cluster with p1 and p3
	 * 
	 * If p1 is strictly included in another pattern p3, p'1.alignedPattern is
	 * set to null. If p1 is identical to another pattern p3, p'1.alignedPattern
	 * is set to p3.
	 */
	public HardClusteringSolution removeDuplicate() {

		/*
		 * Array which contains for each pattern the id of the pattern with
		 * which it has been extracted If a pattern is filtered the value in
		 * clusters for the corresponding index is -1
		 */
		int[] includedIn = new int[patterns.size()];

		for (int i = 0; i < patterns.size(); ++i)
			includedIn[i] = -1;

		/* Set the index of each pattern */
		for (int i = 0; i < patterns.size() / 2; ++i) {

			patterns.get(2 * i).setIndex(2 * i);
			patterns.get(2 * i + 1).setIndex(2 * i + 1);
		}

		/* For all couple of patterns */
		for (int i = 0; i < patterns.size(); ++i) {

			/* If pattern i is not yet removed */
			if (includedIn[i] == -1) {

				Pattern p1 = patterns.get(i);

				/* Compare p1 with all the following patterns */
				for (int j = i + 1; j < patterns.size(); ++j) {

					/* If patterns i and j are not removed */
					if (includedIn[i] == -1 && includedIn[j] == -1) {

						Pattern p2 = patterns.get(j);

						boolean p2_in_p1 = p2.isIncludedIn(p1);
						boolean p1_in_p2 = p1.isIncludedIn(p2);

						/* If p1 is included in p2 */
						if (p1_in_p2) {

							/* If p1 and p2 are identical */
							if (p2_in_p1)
								includedIn[j] = i;
							else
								includedIn[i] = j;

						} else if (p2_in_p1)
							includedIn[j] = i;

					} // End: if (includedIn[i] == -1 && includedIn[j] == -1) {
				} // End: for(int j = i+1 ; j < patterns.size() ; ++j) {
			} // End: if (includedIn[i] == -1) {
		} // for (int i = 0; i < patterns.size(); ++i) {

		List<Cluster> clusters = new ArrayList<Cluster>();

		/* For each extracted alignment (one alignment = 2 patterns) */
		for (int i = 0; i < patterns.size() / 2; ++i) {

			int id1 = 2 * i;
			int id2 = 2 * i + 1;

			Pattern p1 = patterns.get(id1);
			Pattern p2 = patterns.get(id2);

			/* If the first pattern is not included in another one */
			if (includedIn[id1] == -1)

				/* If the second pattern is not included in another one */
				if (includedIn[id2] == -1) {

				Cluster c = new Cluster();
				c.add(p1);
				c.add(p2);
				clusters.add(c);

				}

				/* If the second pattern is included in another one */
				else {

				/*
				 * Create a cluster with the first pattern and the parent of the
				 * second pattern
				 */
				Cluster c = new Cluster();
				c.add(p1);

				int parentId = getParentPattern(includedIn, id2);

				c.add(patterns.get(parentId));
				clusters.add(c);
				}

			/* If the first pattern is included in another one */
			else

			/*
			 * If the second pattern is not included in another one
			 */
			if (includedIn[id2] == -1) {

				/*
				 * Create a cluster with the second pattern and the parent of
				 * the first pattern
				 */
				Cluster c = new Cluster();
				c.add(p2);

				int parentId = getParentPattern(includedIn, id1);

				c.add(patterns.get(parentId));
				clusters.add(c);
			}

			/*
			 * If both patterns from the alignment are included in another
			 * pattern
			 */
			else {

				/*
				 * Create a cluster with the parent of both patterns
				 */
				Cluster c = new Cluster();
				int parentId = getParentPattern(includedIn, id2);
				c.add(patterns.get(parentId));

				parentId = getParentPattern(includedIn, id1);
				c.add(patterns.get(parentId));
				clusters.add(c);
			}

			// TODO Conserver cet alignement tout de même ?

		}

		HardClusteringSolution hcs = new HardClusteringSolution(new ClusterSet(clusters, true));
		hcs.setMethodName("Extracted alignments");

		/* Remove the patterns which are included in another one */
		for (int i = patterns.size() - 1; i >= 0; i--)
			if (includedIn[i] != -1)
				patterns.remove(i);

		/* Update the id */
		for (int i = 0; i < patterns.size(); ++i) {
			patterns.get(i).setIndex(i);
		}

		return hcs;
	}

	/**
	 * Given <includedIn>, return the pattern which includes all the patterns
	 * which includes the pattern <id>
	 * 
	 * @param includedIn
	 *            Array whose size is the number of patterns and whose value for
	 *            a given pattern id correspond to the id of a pattern which
	 *            includes it. If no pattern includes the pattern the value is
	 *            -1.
	 * @param id
	 *            Id of the pattern from which you seek the parent
	 * @return
	 */
	private int getParentPattern(int[] includedIn, int id) {
		int previousParentId = id;
		int currentParentId = includedIn[previousParentId];

		while (currentParentId != -1) {
			previousParentId = currentParentId;
			currentParentId = includedIn[previousParentId];
		}

		return previousParentId;

	}

	public int getIndexOfPattern(Pattern p) {
		return patterns.indexOf(p);
	}

	/**
	 * Test if the program is ready to process
	 * 
	 * @return True if the corpus contain AnnotatedArrays, parameters for the
	 *         extraction algorithme SABRE and if the column format contains
	 *         annotation column with the proper parameter (if numerical columns
	 *         are considered, the maxDist must be specified; if annotation
	 *         columns are considered, a positive score table must be specified)
	 */
	public boolean isReadyToProcess() {

		return (aacf != null && ((aacf.containColumnsOfType(ColumnType.ANNOTATION) && AnnotationColumn.pst != null)
				|| (aacf.containColumnsOfType(ColumnType.NUMERICAL_ANNOTATION) && NumericalColumn.maxSim != null
						&& NumericalColumn.maxSim != -Double.MAX_VALUE)))
				&& arrays != null && SABRE.getInstance().getParam() != null
				&& SABRE.getInstance().getParam().desired_number_of_alignments > 0;
	}

	/**
	 * Change the column format of the corpus
	 * 
	 * @param new_aacf
	 *            New column format
	 * @param update
	 *            True if the annotated arrays in the corpus must be updated
	 *            according to the new format (otherwise the annotated arrays
	 *            are removed)
	 */
	public void setAACF(AAColumnFormat new_aacf, boolean update) {

		List<String> old_paths = new ArrayList<String>();

		/* Remove all the arrays and add their path in <old_paths> */
		if (arrays != null) {

			for (int i = arrays.size() - 1; i >= 0; i--) {
				old_paths.add(arrays.get(i).getFullPath());
				arrays.remove(i);
				notifyObserverRemoveAA(i);
			}

			arrays = null;
		}

		/* Update the column format */
		aacf = new_aacf;

		/* If the annotated arrays previously in the corpus must be updated */
		if (update) {

			arrays = new ArrayList<AnnotatedArray>();

			try {
				/* For each of the previous path */
				for (String s : old_paths)
					add(s, Main.hasHeader);

			} catch (UndefinedColumnFormatException e) {
				e.printStackTrace();
			}

		}

	}

	public boolean isColumnFormatDefined() {
		return aacf != null;
	}

	public int getTotalNumberOfColumns() {
		return aacf.getTotalNumberOfColumns();
	}

	public ColumnType getColumnType(int i) {
		return aacf.getColumn(i).getType();
	}

	public AbstractColumn<?> getColumn(int i) {
		return aacf.getColumn(i);
	}

	public PositionedColumn getPositionedColumn(int i) {
		return aacf.getPositionedColumn(i);
	}

	public int getNumberOfAnnotations() {
		return annotations.size();
	}

	public boolean isColumnHeaderDefined() {
		return aacf.getColumnHeader() != null;
	}

	public String getColumnHeader(int i) {
		return aacf.getColumnHeader().get(i);
	}

	public void setColumnHeader(ArrayList<String> ch) {
		aacf.setColumnHeader(ch);
	}
}
