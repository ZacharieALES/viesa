package main.machine_learning;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;

import View.StandardView;
import exception.CSVSeparatorNotFoundException;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.InvalidCSVFileNumberOfColumnsException;
import exception.InvalidInputFileException;
import exception.InvalidNumberOfColumnsInInputFilesException;
import exception.UndefinedColumnFormatException;
import extraction.ExtractionAlignments;
import extraction.SABRE;
import extraction.SABREParameter;
import model.AAColumnFormat;
import model.Alignment;
import model.AnnotatedArray;
import model.AnnotationColumn;
import model.CommentColumn;
import model.Corpus;
import model.NumericalColumn;
import model.Pattern;
import model.PointXMLSerializable;
import model.PositionedColumn;

public class MainMohamed {
	
	public AAColumnFormat aacf;
	
	public double mult = 1E-5;
	double scoreMin = 30 * mult;
	double maxDist = 0.08; //1 * mult;
//	double min_seed = 20 * mult;
	int desired_number_of_alignments = 25;
	
	int nbOfAnnotationColumns = 5;
	
	String fo_path = "data/Dialogue_Mohamed/csv/";						

	double desynch = maxDist * nbOfAnnotationColumns / 2;
	double gap = 2*desynch;
	
	ArrayList<Integer> al_comment = new ArrayList<Integer>();
	ArrayList<Integer> al_annot = new ArrayList<Integer>();
	ArrayList<String> folders = new ArrayList<String>();

	public static HashMap<String, Integer> numberOfRecognizedDialogues = new HashMap<String, Integer>();
	
	public MainMohamed(){

		al_comment.add(0);

		for(int i = 1 ; i <= nbOfAnnotationColumns ; ++i)
			al_annot.add(i);
		
		folders.add("a");
		folders.add("co");
		folders.add("e");
		folders.add("cu");
		folders.add("i");
		folders.add("g");
//		folders.add("s");
//		folders.add("t");
//		folders.add("w");

		for(String s : folders)
			numberOfRecognizedDialogues.put(s, 0);

		try {
			aacf = MainMohamed.setColumnFormat(al_comment, null, al_annot);
			NumericalColumn.maxSim = maxDist;
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
			e.printStackTrace();
		}

		runInViesa();
//		runCovering(5);
		
		
	}
	
	
	public void runInViesa(){

		StandardView sv = StandardView.getInstance();
		Corpus.getCorpus().addObserver(sv);
		SABRE.getInstance().addObserver(sv);


		SABRE.getInstance().setParam(new SABREParameter(gap, desynch, 0)); 
			
		try {

			Corpus.getCorpus().setDesiredNumberOfAlignments(desired_number_of_alignments);
			Corpus.getCorpus().setColumnFormat(al_comment, null, al_annot);
			NumericalColumn.maxSim = maxDist;
			

			for(String s: folders)
				Corpus.getCorpus().add(fo_path + s, false);
			
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e1) {e1.printStackTrace();}
		catch (UndefinedColumnFormatException e) {e.printStackTrace();}
			
//		sv.process_extraction();
	}
	
	/**
	 * For each couple of AA compute the best score of a pattern.
	 * For a given AA and each category compute the mean best score of this AA with the AAs of the category
	 */
	public void runMeanBestScore(){			
				
		ArrayList<ArrayList<ClassifiedAA>> arrays = new ArrayList<ArrayList<ClassifiedAA>>();
		
		try {
			
			
			for(String s: folders)
				arrays.add(add(fo_path + s, false, s));
			
			/* For each category */
			for(int c1 = 0 ; c1 < arrays.size() ; ++c1){
				
				ArrayList<ClassifiedAA> al_cat = arrays.get(c1);
				
				/* For each annotated element in this category */
				for(int i = 0 ; i < al_cat.size() ; ++i){
					
					ClassifiedAA aa1 = al_cat.get(i);
					
					/* Compare it with each annotated element of the following categories */
					for(int c2 = c1+1 ; c2 < arrays.size() ; ++c2){
						ArrayList<ClassifiedAA> al_cat2 = arrays.get(c2);
						
						/* For each annotated element in category c2 */
						for(ClassifiedAA aa2 : al_cat2){
							double score = SABRE.getInstance().getBestPatternScoreBetween(aa1, aa2);
							aa1.addScore(aa2, score);
							aa2.addScore(aa1, score);
							
//							System.out.println(score + " : " + aa1.getFileName() + " " + aa2.getFileName());
						}
						
					}
					
					/* Compare it with each of the following annotated element in this category */ 
					for(int j = i+1 ; j < al_cat.size() ; ++j){
						ClassifiedAA aa2 = al_cat.get(j);
						double score = SABRE.getInstance().getBestPatternScoreBetween(aa1, aa2);
						aa1.addScore(aa2, score);
						aa2.addScore(aa1, score);
						
//						System.out.println(score + " : " + aa1.getFileName() + " " + aa2.getFileName());
					}
					
					/* Show the results for aa1 */
					System.out.println(aa1.getFileName());
					aa1.displayBestScoreSumByCategories();
					
				}
				
			}
			
		}
		catch (UndefinedColumnFormatException e) {e.printStackTrace();}
		
		
		
//		sv.process_extraction();
//		sv.process_extraction_and_clustering();
		
		/* Creer un corpus */
			
	}
	
	
	/**
	 * For each couple of AA find the patterns.
	 * Let aa1 be an AA of category c1
	 * Let aa2 be an AA of category c2
	 * 
	 * The coordinate of the obtained patterns are added to the cover of :
	 * 	- aa1 in category c2
	 * 	- aa2 in category c1
	 * 
	 * For each AA and for each category we obtain a covering (i.e., a number of annotations which appear in at least one pattern found with an AA of this category) 
	 * 
	 */
	public void runCovering(int minimalSize){			
				
		ArrayList<ArrayList<ClassifiedAA>> arrays = new ArrayList<ArrayList<ClassifiedAA>>();


		SABRE.getInstance().setParam(new SABREParameter(gap, desynch, 0)); 
		try {
			
			
			for(String s: folders)
				arrays.add(add(fo_path + s, false, s));
			
			/* For each category */
			for(ArrayList<ClassifiedAA> al_aa : arrays)
				
				/* For each aa in the category */
				for(int i = al_aa.size() - 1 ; i >= 0 ; --i){
					
					/* Filter the aa if it does not contain enough lines */
					if(al_aa.get(i).getNumberOfLines() < minimalSize)
						al_aa.remove(i);
				}
						
			
			/* For each category */
			for(int c1 = 0 ; c1 < arrays.size() ; ++c1){
				
					
				ArrayList<ClassifiedAA> al_cat = arrays.get(c1);
				/* If there is enough arrays in this category */
				
				if(al_cat.size() > 1){
				
					/* For each annotated element in this category */
					for(int i = 0 ; i < al_cat.size() ; ++i){
						
						ClassifiedAA aa1 = al_cat.get(i);
						
						/* Compare it with each annotated element of the following categories */
						for(int c2 = c1+1 ; c2 < arrays.size() ; ++c2){
							ArrayList<ClassifiedAA> al_cat2 = arrays.get(c2);
							
							if(al_cat2.size() > 1){
							
								/* For each annotated element in category c2 */
								for(ClassifiedAA aa2 : al_cat2){
		
									ExtractionAlignments al_p = SABRE.getInstance().align(aa1, aa2, scoreMin, false);
									aa1.addPatternsCover(al_p, aa2.category);
									aa2.addPatternsCover(al_p, aa1.category);
									
		//							System.out.println(score + " : " + aa1.getFileName() + " " + aa2.getFileName());
								}
							}
							
						}
						
						/* Compare it with each of the following annotated element in this category */ 
						for(int j = i+1 ; j < al_cat.size() ; ++j){
							ClassifiedAA aa2 = al_cat.get(j);
							
							//TODO Voir le 100
							ExtractionAlignments al_p = SABRE.getInstance().align(aa1, aa2, scoreMin, false);
							aa1.addPatternsCover(al_p, aa2.category);
							aa2.addPatternsCover(al_p, aa1.category);
							
	//						System.out.println(score + " : " + aa1.getFileName() + " " + aa2.getFileName());
						}
						
						/* Show the results for aa1 */
						System.out.println(aa1.getFileName());
						aa1.displayPercentageOfCoverByCategories();
						
					}
				}
				
			}
			
			System.out.println("Results for each category");
			
			/* For each category */
			for(Entry<String, Integer> entry : numberOfRecognizedDialogues.entrySet()) {
				
				/* Get its name */
			    String category = entry.getKey();
			    
			    /* Get the number of arrays correctly classified */
			    Integer value = entry.getValue();
			    
			    /* Get the number of arrays in this category */
			    int nbOfArrays = arrays.get(folders.indexOf(category)).size();
			    
			    System.out.println("\t" + category + ": " + Math.round(value*100.0/nbOfArrays) + " : " + nbOfArrays);
			}
			
			 
			
		}
		catch (UndefinedColumnFormatException e) {e.printStackTrace();}
		
		
		
//		sv.process_extraction();
//		sv.process_extraction_and_clustering();
		
		/* Creer un corpus */
			
	}
	
	public ArrayList<ClassifiedAA> add(String sPath, boolean h_header, String category) throws UndefinedColumnFormatException{
		
		ArrayList<ClassifiedAA> result = new ArrayList<ClassifiedAA>();
		
		if(aacf != null){
		
			File directory = new File(sPath);
	
			/* If the file exist */
			if (!directory.exists())
				System.err.println("File/folder " + directory + " doesn't exist");
			else
			{
	
				File[] subFiles = null;
	
				if (!directory.isDirectory()) {
					subFiles = new File[1];
					subFiles[0] = new File(sPath);
				} else
					subFiles = directory.listFiles();
	
				/* Regexp to test that the end of the file end by ".csv" */
				java.util.regex.Pattern csvRegexp = java.util.regex.Pattern
						.compile(".*csv");
	
				/*
				 * For each file at <sPath>
				 */
				for (int j = 0; j < subFiles.length; j++) {
	
					Matcher fileName = csvRegexp.matcher(subFiles[j].getName());
//					System.out.println("file name : " + subFiles[j].getName());
	
					/* If the file is a csv file */
					if (subFiles[j].isFile() && fileName.matches()) {
	
						try {
							System.out.println("Addition of File: " + subFiles[j]
									.getPath());
							result.add(new ClassifiedAA(subFiles[j]
									.getPath(), h_header, aacf, category));
						} catch (CSVSeparatorNotFoundException e) {
							System.err.println("Invalid csv separator in the file "
									+ subFiles[j]);
							e.printStackTrace();
						} catch (InvalidNumberOfColumnsInInputFilesException e) {
							System.err.println("Invalid number of columns " + subFiles[j]);
							e.printStackTrace();
						} catch (InvalidCSVFileNumberOfColumnsException e) {
							System.err.println("Invalid number of columns " + subFiles[j]);
							e.printStackTrace();
						} catch (InvalidInputFileException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		/* If aacf == null */
		else{
			System.err.println("Undefined column format");
		}
		
		return result;

	}

	public static AAColumnFormat setColumnFormat(
			ArrayList<Integer> commentColumns,
			ArrayList<Integer> annotationColumns,
			ArrayList<Integer> numericalColumns) throws InvalidArgumentsToCreateAnAAColumnFormat{
	
		ArrayList<PositionedColumn> al_pc = new ArrayList<PositionedColumn>();
	
		if(commentColumns != null)
			for(Integer i : commentColumns)
				al_pc.add(new PositionedColumn(new CommentColumn(), i));
	
		if(annotationColumns != null)
			for(Integer i : annotationColumns)
				al_pc.add(new PositionedColumn(new AnnotationColumn(), i));
	
		if(numericalColumns != null)
			for(Integer i : numericalColumns)
				al_pc.add(new PositionedColumn(new NumericalColumn(), i));
		
		return new AAColumnFormat(al_pc);
			
	}
	
	
	public class ClassifiedAA extends AnnotatedArray{


		/**
		 * 
		 */
		private static final long serialVersionUID = -9184982297758062552L;
		String category;
		public HashMap<String, ArrayList<Double>> scoreByCategories;
		public HashMap<String, TreeSet<PointXMLSerializable>> coordinateCoveredByPatterns;
		
		public ClassifiedAA(String s_path, boolean h_header, AAColumnFormat aacf, String category)
				throws CSVSeparatorNotFoundException,
				InvalidNumberOfColumnsInInputFilesException,
				InvalidCSVFileNumberOfColumnsException, InvalidInputFileException {
			super(s_path, h_header, aacf);
			
			this.category = category;
			scoreByCategories = new HashMap<String, ArrayList<Double>>();
			coordinateCoveredByPatterns = new HashMap<String, TreeSet<PointXMLSerializable>>();
		}

		public void displayBestScoreSumByCategories() {
			for(Entry<String, ArrayList<Double>> entry : scoreByCategories.entrySet()) {
			    String key = entry.getKey();
			    ArrayList<Double> value = entry.getValue();
			    
			    double sum = 0.0;
			    int counter = 0;
			    for(Double d : value)
			    	if(d != -Double.MAX_VALUE){
			    		sum += d;
			    		counter++;
			    	}
			    
			    sum /= counter;
			    
			    System.out.println("\t" + key + "\t" + Math.round(sum/mult));
			    
			}
			
		}

		public void displayPercentageOfCoverByCategories() {
			
			double bestValue = 0;
			String bestCategory = "";
						
			double aa_size = this.getNumberOfAnnotationColumns() * this.getNumberOfLines();
//			System.out.println("AA size : " + aa_size + " (" + this.getNumberOfAnnotationColumns() + ", " + this.getNumberOfLines() + ")");
			
			for(Entry<String, TreeSet<PointXMLSerializable>> entry : coordinateCoveredByPatterns.entrySet()) {
			    String c_category = entry.getKey();
			    double annotations_covered = entry.getValue().size();
			    			    
			    double percentage = 100*annotations_covered/aa_size;
			    System.out.println("\t" + c_category + "\t" + percentage);
			    
			    if(percentage > bestValue){
			    	bestValue = percentage;
			    	bestCategory = c_category;
			    }
			    else if(percentage == bestValue)
			    	bestCategory = "";
			    
//			    System.out.println("\t annotations_covered: " + annotations_covered);
			    
			}
//			System.out.println("\tcategory: "+ bestCategory);
			
			if(bestCategory.equals(category)){
				numberOfRecognizedDialogues.put(category, numberOfRecognizedDialogues.get(category) + 1);
				System.out.println("\tRecognized");
			}
			else
				System.out.println("\tUnrecognized (best class: " + bestCategory + ")");
			
		}
		
		/**
		 * Get the coordinate from a list of patterns and them to the covering of a category 
		 * @param al_p
		 * @param category
		 */
		public void addPatternsCover(ExtractionAlignments al_p, String category){
			for(ArrayList<Alignment> al_a : al_p.getAlignments())
				for(Alignment a : al_a){
					addPattern(a.getP1(), category);
					addPattern(a.getP2(), category);
				}
		}
		
		/**
		 * Add the coordinate of a pattern to the covering of a category
		 * @param p
		 * @param cat
		 */
		public void addPattern(Pattern p, String cat){
			
			/* If it is the first pattern which covers the ClassifiedAA for this category */ 
			if(!coordinateCoveredByPatterns.containsKey(cat))
			
				coordinateCoveredByPatterns.put(cat, new TreeSet<PointXMLSerializable>(new Comparator<PointXMLSerializable>(){
					public int compare(PointXMLSerializable c1, PointXMLSerializable c2){
						if(c1.getX() == c2.getX())
							return c1.getY() - c2.getY();
						else
							return c1.getX() - c2.getX();
					}
				}));
				
			if(p.getFileName() == this.getFileName())
				coordinateCoveredByPatterns.get(cat).addAll(p.getcAA());
			
		}

		public void addScore(ClassifiedAA aa2, double score) {
			if(!scoreByCategories.containsKey(aa2.category))
				scoreByCategories.put(aa2.category, new ArrayList<Double>());
			
			scoreByCategories.get(aa2.category).add(score);
		}
		
		
	}
}
