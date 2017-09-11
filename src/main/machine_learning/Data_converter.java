package main.machine_learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Data_converter {

	enum Data_Type{
		TRAIN, TEST, DEV
	}

	/** 
	 * Read a file in which each line corresponds to one document in crpe2
	 * and each column to the % of one given pattern in the document
	 * An additional last column corresponds to the class of the document
	 * 
	 * For each line of the input file, a ClassifiedArray is created
	 *  
	 */
	public static List<ClassifiedArray> readPatternFile(String path){

		List<ClassifiedArray> lCA = new ArrayList<>();
		
		try{
			InputStream ips_path=new FileInputStream(path);
			InputStreamReader ipsr_path=new InputStreamReader(ips_path);
			BufferedReader br_path=new BufferedReader(ipsr_path);

			
			/* Current line */
			String s_document = null;

			int nbOfTopics = -1;

			/* Detect the number of topics */
			while(nbOfTopics == -1 && (s_document = br_path.readLine())!=null){

				String[] splitTab = s_document.split("\t");

				if(splitTab.length > 1){

					String[] splitProba = splitTab[0].split(" ");

					if(splitProba.length > 1)
						nbOfTopics = splitProba.length;

				}
			}

			ips_path=new FileInputStream(path);
			ipsr_path=new InputStreamReader(ips_path);
			br_path=new BufferedReader(ipsr_path);

			/* For each line of the document */
			while ((s_document = br_path.readLine())!=null){

				//				System.err.println(". : " + s_document);

				String[] splitTab = s_document.split("\t");

				if(splitTab.length <= 1){
					System.out.println("Skipping the following line which does not contain any tabulation:\n" + s_document);
					continue;
				}

				String[] splitProba = splitTab[0].split(" ");

				/* If the line does not correspond to an utterance */
				if(splitProba.length != nbOfTopics){
					System.out.println("Skipping the following line which contains "+ splitProba.length + " features instead of " + nbOfTopics + ":\n" + s_document);
					continue;
				}

				String documentClass = splitTab[1].trim();
				double[] features = new double[nbOfTopics]; 

				for(int i = 0 ; i < nbOfTopics; i++){
					try{
						features[i] = Double.parseDouble(splitProba[i].replace(",", "."));
					}
					catch(NumberFormatException e){
						System.out.println("Skipping the following line as the feature \"" + splitProba[i] + "\" is not a number:\n" + s_document );
						continue;
					}
				}
				
				lCA.add(new ClassifiedArray(documentClass, features));
			}

			br_path.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
		
		return lCA;
	}


	/** 
	 * The data is contained in one file. 
	 * A line reduced to "#" corresponds to the end of a dialogue.
	 * Each other line is associated to an utterance and are as follows
	 * proba* [0,1]*
	 * 
	 * Note on the proba part:  
	 * - proba: probability that the utterance is in a topic (1st proba <-> 1st topic, 2nd proba <-> 2nd topic, ...)
	 * - the proba are separated by a space
	 * 
	 * Note on the second part (real topic of the dialogue) :
	 * - 0: the dialogue is not in the topic (1st 0 <-> 1st topic, ...)
	 * - 1: the dialogue is in the topic (1st 1 <-> 1st topic, ...)
	 * - the 0/1 are separated by a space
	 * 
	 * General note:
	 * - The first and the second part are separated by a tab
	 *  
	 */
	public static void convertTitouanDataToAnnotatedElements(String dataDocPath, String codingSchemeName, boolean fullCorpus){

		try{
			InputStream ips_path=new FileInputStream(dataDocPath);
			InputStreamReader ipsr_path=new InputStreamReader(ips_path);
			BufferedReader br_path=new BufferedReader(ipsr_path);

			/* Current line */
			String s_document = null;

			int nbOfTopics = -1;
			int nbOfClass = -1;

			/* Detect the number of topics and class */
			while(nbOfTopics == -1 && (s_document = br_path.readLine())!=null){

				String[] splitTab = s_document.split("\t");

				if(splitTab.length > 1){

					String[] splitProba = splitTab[0].split(" ");
					String[] splitClass = splitTab[1].split(" ");

					System.out.println(splitClass[0]);
					if(splitProba.length > 1 && splitClass.length > 1 && !splitClass[0].contains("WTF")){
						nbOfTopics = splitProba.length;
						nbOfClass = splitClass.length;
						System.out.println("Data Titouan: File " + dataDocPath + "\nNumber of topics = " + nbOfTopics);
						System.out.println("Number of classes = " + nbOfClass);
					}

				}
			}

			ips_path=new FileInputStream(dataDocPath);
			ipsr_path=new InputStreamReader(ips_path);
			br_path=new BufferedReader(ipsr_path);


			/* Class of the current document */
			int document_class = -1;

			/* Variable which corresponds to one annotated array */
			String annotated_array = null;

			/* For each line of the document */
			while ((s_document = br_path.readLine())!=null){

				//				System.err.println(". : " + s_document);

				String[] splitTab = s_document.split("\t");

				if(splitTab.length <= 1){

					/* If it corresponds to the end of an array */
					if(annotated_array != null && document_class != -1)
						saveTheAnnotatedArray(document_class, annotated_array, codingSchemeName, fullCorpus);
					annotated_array = null;
					document_class = -1;
					continue;
				}

				String[] splitProba = splitTab[0].split(" ");
				String[] splitClass = splitTab[1].split(" ");

				/* If the line does not correspond to an utterance */
				if(splitProba.length != nbOfTopics 
						|| splitClass.length != nbOfClass){

					/* If it corresponds to the end of an array */
					if(annotated_array != null){

						/* If the class of the document has been identified */
						if(document_class != -1){
							saveTheAnnotatedArray(document_class, annotated_array, codingSchemeName, fullCorpus);
						}
						else
							System.err.println("Undetected class for line: " + s_document);

					}
					else
						System.err.println("Error in file " + dataDocPath + " cannot parse line:\n" + s_document + "\nThe number of topics are not equal to " + nbOfTopics + " or the number of classes are not equal to " + nbOfClass);

					//					System.err.println("Start new dialogue");
					annotated_array = null;
					document_class = -1;

					continue;

				}

				/* If the lines corresponds to a new utterance */
				if(annotated_array == null){
					annotated_array = "";
				}

				/* Get the class of the document according to this line */
				int j = 0;
				int t_documentClass = -1;
				while(t_documentClass == -1 && j < splitClass.length){
					try{
						int id = Integer.parseInt(splitClass[j]);

						if(id == 1)
							t_documentClass = j;
						else
							j++;
					}
					catch(NumberFormatException e){
						System.err.println("Error in file " + dataDocPath + " cannot parse \"" + splitClass[j] + "\" to an int");
						j = splitClass.length;
					}
				}

				/* If the class has been found */
				if(t_documentClass == -1){
					System.err.println("Error in file " + dataDocPath + " cannot find topic on line:\n" + s_document);
					annotated_array = null;
					document_class = -1;
					continue;
				}

				/* If it is the first line of a dialogue */
				if(document_class == -1)
					document_class = t_documentClass;

				/* If it is not the first line and the class does not match the one previously found */
				else if(t_documentClass != document_class){
					System.err.println("Error in file " + dataDocPath + " we find class " + t_documentClass + " but it should be class " + document_class + " on line:\n" + s_document);
					annotated_array = null;
					document_class = -1;
				}

				/* If the class found on this line is coherent add the line to the annotated array */
				annotated_array += splitTab[0].replace(" ", ";") + "\n";
			}

			/* If the last annotated array has not been saved */
			if(annotated_array != null)
				saveTheAnnotatedArray(document_class, annotated_array, codingSchemeName, fullCorpus);


			br_path.close();
		}catch(Exception e){
			e.printStackTrace();
		}		
	}


	/** 
	 * The data are contained in 3 files:
	 * - dataDocPath and dataSentPath both contain the text of all the dialogues (in different formats);
	 * - vectorSentPath contain the numerical annotations.
	 * 
	 * The content of the files is as follows:
	 * - dataDocPath contains 1 doc per line (line pattern: "<class>\t <content_of_the_doc>")
	 * - dataSentPath and vectorSentPath contain 1 sentence per line ("<class>\t <first value>\t <second value> ...")
	 * 
	 * We use "sent" format instead of "doc" format since it contains several lines per document.
	 * 
	 * dataDocPath will help identifying when a file ends (otherwise it would not be possible to identify two consecutive documents of the same class) 
	 */
	public static void convertIrmanDataToAnnotatedElements(Data_Type type, String dataDocPath, String dataSentPath, String vectorSentPath, String codingSchemeName)	
	{

		String s_type="unknown_type";

		switch (type) {
		case TRAIN:
			s_type = "train";
			break;

		case DEV:
			s_type= "dev";
			break;

		case TEST:
			s_type="test";

		}

		try{
			InputStream ips_data_doc_path=new FileInputStream(dataDocPath + s_type + ".txt");
			InputStreamReader ipsr_data_doc_path=new InputStreamReader(ips_data_doc_path);
			BufferedReader br_data_doc_path=new BufferedReader(ipsr_data_doc_path);

			InputStream ips_data_sent_path=new FileInputStream(dataSentPath + s_type + ".txt");
			InputStreamReader ipsr_data_sent_path=new InputStreamReader(ips_data_sent_path);
			BufferedReader br_data_sent_path=new BufferedReader(ipsr_data_sent_path);

			InputStream ips_vector_sent_path=new FileInputStream(vectorSentPath + s_type + ".vectors");
			InputStreamReader ipsr_vector_sent_path=new InputStreamReader(ips_vector_sent_path);
			BufferedReader br_vector_sent_path=new BufferedReader(ipsr_vector_sent_path);

			/* Line obtained from each of the three documents */
			String s_document = null;
			String s_document_line_text = null;
			String s_document_line_annotations = null;

			/* Class of the current document */
			String document_class = null;

			/* Variable which corresponds to one annotated array */
			String annotated_array = null;

			boolean skip_reading_lines = false;

			/* For each line of the "sent" documents */
			while (skip_reading_lines || (s_document_line_text=br_data_sent_path.readLine())!=null 
					&& (s_document_line_annotations=br_vector_sent_path.readLine())!=null){

				if(!skip_reading_lines){

					/* Get the content of the next line of the document */
					s_document_line_text = s_document_line_text.split("\t")[1];

					String[] temp = s_document_line_annotations.split("\t");

					if(temp.length > 1)
						s_document_line_annotations = temp[1];
					else
						s_document_line_annotations = temp[0];
				}

				skip_reading_lines = false;

				/* If we consider a new annotated array */
				boolean isANewAnnotatedArray = annotated_array == null;

				if(isANewAnnotatedArray){
					annotated_array = "";

					/* Get the content of the next document */
					s_document=br_data_doc_path.readLine();

					/* Get the class of the new document */
					String[] temp = s_document.split("\t");
					document_class = temp[0];

					/* Get the text of the whole document */
					s_document = temp[1];
				}


				/* If the new line is in the current document */
				if(s_document.contains(s_document_line_text)){

					/* Get all the annotations in an array */
					String[] as_annotations = s_document_line_annotations.split(" ");

					/* Add the text in the array */
					annotated_array += s_document_line_text.replace(";", ",");

					/* Add the annotations in the array */
					for(String s : as_annotations)
						if(!s.trim().equals(""))
							annotated_array +=";" + s;

					annotated_array+="\n";

				}
				/* If the new line is in the next document */
				else{
					if(isANewAnnotatedArray){
						System.err.println("One line is not contained in two consecutive documents.\n"
								+ "Line: "+ s_document_line_text + "\n"
								+ "Second document: " + s_document);
						System.exit(0);
					}
					/* If the new line is in the next document, don't read the next line, read the next document instead */
					else{

						skip_reading_lines = true;
						saveTheAnnotatedArray(codingSchemeName, document_class, annotated_array, s_type);
						annotated_array = null;
					}
				}


			}
			br_data_sent_path.close();
			br_data_doc_path.close();
			br_vector_sent_path.close();
		}catch(Exception e){
			e.printStackTrace();
		}		
	}




	private static void saveTheAnnotatedArray(int document_class, String annotated_array, String corpusName, boolean fullCorpus) {

		String mainOutputFolderPath = "./data/Dialogues_Titouan/csv/" + corpusName;
		String classOutputFolderPath = mainOutputFolderPath + "/" + document_class;

		if(!fullCorpus)
			classOutputFolderPath = mainOutputFolderPath;

		File mainOutputFolder = new File(mainOutputFolderPath);

		if(!mainOutputFolder.exists()){
			boolean success = mainOutputFolder.mkdirs();

			if(!success){
				System.err.println("Unable to create the main output folder " + mainOutputFolderPath);
				System.exit(0);
			}
		}

		File classOutputFolder = new File(classOutputFolderPath);

		if(!classOutputFolder.exists()){
			boolean success = classOutputFolder.mkdirs();

			if(!success){
				System.err.println("Unable to create the class output folder " + classOutputFolderPath);
				System.exit(0);
			}
		}


		int i = 0;

		try{

			File f = null;

			do{
				i++;

				f = new File(classOutputFolder + "/" + document_class + "_" + i + "_" + corpusName  + ".csv");
			}while(f.exists());

			/* If we convert the full corpus or if we converted less than 5 files of this corpus */
			if(fullCorpus || i < 5){
				FileWriter fw = new FileWriter(f, true);
				BufferedWriter output = new BufferedWriter(fw);

				System.out.println("Created file: " + f.getCanonicalPath());
				output.write(annotated_array);
				output.flush();
				output.close();
			}
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}

	}


	/**
	 * Create a csv file which represents an annotated array
	 * @param coding_scheme Name of the coding scheme used to create the annotations
	 * @param document_class Class of the document
	 * @param annotated_array Content of the created csv file
	 * @param s_type dev, train or test
	 */
	private static void saveTheAnnotatedArray(String coding_scheme, String document_class, String annotated_array, String s_type) {

		String mainOutputFolderPath = "./data_converted_" + coding_scheme;
		String classOutputFolderPath = mainOutputFolderPath + "/" + document_class;

		File mainOutputFolder = new File(mainOutputFolderPath);

		if(!mainOutputFolder.exists()){
			boolean success = mainOutputFolder.mkdirs();

			if(!success){
				System.err.println("Unable to create the main output folder " + mainOutputFolderPath);
				System.exit(0);
			}
		}

		File classOutputFolder = new File(classOutputFolderPath);

		if(!classOutputFolder.exists()){
			boolean success = classOutputFolder.mkdirs();

			if(!success){
				System.err.println("Unable to create the class output folder " + classOutputFolderPath);
				System.exit(0);
			}
		}


		int i = 0;

		try{

			File f = null;

			do{
				i++;
				f = new File(classOutputFolder + "/" + coding_scheme + "_" + s_type  + "_" + document_class + i + ".csv");
			}while(f.exists());


			FileWriter fw = new FileWriter(f, true);
			BufferedWriter output = new BufferedWriter(fw);

			System.out.println("Created file: " + f.getCanonicalPath());
			output.write(annotated_array);
			output.flush();
			output.close();
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}

	}

	public static void main(String[] args){

		//		String coding_scheme_name = "nbow-doc_do0.5";
		//
		//		//		convertIrmanDataToAnnotatedElements(Data_Type.TRAIN, "../../../Test/donnees/r8-nbow/data/doc/", "../../../Test/donnees/r8-nbow/data/sent/", "../../../Test/donnees/r8-nbow/vectors/", coding_scheme_name);
		//		convertIrmanDataToAnnotatedElements(Data_Type.TRAIN, "../../../Dropbox/31_Recherche/Classification_dialogues/data/irman/data/doc/", "../../../Dropbox/31_Recherche/Classification_dialogues/data/irman/data/sent/", "../../../Dropbox/31_Recherche/Classification_dialogues/data/irman/vectors/" + coding_scheme_name + "/", coding_scheme_name);
		boolean fullCorpus = true;

		//		for(int i = 1 ; i <= 10 ; ++i){
		//			String ldaSet = "f" + i;
		String ldaSet = "f2";
		String set = "dev";//"test";//"train";
		convertTitouanDataToAnnotatedElements("data/Dialogues_Titouan/raw/" + ldaSet + "_5_" + set + "MlpMallet.data", "ldaset_" + ldaSet + "_5col_" + set + "_mlp_mallet", fullCorpus);
		//		}

	}
}
