package main.machine_learning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import View.StandardView;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.SABRE;
import extraction.SABREParameter;
import model.Corpus;

public class MainTitouan {

	public static void runInViesa(int set){

		StandardView sv = StandardView.getInstance();
		Corpus.getCorpus().addObserver(sv);
		SABRE.getInstance().addObserver(sv);

//		PositiveScoreTable st;

		String ldaSet = "f" + set;
		String fo_path = "data/Dialogues_Titouan/csv/ldaset_" + ldaSet + "_5col_train_mlp_mallet";	

		//f3 et f4 pas mal de 5 / 5

		double maxDistance = 0.001;
		double desynch = 0.001;
		int desired_number_of_alignments = 25;

		double gap = 2*desynch;

		/* TODO Useless but does not open the window otherwise... */
//		String fo_path_temp = "data/Dialogues_parents_enfants/";						
//		st = new PositiveScoreTable(fo_path_temp + "couples_scores_positifs.csv");

		SABRE.getInstance().setParam(new SABREParameter(gap, desynch, desired_number_of_alignments)); 

		ArrayList<Integer> al_comment = new ArrayList<Integer>();
		ArrayList<Integer> al_numerical = new ArrayList<Integer>();

		al_numerical.add(0);
		al_numerical.add(1);
		al_numerical.add(2);
		al_numerical.add(3);
		al_numerical.add(4);

		try {
			Corpus.getCorpus().setColumnFormat(al_comment, null, al_numerical);
			Corpus.getCorpus().setMaxDistance(maxDistance);
			Corpus.getCorpus().add(fo_path, false);
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
			e.printStackTrace();
		} catch (UndefinedColumnFormatException e) {
			e.printStackTrace();
		}

		//			sv.process_extraction();
		//			sv.process_extraction_and_clustering();

	}





	public static void main(String[] args){

		int nbOfAnnotationColumn = 5;

		double maxDist = 0.01;
		double minScore = 2 * maxDist;
		double minFrequencyDifference = 0.20;
		double minPatternPercentage = 0.8;
		int firstAnnotationColumn = 0;
		int nbOfPatternsByClass;

		String corpusName = "Dialogues_Titouan";
		String csvFolder = "data/" + corpusName + "/csv/";
		String ldaSet = "f2";
		String trainFolderName = "ldaset_"  + ldaSet + "_" + nbOfAnnotationColumn + "col_train_mlp_mallet";
		String devFolderName = "ldaset_"  + ldaSet + "_" + nbOfAnnotationColumn + "col_dev_mlp_mallet";
		String testFolderName = "ldaset_"  + ldaSet + "_" + nbOfAnnotationColumn + "col_test_mlp_mallet";

		ClassRepresentativePatternExtractor crpe = ClassRepresentativePatternExtractor.extractRepresentativePatterns(
				csvFolder, trainFolderName, nbOfAnnotationColumn, minScore, maxDist, minFrequencyDifference, minPatternPercentage, firstAnnotationColumn);

		crpe.initializeParameters();

		ClassRepresentativePatternExtractor crpeDev = new ClassRepresentativePatternExtractor(csvFolder + devFolderName, nbOfAnnotationColumn, firstAnnotationColumn);
		ClassRepresentativePatternExtractor crpeTest = new ClassRepresentativePatternExtractor(csvFolder + testFolderName, nbOfAnnotationColumn, firstAnnotationColumn);

		for(nbOfPatternsByClass = 60 ; nbOfPatternsByClass >= 10 ; nbOfPatternsByClass -= 10)
		{

			// TODO adapter les parametres d'extraction pour obtenir les motifs les plus frequents par classe

			int tempnbOfPatterns = reduceNumberPatternsByClass(crpe, nbOfPatternsByClass);

			if(tempnbOfPatterns == nbOfPatternsByClass){

				//			String outputFile = 
				String outputFolder = "./results/dialogue_classification/patternVectorFiles/";
				String subFolder = outputFolder + "/" + corpusName + "__nbOfPatternByClass_" + tempnbOfPatterns + "__maxDistance_" + maxDist + "__minFrequencyDifference_" + minFrequencyDifference + "__minPatternPercentage_" + minPatternPercentage;

				File fSubFolder = new File(subFolder);
				if(!fSubFolder.exists())
					fSubFolder.mkdir();


				createOutputForEachFile(crpe, crpe, minScore, subFolder + "/output_train");
				//			createOutputForEachFile(crpe, crpe, minScore, outputFolder + "train__" + outputFile);


				//		crpe.classifyTrainDialogues();



				createOutputForEachFile(crpe, crpeDev, minScore, subFolder + "/output_dev");

				//		crpe.classifyCorpus(crpeDev.getL_cc());



				createOutputForEachFile(crpe, crpeTest, minScore, subFolder + "/output_test");
			}
		}

		//		MainTitouan.runInViesa();
	}





	/**
	 * Create an output file in which each line corresponds to one document in crpe2
	 * and each column corresponds to a pattern in crpe
	 * An additional last column corresponds to the class of the document
	 * 
	 * @param crpe
	 * @param crpe2
	 */
	private static void createOutputForEachFile(ClassRepresentativePatternExtractor crpe,
			ClassRepresentativePatternExtractor crpe2, double minScore, String outputPath) {

		try{
			FileWriter fw = new FileWriter(outputPath, false);
			BufferedWriter output = new BufferedWriter(fw);
			DecimalFormat df = new DecimalFormat("#0.00");


			/* For each aa in crpe2 */
			for(AAClass cClass2: crpe2.getL_cc()){

				for(ClassifiedAA caa2: cClass2.getL_aa()){

					/* For each pattern in crpe1 */
					for(int id1 = 0 ; id1 < crpe.getL_cc().size() ; id1++){

						AAClass cClass1 = crpe.getL_cc().get(id1);

						for(int i = 0 ; i < cClass1.getL_p().size() ; ++i){

							output.write(df.format(cClass1.getL_p().get(i).percentageContainedInCAA(caa2, minScore)));

							if(i < cClass1.getL_p().size() - 1 || id1 < crpe.getL_cc().size() - 1)
								output.write(" ");
						}

					}

					output.write("\t" + caa2.aa_class + "\n");
				}

			}

			output.flush();
			output.close();
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}

	}





	private static int reduceNumberPatternsByClass(ClassRepresentativePatternExtractor crpe, int nbOfPatternsByClass) {

		for(AAClass cClass: crpe.getL_cc())
			if(cClass.getL_p().size() < nbOfPatternsByClass)
				nbOfPatternsByClass = cClass.getL_p().size();

		/* Only keep that number of pattern for each class */
		for(AAClass cClass: crpe.getL_cc()){

			if(cClass.getL_p().size() > nbOfPatternsByClass){

				TreeSet<FrequencedPattern> orderedPatterns = new TreeSet<>(new Comparator<FrequencedPattern>(){

					@Override
					public int compare(FrequencedPattern arg0, FrequencedPattern arg1) {

						double d0 = arg0.getFrequencyDifferenceBetweenTwoMostFrequentClass();
						double d1 = arg1.getFrequencyDifferenceBetweenTwoMostFrequentClass();

						int result = new Double(100*(d1 - d0)).intValue(); 
						if(result == 0)
							result = 1;

						return result;
					}

				});

				for(FrequencedPattern fp: cClass.getL_p()){
					orderedPatterns.add(fp);
					//					System.out.println("Add to tree pattern with diff: " + fp.getFrequencyDifferenceBetweenTwoMostFrequentClass());
				}

				cClass.getL_p().clear();


				Iterator<FrequencedPattern> it = orderedPatterns.iterator();

				for(int i = 0 ; i < nbOfPatternsByClass ; i++){

					FrequencedPattern current = it.next();
					cClass.getL_p().add(current);

					//					System.out.println("Add pattern with diff frequency: " + current.greatestDifferenceInFrequency());

				}

				System.out.println("Patterns in class: " + cClass.getL_p().size());


			}

		}

		return nbOfPatternsByClass;

	}
}
