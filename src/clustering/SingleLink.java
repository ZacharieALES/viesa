package clustering;

import java.util.ArrayList;

import model.Corpus;
import model.Pattern;

public class SingleLink extends AbstractClusteringMethod{
	
	private double[][] sl_similarity;
	private int[] maxSim;
	private int size;
	
	private ArrayList<ClusterSet> al_cs;
	private ArrayList<Cluster> al_c;

	/* Merge patterns.size() - 1 clusters */
	private void merge(){

		/* Find the closest pair of patterns p1 p2 */
		int p1 = 0;

		for (int j = 1; j < size; j++)
			if (sl_similarity[j][maxSim[j]] > sl_similarity[p1][maxSim[p1]])
				p1 = j;

		int p2 = maxSim[p1];
		
		/*
		 * Merge clusters p1 and p2 in p1 and set an empty
		 * cluster in p2
		 */
		ClusterSet cs = new ClusterSet(al_cs.get(al_cs.size()-1));
		

		/* Create an arrayList of patterns with all the patterns in the cluster of p1 and p2 */
		ArrayList<Pattern> al_p = new ArrayList<Pattern>(cs.get(p1).getPatterns());
		al_p.addAll(cs.get(p2).getPatterns());
		
		/* Set this new list as the cluster of p1, and set an empty cluster for p2 */
		cs.clusters.set(p1, new Cluster(al_p));
		cs.clusters.set(p2, new Cluster(new ArrayList<Pattern>()));
		al_cs.add(cs);

		/* Update p1's row and column */
		for (int j = 0; j < size; j++)
			if (sl_similarity[p2][j] > sl_similarity[p1][j]){ 
				sl_similarity[p1][j] = sl_similarity[p2][j];
				sl_similarity[j][p1] = sl_similarity[p2][j];
			}

		/* Set p2 row and column to the min value */
		for (int j = 0; j < size; j++){
			sl_similarity[p2][j] = Integer.MIN_VALUE;
			sl_similarity[j][p2] = Integer.MIN_VALUE;
		}

		/* Update maxSim */
		for (int j = 0; j < size; j++) {
			if (maxSim[j] == p2 && j != p1)
				maxSim[j] = p1;
			if (sl_similarity[p1][j] > sl_similarity[p1][maxSim[p1]]	&& j != p1)
				maxSim[p1] = j;
		}
	}


	/* For each ClusterSet */
	private void prune(ClusterSet cs){
		/* For each Cluster */
		for (int j = cs.clusters.size() - 1; j >= 0; j--) {

			Cluster c = cs.clusters.get(j);

			/* If the cluster is empty, remove it */
			if (c.getPatterns().size() == 0)
				cs.clusters.remove(j);

		}
	}
	
	public ArrayList<ClusterSet> getCS(){
		return al_cs;
	}
	
	public int getSize(){
		return size;
	}

	@Override
	public ClusteringSolution cluster() {
		

		firePropertyChange("description", "", "Clustering (SINGLE_LINK)");


		size = Corpus.getCorpus().getPatternSize();
		sl_similarity = new double[size][size];
		for (int i = 0; i < size; i++) {
			sl_similarity[i] = Corpus.getCorpus().getPatternsSimilarity()[i].clone();
			sl_similarity[i][i] = Integer.MIN_VALUE;
		}
		maxSim = new int[size];

		al_cs = new ArrayList<ClusterSet>();
		al_c = new ArrayList<Cluster>();
		
		/**
		 * Single-Link clustering
		 */
		progress = 0.0;
		double invMaxProgress = 1.0/((Corpus.getCorpus().getPatternSize()<<1));
		double step;
		
		if (invMaxProgress != 0)
			step = 100 * invMaxProgress;
		else
			step = 100;
		this.setProgress(0);

		/* Get the closest neighbor for each point and create a cluster for each pattern */
		int i = 0;

		while (i < Corpus.getCorpus().getPatternSize()
				&& !isCancelled()) {

			Pattern p = Corpus.getCorpus().getPattern(i);
			int j = p.getIndex();
			
			/* Create the cluster */
			ArrayList<Pattern> al_p = new ArrayList<Pattern>();
			al_p.add(p);
			this.al_c.add(new Cluster(al_p));

			/* Find the most similar pattern */
			maxSim[j] = 0;

			for (int k = 1; k < size; k++)
				if (sl_similarity[j][k] > sl_similarity[j][maxSim[j]])
					maxSim[j] = k;
			
			this.progress(step);

			i++;
		}

		/* Create the first partition (it only contains clusters reduce to one pattern) */
		al_cs.add(new ClusterSet(al_c));
		
		
		i = 0;

		/* Merge corpus.getPatterns().size() - 1 clusters */
		while (i < Corpus.getCorpus().getPatternSize() - 1
				&& !isCancelled()) {

			merge();
			
			this.progress(step);

			i++;
		}

		/* For each ClusterSet */
		for (ClusterSet cs : getCS()) {
			prune(cs);
		}

		HierarchicalClusteringSolution singleLinkSolution = new HierarchicalClusteringSolution(
				getCS());

		singleLinkSolution.setMethodName("Single-Link");

		
//		System.out.println("Single-Link: end");
		
		return singleLinkSolution;
	}

}
