package main.machine_learning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import exception.AbstractException;
import exception.CSVSeparatorNotFoundException;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.InvalidCSVFileNumberOfColumnsException;
import exception.InvalidInputFileException;
import exception.InvalidNumberOfColumnsInInputFilesException;
import extraction.ExtractionAlignments;
import extraction.SABRE;
import extraction.SABREParameter;
import model.AAColumnFormat;
import model.Alignment;
import model.AnnotatedArray;
import model.NumericalColumn;

/**
 * Contains an AAClass for each considered class of document.
 * Enable to identify representative pattern of the classes
 * 
 * Parameters to fix:
 * - how do you know if a pattern "p" appears in an annotated array "aa" ? 
 * (if the score of the best pattern returned when aligning "p" and "aa" is at least 75% of the one obtained when alignment p with itself)
 *  
 * - how to know if a pattern is interessant to identify classes?
 * (if its maximal frequency in the documents of a class is sufficiently different from its minimal frequency in the documents of another class)  
 *  
 * @author zach
 *
 */
@XmlRootElement
public class ClassRepresentativePatternExtractor implements Serializable{

	private static final long serialVersionUID = 7439922777418319998L;

	/**
	 * List of the AAClass
	 */
	private List<AAClass> l_cc = new ArrayList<>();

	double MINIMUM_SCORE = 10.0;
	//	inal double MINIMUM_SCORE = 0.0;

	/* Percentage of the maximal possible score of a pattern above which it is considered to appear in an AnnotatedArray */ 
	//	private double MIN_PATTERN_PERCENTAGE = 0.8;
	//	private double MIN_PATTERN_PERCENTAGE = 0.75;
	//	private double MIN_PATTERN_PERCENTAGE = 0.5;
	double MIN_PATTERN_PERCENTAGE = 0.6;

	/* Minimal difference in the frequency of two classes above which a pattern is considered relevant to identify document classes */
	double MIN_FREQUENCY_DIFFERENCE = 0.5;

	/* Maximal distance under which two numerical annotations are considered to be similar */
	//	private double MAXIMAL_DISTANCE = 0.09;
	//	private double MAXIMAL_DISTANCE = 0.1;
	double MAXIMAL_DISTANCE = 0.08;

	int nbOfAnnotationColumns = 300;
	int idFirstAnnotationColumn;

	String data_folder = "./data/";
	String corpus_name = "data_converted_nbow-doc_do0.5_train";
	String saveFolder = "./results/dialogue_classification/";

	String characteristics;

	String saveFileName =  saveFolder + corpus_name + characteristics+ ".ser";
	String saveDropboxFileName =  "../../../Dropbox/31_Recherche/Classification_dialogues/results/"+ corpus_name + characteristics+ ".ser";

	String tempSaveFolder = saveFolder + "temp/";

	/* Number of the iteration to save regularly the results */
	int currentIteration = 0;

	/* Number of iterations prior to save the temp file */ 
	@XmlTransient
	int whenToSerialize = 20000;


	public ClassRepresentativePatternExtractor(){}

	/**
	 * Create a PatternExtractor from a path
	 * @param path Path to a folder which contains one subfolder per class. Each subfolder must contains the csv files of its annotated arrays
	 * @param MAXIMAL_DISTANCE Maximal distance between two similar annotations
	 */

	public ClassRepresentativePatternExtractor(String path, int numbOfAnnotationColumns, int idFirstAnnotationColumn){

		nbOfAnnotationColumns = numbOfAnnotationColumns;
		this.idFirstAnnotationColumn = idFirstAnnotationColumn;

		AAColumnFormat aacf = createAACF();

		File directory = new File(path);

		if(!directory.exists()){
			System.out.println("File/folder " + directory +" doesn't exist");

		}else if(!directory.isDirectory()){
			System.out.println(directory + " is a file not a folder");
			System.exit(0);
		}
		// If the argument file is an existing directory
		else{

			File[] subfiles = directory.listFiles();

			// For each file in the main directory
			for(int i=0 ; i<subfiles.length; i++){

				// For each directory in the main directory
				if(subfiles[i].isDirectory()){

					AAClass c_class = new AAClass(subfiles[i].getName());

					File[] subfiles2 = subfiles[i].listFiles();

					// For all csv file in the folder
					for(int j = 0 ; j < subfiles2.length ; ++j){
						java.util.regex.Pattern csvRegexp = java.util.regex.Pattern.compile(".*csv");
						Matcher fileName = csvRegexp.matcher(subfiles2[j].getPath());

						if(fileName.matches()){
							try {
								System.out.println("Add file: " + subfiles2[j].getCanonicalPath());
								c_class.addAA(new ClassifiedAA(subfiles2[j].getAbsolutePath(), false, aacf, c_class.className));
							} catch (AbstractException e) {
								System.err.println(e.getMessage());
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						else
							System.out.println("Warning: the following file - located in the folder of a class - is not a csv file: " + subfiles2[j].getPath());

					} // End: for all csv file in the folder

					l_cc.add(c_class);
				} // End: for each directory in the main directory
			} // End: for each file in the main directory        
		} // End: if the argument file is an existing directory
	} // End: constructor



	public int getIdFirstAnnotationColumn() {
		return idFirstAnnotationColumn;
	}

	@XmlAttribute
	public void setIdFirstAnnotationColumn(int idFirstAnnotationColumn) {
		this.idFirstAnnotationColumn = idFirstAnnotationColumn;
	}

	private AAColumnFormat createAACF() {

		AAColumnFormat aacf = null;

		ArrayList<Integer> al_comment = new ArrayList<>();
		ArrayList<Integer> al_annot = new ArrayList<>();

		//		System.out.println(idFirstAnnotationColumn);

		for(int i = 0 ; i < idFirstAnnotationColumn ; ++i)
			al_comment.add(i);

		for(int i = idFirstAnnotationColumn ; i < idFirstAnnotationColumn + nbOfAnnotationColumns ; ++i)
			al_annot.add(i);

		try {
			aacf = new AAColumnFormat(al_comment, null, al_annot);
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e1) {
			System.err.println(e1.getMessage());
			e1.printStackTrace();
			System.exit(0);
		}

		return aacf;
	}

	public void extractRelevantPatternsFromAllClass(int id_class, int id_firstAA, int id_secondAA){

		boolean isFirstIteration = true;

		for(int i = id_class ; i < l_cc.size() ; ++i){

			if(isFirstIteration){
				extractRelevantPatternsFromClass(i, id_firstAA, id_secondAA);
				isFirstIteration = false;
			}
			else
				extractRelevantPatternsFromClass(i);
		}
	}

	public void extractRelevantPatternsFromAllClass(){
		extractRelevantPatternsFromAllClass(0, 0, 1);
	}

	public void extractRelevantPatternsFromClass(int id_class){
		extractRelevantPatternsFromClass(id_class, 0, 1);
	}

	public void initializeParameters(){

		double sabre_desynch = 100000 * MAXIMAL_DISTANCE * nbOfAnnotationColumns / 2.0;
		SABRE.getInstance().setParam(new SABREParameter(2*sabre_desynch, sabre_desynch));
		NumericalColumn.maxSim = MAXIMAL_DISTANCE;

	}

	public void extractRelevantPatternsFromClass(int id_class, int id_firstAA, int id_secondAA){

		initializeParameters();
		boolean isFirstLoop = true;

		/* If the class id is valid */
		if(id_class >= 0 && id_class < l_cc.size()){

			AAClass c_class = l_cc.get(id_class);

			/* For each couple of AA in this class */
			for(int i = id_firstAA ; i < c_class.aaSize() - 1 ; ++i){

				AnnotatedArray aa1 = c_class.getAA(i);

				/* If it is the first loop use the second id provided (in order to start from where it was left) */
				if(isFirstLoop)
					isFirstLoop = false;
				else
					id_secondAA = i+1;


				for(int j = id_secondAA ; j < c_class.aaSize() ; ++j){

					//					System.out.println("minScore: " + this.MINIMUM_SCORE);
					//					System.out.println("maxDist: " + this.MAXIMAL_DISTANCE);
					//					System.out.println("Numerical column maxDist: " + NumericalColumn.maxSim);
					//					System.out.println("gap SABRE: " + SABRE.getInstance().getParam().gap_cost);
					//					System.out.println("NumCol maxSim: " + NumericalColumn.maxSim);
					//
					//					System.out.println("AA1: ");
					//					System.out.println(aa1);
					//					System.out.println("AA2: ");
					//					System.out.println(c_class.getAA(j));

					/* Extract the patterns */
					ExtractionAlignments ea = SABRE.getInstance().align(aa1, c_class.getAA(j), MINIMUM_SCORE, false);

					System.out.println(id_class + "/" + l_cc.size() +" " + i+"/" +(c_class.aaSize()-2) + " " + j + "/" + (c_class.aaSize()-1) + "(" + ea.getAlignments().size() + " alignments)");
					//					System.out.println("alignment of: " + aa1.getFileName() + "\nand: " + c_class.getAA(j) + "\n---");

					//					for(List<Alignment> la: ea.alignments){
					//						for(Alignment a: la){
					//							System.out.println("Pattern in 1: ");
					//							System.out.println(a.getP1().getCoordinates());
					//							System.out.println(a.getP1());
					//							System.out.println("Pattern in 2: ");
					//							System.out.println(a.getP2().getCoordinates());
					//							System.out.println(a.getP2());
					//							System.out.println("---");
					//						}
					//					}

					//					List<FrequencedPattern> unsuitablePatterns = new ArrayList<>();

					/* For each extracted alignment */
					for(ArrayList<Alignment>  al_a : ea.getAlignments())
						for(Alignment a : al_a){
							FrequencedPattern fp1 = new FrequencedPattern(a.getP1(), c_class);
							//							FrequencedPattern fp2 = new FrequencedPattern(a.getP2(), MINIMUM_SCORE, MIN_PATTERN_PERCENTAGE);

							//							System.out.println(fp1);

							/* If the pattern has not already been identified as relevant for this class */
							if(!fp1.isAlreadyContainedInClass(c_class, MIN_PATTERN_PERCENTAGE, MINIMUM_SCORE)){
								//							if(true){

								/* If the pattern has not already been identified as irrelevant */
								//								if(!fp1.isAlreadyConsideredUnsuitable(unsuitablePatterns, MIN_PATTERN_PERCENTAGE, MINIMUM_SCORE))
								{
									double frequency;
									//
									//									fp1.computeFrequencyForEachClass(l_cc, MIN_PATTERN_PERCENTAGE, MINIMUM_SCORE);
									//
									//									//									System.out.println(fp1.getFrequenceByClass());
									//
									//									double diff_freq1 = fp1.greatestDifferenceInFrequency();
									//
									//									if(fp1.getMostFrequentClass().equals(c_class.className) && diff_freq1 >= MIN_FREQUENCY_DIFFERENCE)
									//										c_class.addPattern(fp1);
									//									//									else
									//									//										unsuitablePatterns.add(fp1);



									frequency = fp1.computeFrequencyForItsClass(MIN_PATTERN_PERCENTAGE, MINIMUM_SCORE);

									/* If the pattern appears enough in its own class */
									if(frequency >= MIN_FREQUENCY_DIFFERENCE){

										fp1.computeFrequencyForOtherClass(l_cc, MIN_PATTERN_PERCENTAGE, MINIMUM_SCORE);

										//								/* Compute the frequency of the two patterns in all classes */ 
										//								fp1.computeFrequencyForEachClass(l_cc);
										//							fp2.computeFrequencyForEachClass(l_cc);

										double diff_freq1 = fp1.greatestDifferenceInFrequency();
										//							double diff_freq2 = fp2.greatestDifferenceInFrequency();

										/* If the frequency of the best pattern is high enough */
										if(fp1.getMostFrequentClass().equals(c_class.className) && diff_freq1 >= MIN_FREQUENCY_DIFFERENCE)
											//								if(diff_freq1 >= diff_freq2)
											c_class.addPattern(fp1);
										//								else
										//									c_class.addPattern(fp2);
										//							else if(diff_freq2 >= MIN_FREQUENCY_DIFFERENCE)
										//								c_class.addPattern(fp2);
									}
								}
							}
						}

					currentIteration++;

					if(currentIteration % whenToSerialize == 0)
					{

						for(int ic = 0 ; ic < this.l_cc.size() ; ++ic){

							AAClass c = l_cc.get(ic);


							if(c.getL_p().size() > 0){


								//								this.serialiseTempFile(id_class, i, j);

//								this.serialiseTempFileXML(id_class, i, j);

								//								System.out.println("--------- PATTERN");
								//								for(PointXMLSerializable pxs: c.getL_p().get(0).getP().getcAA())
								//									System.out.print(pxs.getX() + "-"+ pxs.getY() + " " );
								//								System.out.println();
								//								System.out.println(c.getL_p().get(0).getP().getPatternAA().displayNumericalColumns());
								//								System.out.println("\t" + c.getL_p().get(0).getFrequenceByClass());
								//								
								//								ClassRepresentativePatternExtractor crpe = this.deserialiseXML(tempSaveFolder + "/" + characteristics + "_class_" + id_class + "_1stPattern_" + i + "_2ndPattern_" + j + ".xml");
								//
								//								
								//								System.out.println("--------- PATTERN SAVED");
								//								for(PointXMLSerializable pxs: c.getL_p().get(0).getP().getcAA())
								//									System.out.print(pxs.getX() + "-"+ pxs.getY() + " " );
								//								System.out.println();
								//								System.out.println(crpe.getL_cc().get(ic).getL_p().get(0).getP().getPatternAA().displayNumericalColumns());
								//
								//								System.out.println("\t" + crpe.getL_cc().get(ic).getL_p().get(0).getFrequenceByClass());
								//							
								//								System.exit(0);

							}

						}

					}
				}
			} //for(int i = 0 ; i < c_class.aaSize() - 1 ; ++i){

			System.out.println("  relevant patterns in class " + c_class.getClassName() + " : " + c_class.pSize());
		}

	}


	//	public static void main(String[] args){
	//
	//		int idFirstAnnotationColumn = 1;
	//
	//		ClassRepresentativePatternExtractor temp_crpe = new ClassRepresentativePatternExtractor();
	//		temp_crpe.setIdFirstAnnotationColumn(idFirstAnnotationColumn);
	//
	//		if(args != null && args.length >= 4){
	//			temp_crpe.MINIMUM_SCORE = Double.parseDouble(args[0]);
	//			temp_crpe.MAXIMAL_DISTANCE = Double.parseDouble(args[1]);
	//			temp_crpe.MIN_PATTERN_PERCENTAGE = Double.parseDouble(args[2]);
	//			temp_crpe.MIN_FREQUENCY_DIFFERENCE = Double.parseDouble(args[3]);
	//
	//			System.out.println("Input arguments found:"
	//					+ "\n\tmin score: " + temp_crpe.MINIMUM_SCORE
	//					+ "\n\tmax dist: " + temp_crpe.MAXIMAL_DISTANCE
	//					+ "\n\tmin pattern percentage: " + temp_crpe.MIN_PATTERN_PERCENTAGE
	//					+ "\n\tmin freq difference: " + temp_crpe.MIN_FREQUENCY_DIFFERENCE
	//					);
	//
	//		}
	//
	//		temp_crpe.setCharacteristics(temp_crpe.characToString());
	//
	//		//		System.out.println("Temp charact null: " + (temp_crpe.characteristics == null));
	//		//		System.out.println(temp_crpe.characteristics);
	//
	//		ClassRepresentativePatternExtractor crpe;
	//
	//		double sabre_desynch = temp_crpe.MAXIMAL_DISTANCE * temp_crpe.nbOfAnnotationColumns / 2.0;
	//		SABRE.getInstance().setParam(new SABREParameter(2*sabre_desynch, sabre_desynch));
	//		NumericalColumn.maxSim = temp_crpe.MAXIMAL_DISTANCE;
	//
	//		File f = temp_crpe.getSaveFile();
	//
	//		/* If the pattern have not been extracted with this characteristics on this corpus */
	//		if(f == null || !f.exists()){
	//
	//			/* If the extraction has been previously started */
	//			if((f = temp_crpe.correspondingTempFileExists()) != null){
	//
	//				/* Finish it */
	//				System.out.println("Filename does not exist, but temp file does, resuming pattern extraction (filname: " + f.getName() + ")");
	//
	//
	//				//				String saveFile = characteristics + "_class_" + id_class + "_1stPattern_" + id1 + "_2ndPattern_" + id2 + ".ser";
	//
	//				/* Get the ids from the file name */
	//				String s_temp = f.getName().split("_class_")[1];
	//				String[] as_temp = s_temp.split("_1stPattern_");
	//				Integer id_class = Integer.parseInt(as_temp[0]);
	//				as_temp = as_temp[1].split("_2ndPattern_");
	//				Integer id_1stPattern = Integer.parseInt(as_temp[0]);
	//				Integer id_2ndPattern = Integer.parseInt(as_temp[1].split(".xml")[0]);
	//				//				Integer id_2ndPattern = Integer.parseInt(as_temp[1].split(".ser")[0]);
	//
	//				crpe = temp_crpe.deserialiseXML(f.getAbsolutePath());
	//				crpe.sameParametersThan(temp_crpe);
	//
	//				//				System.out.println("Current charact null: " + (crpe.characteristics == null));
	//				System.out.println("Save file (at then end of the process): " + crpe.saveFileName);
	//
	//				crpe.extractRelevantPatternsFromAllClass(id_class, id_1stPattern, id_2ndPattern);
	//
	//			}
	//			/* If a compatible save file exists */
	//			else if(temp_crpe.getApproximatedFile() != null){ 
	//
	//				f = temp_crpe.getApproximatedFile();
	//				System.out.println("Filename does not exist, but an approximated file has been found: " + f.getName());
	//				System.out.println("Deserialization...");
	//				crpe = ClassRepresentativePatternExtractor.deserialise(f.getAbsolutePath());
	//				crpe.sameParametersThan(temp_crpe);
	//
	//				/* Get all the patterns in one list */
	//				List<FrequencedPattern> l_fp = new ArrayList<>();
	//
	//				/* For each class */
	//				for(int i = 0 ; i < crpe.l_cc.size() ; ++i){
	//
	//					/* Add the frequenced patterns in l_fp */
	//					l_fp.addAll(crpe.l_cc.get(i).getL_p());
	//					crpe.l_cc.get(i).getL_p().clear();
	//
	//				}
	//
	//				int originalNbOfPatterns = l_fp.size();
	//
	//				/* If the current minimal percentage above which a pattern is considered to be in an aa
	//				 * is greater than the one in the saved file 
	//				 */
	//				if(getMinPatternsPercentage(f) <= crpe.MIN_PATTERN_PERCENTAGE){
	//
	//					/* Recompute all the percentages and only keep the relevant patterns */
	//
	//					/* For each pattern */
	//					for(int i = l_fp.size()-1 ; i >= 0 ; --i){
	//
	//						System.out.print("Recompute frequencies of patterns " + i + "/" + l_fp.size() + " ");
	//						FrequencedPattern fp = l_fp.get(i);
	//
	//						/* Compute its frequency */
	//						fp.computeFrequencyForEachClass(crpe.l_cc);
	//
	//						System.out.println(fp.greatestDifferenceInFrequency());
	//
	//					}
	//
	//				}
	//
	//				/* For each pattern */
	//				for(int i = l_fp.size()-1 ; i >= 0 ; --i){
	//
	//					FrequencedPattern fp = l_fp.get(i);
	//					System.out.print(i + "/" + l_fp.size() + " " + fp.greatestDifferenceInFrequency() + "% ");
	//
	//					/* If the greatest difference is not valid, remove it */
	//					if(fp.greatestDifferenceInFrequency() < crpe.MIN_FREQUENCY_DIFFERENCE){
	//						l_fp.remove(i);
	//						System.out.println("(removed)");
	//					}
	//					else
	//						System.out.println("(kept)");
	//				}
	//
	//				System.out.println(l_fp.size() + "/" + crpe.nbOfAnnotationColumns + " patterns kept");
	//
	//				crpe.l_cc.get(0).getL_p().addAll(l_fp);
	//
	//				crpe.serialise(crpe.saveFileName + "_filtered");
	//
	//
	//			}
	//
	//			/* If this is a new extraction */
	//			else{
	//
	//				System.out.println("Filename does not exist, starting pattern extraction (filname: " + temp_crpe.saveFileName + ")");
	//				//		crpe = new ClassRepresentativePatternExtractor("./data/Dialogue_Mohamed/csv/", 10, 1);
	//				//		crpe = new ClassRepresentativePatternExtractor("./data/Dialogue_Mohamed/csv/", 100, 1)
	//				crpe = new ClassRepresentativePatternExtractor(temp_crpe.data_folder + temp_crpe.corpus_name + "/", temp_crpe.nbOfAnnotationColumns, 1);
	//				crpe.sameParametersThan(temp_crpe);
	//				//			crpe = new ClassRepresentativePatternExtractor("./data/data_converted_nbow-doc_do0.5_cut/", corpus, nbOfAnnotationColumns, 1);
	//
	//				crpe.extractRelevantPatternsFromAllClass();
	//			}
	//
	//			//			crpe.serialise(saveDropboxFileName);
	//
	//			System.out.println("Save results in: " + crpe.saveFileName);
	//			crpe.serialise(crpe.saveFileName);
	//		}
	//		else{		
	//
	//			System.out.println("Serialized file found.\n Deserialization...");
	//			crpe = ClassRepresentativePatternExtractor.deserialise(temp_crpe.saveFileName);
	//			System.out.println(".");
	//
	//		}
	//
	//		/* Display the patterns frequency: For each class */
	//		for(AAClass aac : crpe.l_cc){
	//
	//			System.out.println("- " + aac.getClassName());
	//
	//			/* For each frequent pattern */
	//			for(int i = 0 ; i < Math.min(aac.pSize(), 15) ; ++i){
	//				System.out.println("\t" + aac.getPattern(i).getFrequenceByClass());
	//			}
	//
	//			if(aac.pSize() > 15){
	//				System.out.println("... " + (aac.pSize()-15) + " remaining patterns");
	//			}
	//
	//		}
	//
	//		/* For each class */
	//		String output = "";
	//		for(AAClass aac : crpe.l_cc){
	//
	//			int nbOfProperlyClassifiedAA = 0;
	//
	//			/* For each AA in this class */
	//			for(ClassifiedAA caa : aac.getL_aa())
	//				if(crpe.classify(caa, false))
	//					nbOfProperlyClassifiedAA++;
	//
	//			String temp = (aac.className + ": " + Math.round(100.0*((double)nbOfProperlyClassifiedAA)/aac.getL_aa().size()) + "%") + "\n";;
	//			System.out.print(temp);
	//			output += temp;
	//
	//		}
	//
	//		System.out.println(output);
	//
	//
	//	}
	//

	public static ClassRepresentativePatternExtractor extractRepresentativePatterns(String dataFolder, String corpusName, int nbOfAnnotationColumn, double minScore, double maxDist, double minFrequencyDifference, double minPatternPercentage, int idFirstAnnotationColumn){

		/* Temp object used to check if a saved file exists */
		ClassRepresentativePatternExtractor temp_crpe = new ClassRepresentativePatternExtractor();
		temp_crpe.idFirstAnnotationColumn = idFirstAnnotationColumn;
		temp_crpe.MINIMUM_SCORE = minScore;
		temp_crpe.MAXIMAL_DISTANCE = maxDist;
		temp_crpe.MIN_PATTERN_PERCENTAGE = minPatternPercentage;
		temp_crpe.MIN_FREQUENCY_DIFFERENCE = minFrequencyDifference;

		temp_crpe.data_folder = dataFolder;
		temp_crpe.nbOfAnnotationColumns = nbOfAnnotationColumn;
		temp_crpe.corpus_name = corpusName;
		temp_crpe.setCharacteristics(temp_crpe.characToString()); // Also set the save file name

		ClassRepresentativePatternExtractor crpe;

		File f = temp_crpe.getSaveFile();

		/* If the pattern have not been extracted with this characteristics on this corpus */
		if(f == null || !f.exists()){

			//			/* If the extraction has been previously started */
			//			if((f = temp_crpe.correspondingTempFileExists()) != null){
			//
			//				/* Finish it */
			//				System.out.println("Filename does not exist, but temp file does, resuming pattern extraction (filname: " + f.getName() + ")");
			//
			//
			//				//	String saveFile = characteristics + "_class_" + id_class + "_1stPattern_" + id1 + "_2ndPattern_" + id2 + ".ser";
			//
			//				/* Get the ids from the file name */
			//				String s_temp = f.getName().split("_class_")[1];
			//				String[] as_temp = s_temp.split("_1stPattern_");
			//				Integer id_class = Integer.parseInt(as_temp[0]);
			//				as_temp = as_temp[1].split("_2ndPattern_");
			//				Integer id_1stPattern = Integer.parseInt(as_temp[0]);
			//				Integer id_2ndPattern = Integer.parseInt(as_temp[1].split(".xml")[0]);
			//				//				Integer id_2ndPattern = Integer.parseInt(as_temp[1].split(".ser")[0]);
			//
			//				crpe = temp_crpe.deserialiseXML(f.getAbsolutePath());
			//				crpe.sameParametersThan(temp_crpe);
			//
			//				//				System.out.println("Current charact null: " + (crpe.characteristics == null));
			//				System.out.println("Save file (at then end of the process): " + crpe.saveFileName);
			//
			//				crpe.extractRelevantPatternsFromAllClass(id_class, id_1stPattern, id_2ndPattern);
			//
			//			}
			//			/* If a compatible save file exists */
			//			else if(temp_crpe.getApproximatedFile() != null){ 
			//
			//				f = temp_crpe.getApproximatedFile();
			//				System.out.println("Filename does not exist, but an approximated file has been found: " + f.getName());
			//				System.out.println("Deserialization...");
			//				crpe = ClassRepresentativePatternExtractor.deserialise(f.getAbsolutePath());
			//				crpe.sameParametersThan(temp_crpe);
			//
			//				/* Get all the patterns in one list */
			//				List<FrequencedPattern> l_fp = new ArrayList<>();
			//
			//				/* For each class */
			//				for(int i = 0 ; i < crpe.l_cc.size() ; ++i){
			//
			//					/* Add the frequenced patterns in l_fp */
			//					l_fp.addAll(crpe.l_cc.get(i).getL_p());
			//					crpe.l_cc.get(i).getL_p().clear();
			//
			//				}
			//
			//				int originalNbOfPatterns = l_fp.size();
			//
			//				/* If the current minimal percentage above which a pattern is considered to be in an aa
			//				 * is greater than the one in the saved file 
			//				 */
			//				if(getMinPatternsPercentage(f) <= crpe.MIN_PATTERN_PERCENTAGE){
			//
			//					/* Recompute all the percentages and only keep the relevant patterns */
			//
			//					/* For each pattern */
			//					for(int i = l_fp.size()-1 ; i >= 0 ; --i){
			//
			//						System.out.print("Recompute frequencies of patterns " + i + "/" + l_fp.size() + " ");
			//						FrequencedPattern fp = l_fp.get(i);
			//
			//						/* Compute its frequency */
			//						fp.computeFrequencyForEachClass(crpe.l_cc);
			//
			//						System.out.println(fp.greatestDifferenceInFrequency());
			//
			//					}
			//
			//				}
			//
			//				/* For each pattern */
			//				for(int i = l_fp.size()-1 ; i >= 0 ; --i){
			//
			//					FrequencedPattern fp = l_fp.get(i);
			//					System.out.print(i + "/" + l_fp.size() + " " + fp.greatestDifferenceInFrequency() + "% ");
			//
			//					/* If the greatest difference is not valid, remove it */
			//					if(fp.greatestDifferenceInFrequency() < crpe.MIN_FREQUENCY_DIFFERENCE){
			//						l_fp.remove(i);
			//						System.out.println("(removed)");
			//					}
			//					else
			//						System.out.println("(kept)");
			//				}
			//
			//				System.out.println(l_fp.size() + "/" + crpe.nbOfAnnotationColumns + " patterns kept");
			//
			//				crpe.l_cc.get(0).getL_p().addAll(l_fp);
			//
			//				crpe.serialise(crpe.saveFileName + "_filtered");
			//
			//
			//			}

			/* If this is a new extraction */
			//			else
			{

				System.out.println("Filename does not exist, starting pattern extraction (filname: " + temp_crpe.saveFileName + ")");
				//		crpe = new ClassRepresentativePatternExtractor("./data/Dialogue_Mohamed/csv/", 10, 1);
				//		crpe = new ClassRepresentativePatternExtractor("./data/Dialogue_Mohamed/csv/", 100, 1)
				crpe = new ClassRepresentativePatternExtractor(temp_crpe.data_folder + temp_crpe.corpus_name + "/", temp_crpe.nbOfAnnotationColumns, idFirstAnnotationColumn);
				crpe.sameParametersThan(temp_crpe);
				//			crpe = new ClassRepresentativePatternExtractor("./data/data_converted_nbow-doc_do0.5_cut/", corpus, nbOfAnnotationColumns, 1);

				crpe.extractRelevantPatternsFromAllClass();
			}

			//			crpe.serialise(saveDropboxFileName);

			System.out.println("Save results in: " + crpe.saveFileName);
			crpe.serialise(crpe.saveFileName);
		}
		else{		

			System.out.println("Serialized file found.\n Deserialization...");
			crpe = ClassRepresentativePatternExtractor.deserialise(temp_crpe.saveFileName);
			System.out.println(".");

		}

		/* Display the patterns frequency: For each class */
		for(AAClass aac : crpe.l_cc){

			System.out.println("- " + aac.getClassName() + " : " + aac.getL_p().size() + " patterns");
			//			System.out.println("- " + aac.getClassName());
			//
			//			/* For each frequent pattern */
			//			for(int i = 0 ; i < Math.min(aac.pSize(), 15) ; ++i){
			//				System.out.println("\t" + aac.getPattern(i).getFrequenceByClass());
			//			}
			//
			//			if(aac.pSize() > 15){
			//				System.out.println("... " + (aac.pSize()-15) + " remaining patterns");
			//			}

		}

		return crpe;


	}

	public void classifyTrainDialogues(){

		classifyCorpus(l_cc);

	}

	public void classifyCorpus(List<AAClass> corpus){

		initializeParameters();

		/* For each class */
		String output = "";
		for(AAClass aac : corpus){

			int nbOfProperlyClassifiedAA = 0;

			/* For each AA in this class */
			for(ClassifiedAA caa : aac.getL_aa())
				if(classify(caa, false))
					nbOfProperlyClassifiedAA++;

			String temp = (aac.className + ": " + Math.round(100.0*((double)nbOfProperlyClassifiedAA)/aac.getL_aa().size()) + "%") + "\n";;
			System.out.print(temp);
			output += temp;

		}

		System.out.println(output);
	}


	void sameParametersThan(ClassRepresentativePatternExtractor temp_crpe) {
		// TODO Auto-generated method stub
		MINIMUM_SCORE = temp_crpe.MINIMUM_SCORE;
		MAXIMAL_DISTANCE = temp_crpe.MAXIMAL_DISTANCE;
		MIN_PATTERN_PERCENTAGE = temp_crpe.MIN_PATTERN_PERCENTAGE;
		MIN_FREQUENCY_DIFFERENCE = temp_crpe.MIN_FREQUENCY_DIFFERENCE;

		idFirstAnnotationColumn = temp_crpe.idFirstAnnotationColumn;
		data_folder = temp_crpe.data_folder;
		corpus_name = temp_crpe.corpus_name;
		nbOfAnnotationColumns = temp_crpe.nbOfAnnotationColumns;

		setCharacteristics(temp_crpe.characteristics);
		System.out.println("nbOfColumns: " + nbOfAnnotationColumns);
	}

	public File correspondingTempFileExists() {

		File result = null;

		File t_folder = new File(tempSaveFolder);

		if(t_folder.exists()){

			File[] subfiles = t_folder.listFiles();
			int i = 0;

			System.out.println("Characteristics: " + characteristics);
			while(result == null && i < subfiles.length){

				//				System.out.println("Test for temp file: " + subfiles[i]);

				if(subfiles[i].getPath().contains(characteristics)){
					result = subfiles[i];
				}

				++i;
			}

		}

		return result;
	}

	/**
	 * 
	 * @param caa
	 * @return True if caa is properly classified; false otherwise
	 */
	private boolean classify(ClassifiedAA caa, boolean useFirstCriterion) {

		HashMap<String, Double> probabilityByClass = new HashMap<>();
		HashMap<String, Integer> nbOfContainedPatterns = new HashMap<>();

		/* Initialize the probability */
		for(AAClass aac : this.l_cc){
			probabilityByClass.put(aac.className, 0.0);
			nbOfContainedPatterns.put(aac.className, 0);
		}

		int numberOfFrequentPatterns = 0;


		/* For each class */
		for(int id_class = 0 ; id_class < this.l_cc.size() ; ++id_class){

			AAClass aac = this.l_cc.get(id_class);

			numberOfFrequentPatterns += aac.getL_p().size();

			/* For each frequent pattern in this class */
			for(FrequencedPattern fp : aac.getL_p()){

				/* Is the pattern contained in the annotated array */
				boolean isContained = fp.isContainedInCAA(caa, MIN_PATTERN_PERCENTAGE, MINIMUM_SCORE);

				/* For each class */
				for (Map.Entry<String, Double> entry : fp.getFrequenceByClass().entrySet()) {

					/* Get the frequence of <fp> in this class */
					Double frequence = entry.getValue();

					/* Get the current proba for this class */
					Double proba = probabilityByClass.get(entry.getKey());

					if(isContained && aac.className.equals(entry.getKey())){
						//					if(isContained){
						////						proba += frequence;
						//						proba += frequence/aac.getL_p().size();
						proba += 1.0/aac.getL_p().size();
						//						System.out.println("Class " + aac.className + " add proba " + (1.0/aac.getL_p().size()) );

						//						System.out.println("Class " + aac.className + " + " + (1.0/aac.getL_p().size()));

						nbOfContainedPatterns.put(entry.getKey(), nbOfContainedPatterns.get(entry.getKey())+1);
						probabilityByClass.put(entry.getKey(), proba); 
					}
					else
						if(useFirstCriterion){
							proba += (1.0-frequence);
							probabilityByClass.put(entry.getKey(), proba);
						}


				}


			}
		}


		//		/* For each class */
		//		for(int id_class = 0 ; id_class < this.l_cc.size() ; ++id_class){
		//
		//			/* Divide the proba by the number of patterns in it */
		//			AAClass aac = this.l_cc.get(id_class);			
		//			probabilityByClass.put(aac.className, probabilityByClass.get(aac.className)/aac.getL_p().size());
		//		}

		double probaMax = -Double.MAX_VALUE;
		String classMax = "";

		String  output = new String(caa.aa_class) + " - ";

		/* For each class */
		for (Map.Entry<String, Double> entry : probabilityByClass.entrySet()) {

			Double proba = entry.getValue();

			//			if(useFirstCriterion)
			//				proba /= (double)numberOfFrequentPatterns;
			//			else
			//				proba /= (double)nbOfContainedPatterns.get(entry.getKey()) ;

			if(proba > probaMax){
				probaMax = proba;
				classMax = entry.getKey();
			}

			probabilityByClass.put(entry.getKey(), proba);
			//			output += entry.getKey() + "=" + (Math.round(100*entry.getValue())) + ", ";
			output += entry.getKey() + "=" + (entry.getValue()) + ", ";
		}

		boolean result = false;

		if(classMax.equals(caa.aa_class)){
			result = true;
			output = "!\t" + output;
		}
		else
			output = "(predit " + classMax + ") " + output;

		System.out.println("\t" + output);

		return result;
	}


	public void serialise(String fileName){

		ObjectOutputStream oos = null;

		try {
			final FileOutputStream outputFile = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(outputFile);
			oos.writeObject(this);
			oos.flush();
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void serialiseTempFile(int id_class, int id1, int id2){

		File t_folder = new File(tempSaveFolder);

		/* If the folder does not exist, create it */
		if(!t_folder.exists())
			t_folder.mkdir();

		/* If the folder already exists */
		else{

			/* Remove all the previously saved temp files which correspond to the same characteristics 
			 * (to only keep the more recent one) */
			File[] subfiles = t_folder.listFiles();

			for(int i=0 ; i<subfiles.length; i++)
				if(subfiles[i].getName().contains(characteristics))
					subfiles[i].delete();
		}

		String saveFile = tempSaveFolder + "/" + characteristics + "_class_" + id_class + "_1stPattern_" + id1 + "_2ndPattern_" + id2 + ".ser";

		ObjectOutputStream oos = null;

		try {
			final FileOutputStream outputFile = new FileOutputStream(saveFile);
			oos = new ObjectOutputStream(outputFile);
			oos.writeObject(this);
			oos.flush();
			System.out.println("Saved temp file: " + saveFile);
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void serialiseTempFileXML(int id_class, int id1, int id2){


		File t_folder = new File(tempSaveFolder);

		/* If the folder does not exist, create it */
		if(!t_folder.exists())
			t_folder.mkdir();

		/* If the folder already exists */
		else{

			/* Remove all the previously saved temp files which correspond to the same characteristics 
			 * (to only keep the more recent one) */
			File[] subfiles = t_folder.listFiles();

			for(int i=0 ; i<subfiles.length; i++)
				if(subfiles[i].getName().contains(characteristics))
					subfiles[i].delete();
		}

		String saveFile = tempSaveFolder + "/" + characteristics + "_class_" + id_class + "_1stPattern_" + id1 + "_2ndPattern_" + id2 + ".xml";


		try {

			for(AAClass aac: l_cc)
				for(FrequencedPattern fp: aac.getL_p())
					fp.getP().prepareToXMLSave();

			JAXBContext jaxbContext = JAXBContext.newInstance(ClassRepresentativePatternExtractor.class);
			//class responsible for the process of 
			//serializing Java object into XML data
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			//marshalled XML data is formatted with linefeeds and indentation
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//specify the xsi:schemaLocation attribute value 
			//to place in the marshalled XML output
			//			jaxbMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, 
			//					"http://www.mysamplecode.com/ws/v10 OrderService_v10.xsd");

			//			//send to console
			//			jaxbMarshaller.marshal(this, System.out);
			//send to file system
			OutputStream os = new FileOutputStream(saveFile);
			jaxbMarshaller.marshal(this, os );

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}



	}

	public static ClassRepresentativePatternExtractor deserialise(String fileName){
		ObjectInputStream ois = null;
		ClassRepresentativePatternExtractor result = null;

		try {
			final FileInputStream inputFile = new FileInputStream(fileName);
			ois = new ObjectInputStream(inputFile);
			result = (ClassRepresentativePatternExtractor) ois.readObject();
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}

		return result; 

	}

	public ClassRepresentativePatternExtractor deserialiseXML(String fileName){

		ClassRepresentativePatternExtractor crpe = null;

		try {

			//create file input stream
			InputStream is = new FileInputStream(fileName);
			//XML and Java binding 
			JAXBContext jaxbContext = JAXBContext.newInstance(ClassRepresentativePatternExtractor.class);

			//class responsible for the process of deserializing 
			//XML data into Java object
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			crpe = (ClassRepresentativePatternExtractor) jaxbUnmarshaller.unmarshal(is);

			AAColumnFormat aacf = createAACF();

			/* For each class */
			for(AAClass c: crpe.getL_cc()){

				List<ClassifiedAA> newL_aa = new ArrayList<>();

				/* For each classified annotated array */
				for(ClassifiedAA aa: c.getL_aa()){

					/* Recreate the annotated array (as only the filename has been saved (i.e., not the annotations) in the XML file */
					try {
						newL_aa.add(new ClassifiedAA(aa.getFullPath(), false, aacf, aa.aa_class));
					} catch (CSVSeparatorNotFoundException e) {
						e.printStackTrace();
					} catch (InvalidNumberOfColumnsInInputFilesException e) {
						e.printStackTrace();
					} catch (InvalidCSVFileNumberOfColumnsException e) {
						e.printStackTrace();
					} catch (InvalidInputFileException e) {
						e.printStackTrace();
					}

				}

				c.setL_aa(newL_aa);

				/* For each frequent pattern */
				for(FrequencedPattern fp: c.getL_p()){

					fp.getP().restoreFromXMLSave();

					/* Associate its corresponding original annotated array */
					ClassifiedAA result = null;
					int i = 0;

					while(result == null && i < c.getL_aa().size()){

						if(c.getL_aa().get(i).getFullPath().contains(fp.getP().getFileName()))
							result = c.getL_aa().get(i);					  

						++i;
					}

					if(result != null)
						fp.getP().setOriginalAA(result);
					else{
						System.err.println("Error: annotated array not found in class.\n\tPath according to the pattern: " + fp.getP().getFileName() + "\n\tPath in the class: ");
						for(ClassifiedAA caa: c.getL_aa())
							System.err.println(caa.getFullPath());
					}


				}
			}



		} catch (JAXBException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return crpe;

	}

	public static String doubleToString(double d, int digitAfter0){
		double power = Math.pow(10.0, digitAfter0);
		return ((Double)(Math.round(d*power)/power)).toString();
	}

	/**
	 * Get the save file which corresponds exactly to the current parameters (or null if none)
	 * @return
	 */
	public File getSaveFile(){

		/* Check if the exact save file exists */
		return new File(saveFileName);

	}

	/**
	 * Get the save file which corresponds to parameters compatible with the current one (or null if none)
	 * @return
	 */
	public File getApproximatedFile(){

		File folder = new File(saveFolder);
		File result = null;


		if (folder.exists()){

			int i = 0;
			boolean found = false;

			while (!found && i < folder.listFiles().length){

				System.out.println("Test file: " + folder.listFiles()[i].getName());
				if(isCompatible(folder.listFiles()[i])){
					found = true;
					result = folder.listFiles()[i];
				}
				else
					++i;
			}
		}

		return result;
	}

	/**
	 * Test if a serialized file is compatible with the current parameters 
	 * Parameters are compatible if:
	 * - the data set is the same
	 * - the MINIMUM_SCORE and MAXIMAL_DISTANCE are identical; and
	 * - the MIN_PATTERN_PERCENTAGE is greater than or equal.
	 * 
	 * @param f The serialized file to test
	 * @return True if the file is compatible; false otherwise
	 */
	public boolean isCompatible(File f){

		String fileName = f.getName();

		if(corpus_name.equals(getDataSet(f))
				&& getMinScore(f) == MINIMUM_SCORE
				&& getMaxDist(f) == MAXIMAL_DISTANCE
				&& getMinPatternsPercentage(f) == MIN_PATTERN_PERCENTAGE
				&& getMinFrequencyDifference(f) >= MIN_FREQUENCY_DIFFERENCE
				){

			System.out.println("MIN_SCORE: " + getMinScore(f) + " : " + MINIMUM_SCORE);
			System.out.println("MAXIMAL_DISTANCE: " + getMaxDist(f) + " : " + MAXIMAL_DISTANCE);
			System.out.println("MIN_PATTERN_PERCENTAGE: " + getMinPatternsPercentage(f) + " : " + MIN_PATTERN_PERCENTAGE);
			System.out.println("MIN_FREQUENCY_DIFFERENCE: " + getMIN_FREQUENCY_DIFFERENCE() + " : " + MIN_FREQUENCY_DIFFERENCE);
			return true;
		}
		else
			return false;

	}


	private Double getMinFrequencyDifference(File f) {
		String[] s_temp = f.getName().split("-Df");
		Double result = null;

		if(s_temp.length > 1)
			result = Double.parseDouble(s_temp[1].split("-C")[0]);

		return result;
	}

	private static String getDataSet(File f){

		return f.getName().split("-S")[0];

	}

	private static Double getMinPatternsPercentage(File f){

		Double result = null;

		String[] t = f.getName().split("-P");

		if(t.length > 1)
			result =  Double.parseDouble(t[1].split("-Df")[0]);

		return result;
	}

	private static Double getMaxDist(File f){

		Double result = null;

		String[] t = f.getName().split("-Ds");

		if(t.length > 1)
			result =  Double.parseDouble(t[1].split("-P")[0]);

		return result;
	}

	private static Double getMinScore(File f){

		Double result = null;

		String[] t = f.getName().split("-S");

		if(t.length > 1)
			result =  Double.parseDouble(t[1].split("-Ds")[0]);

		return result;
	}

	public void filterPatternLessFrequentInTheirOwnClass(){

		System.out.println("Filter pattern which does not appear the most frequently in their class");

		/* For each class */
		for(int i = 0 ; i < l_cc.size() ; ++i){

			System.out.println("\n"+l_cc.get(i).className);

			/* For each pattern in this class */
			for(int j = l_cc.get(i).getL_p().size() - 1 ; j >= 0 ; --j){

				FrequencedPattern fp = l_cc.get(i).getL_p().get(j);

				/* Frequency of the pattern in the class in which it has been found */
				double foundClassFrequency = fp.getFrequenceByClass().get(l_cc.get(i).className);

				double maxFreqInOtherClasses = 0.0;

				for(Map.Entry<String, Double> freq: fp.getFrequenceByClass().entrySet()){
					if(freq.getValue() > maxFreqInOtherClasses && !freq.getKey().equals(l_cc.get(i).className))
						maxFreqInOtherClasses = freq.getValue();
				}



				if( foundClassFrequency < maxFreqInOtherClasses){
					l_cc.get(i).getL_p().remove(j);
					System.out.print(".");
				}
				else
					System.out.println("\n\t" + fp.getFrequenceByClass());
			}

		}
	}

	public String characToString(){

		return  "-S"  + ClassRepresentativePatternExtractor.doubleToString(MINIMUM_SCORE, 4)
		+ "-Ds" + ClassRepresentativePatternExtractor.doubleToString(MAXIMAL_DISTANCE, 4)
		+ "-P"  + ClassRepresentativePatternExtractor.doubleToString(MIN_PATTERN_PERCENTAGE, 2)
		+ "-Df" + ClassRepresentativePatternExtractor.doubleToString(MIN_FREQUENCY_DIFFERENCE, 2)
		+ "-C" + nbOfAnnotationColumns;
	}

	public List<AAClass> getL_cc() {
		return l_cc;
	}

	@XmlElementWrapper
	@XmlElement (name = "AAClass")
	public void setL_cc(List<AAClass> l_cc) {
		this.l_cc = l_cc;
	}

	public double getMINIMUM_SCORE() {
		return MINIMUM_SCORE;
	}

	@XmlAttribute
	public void setMINIMUM_SCORE(double mINIMUM_SCORE) {
		MINIMUM_SCORE = mINIMUM_SCORE;
	}

	public double getMIN_PATTERN_PERCENTAGE() {
		return MIN_PATTERN_PERCENTAGE;
	}

	@XmlAttribute
	public void setMIN_PATTERN_PERCENTAGE(double mIN_PATTERN_PERCENTAGE) {
		MIN_PATTERN_PERCENTAGE = mIN_PATTERN_PERCENTAGE;
	}

	public double getMIN_FREQUENCY_DIFFERENCE() {
		return MIN_FREQUENCY_DIFFERENCE;
	}

	@XmlAttribute
	public void setMIN_FREQUENCY_DIFFERENCE(double mIN_FREQUENCY_DIFFERENCE) {
		MIN_FREQUENCY_DIFFERENCE = mIN_FREQUENCY_DIFFERENCE;
	}

	public double getMAXIMAL_DISTANCE() {
		return MAXIMAL_DISTANCE;
	}

	@XmlAttribute
	public void setMAXIMAL_DISTANCE(double mAXIMAL_DISTANCE) {
		MAXIMAL_DISTANCE = mAXIMAL_DISTANCE;
	}

	public int getNbOfAnnotationColumns() {
		return nbOfAnnotationColumns;
	}

	@XmlAttribute
	public void setNbOfAnnotationColumns(int nbOfAnnotationColumns) {
		this.nbOfAnnotationColumns = nbOfAnnotationColumns;
	}

	public String getData_folder() {
		return data_folder;
	}

	@XmlAttribute
	public void setData_folder(String data_folder) {
		this.data_folder = data_folder;
	}

	public String getCorpus_name() {
		return corpus_name;
	}

	@XmlAttribute
	public void setCorpus_name(String corpus_name) {
		this.corpus_name = corpus_name;
	}

	public String getSaveFolder() {
		return saveFolder;
	}

	@XmlAttribute
	public void setSaveFolder(String saveFolder) {
		this.saveFolder = saveFolder;
	}

	public String getCharacteristics() {
		return characteristics;
	}

	@XmlAttribute
	public void setCharacteristics(String characteristics) {
		this.characteristics = characteristics;
		saveFileName =  saveFolder + corpus_name + characteristics+ ".ser";
	}

	public String getSaveFileName() {
		return saveFileName;
	}

	@XmlAttribute
	public void setSaveFileName(String saveFileName) {
		this.saveFileName = saveFileName;
	}

	@XmlAttribute
	public String getSaveDropboxFileName() {
		return saveDropboxFileName;
	}

	public void setSaveDropboxFileName(String saveDropboxFileName) {
		this.saveDropboxFileName = saveDropboxFileName;
	}

	@XmlAttribute
	public String getTempSaveFolder() {
		return tempSaveFolder;
	}

	public void setTempSaveFolder(String tempSaveFolder) {
		this.tempSaveFolder = tempSaveFolder;
	}

	public int getCurrentIteration() {
		return currentIteration;
	}

	@XmlAttribute
	public void setCurrentIteration(int ration) {
		this.currentIteration = currentIteration;
	}


}
