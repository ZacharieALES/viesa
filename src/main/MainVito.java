package main;

//TODO permettre à un utilisateur de filtrer les motifs selon certaines contraintes :
// - motifs contenant des annotations dans telle colonne
// - motifs contenant des valeurs différentes dans telle colonne

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import View.StandardView;
import au.com.bytecode.opencsv.CSVReader;
import exception.AbstractException;
import exception.CSVSeparatorNotFoundException;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.InvalidCSVFileNumberOfColumnsException;
import exception.InvalidInputFileException;
import exception.InvalidNumberOfColumnsInInputFilesException;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;
import model.AAColumnFormat;
import model.AnnotationColumn;
import model.Corpus;

public class MainVito {
	
	public AAColumnFormat aacf;
	
	double scoreMin = 200;
//	double min_seed = 6;
	
	String fo_path = "data/Experience_Vito/";
	String csv_path = fo_path + "csv/";
	String original_csv_path = csv_path + "original_files/";

	double desynch = 20;
	double gap = 2*desynch;
	
	ArrayList<Integer> al_comment = new ArrayList<Integer>();
	ArrayList<Integer> al_annot = new ArrayList<Integer>();
	ArrayList<String> folders = new ArrayList<String>();

	private int desired_number_of_alignments = 25;

	public static HashMap<String, Integer> numberOfRecognizedDialogues = new HashMap<String, Integer>();
	
	public MainVito(){
		
		boolean runInViesa = true;
		boolean convertInputFile = false;
		boolean convertInputFileV2 = false;
		boolean countSuccessiveNumberOfIdenticalAnnotations = false;
		boolean removeSuccessiveAnnotations = false;

		if(runInViesa)
			runInViesa();
		
		if(convertInputFile)
			convertInputFile(360050, 800, 6);
		
		if(convertInputFileV2){
//			convertInputFileV2("all_participants.csv", 360050, 6);
			convertInputFileV2("novice_all.csv", 277010, 6);
		}
		
		if(countSuccessiveNumberOfIdenticalAnnotations)
			countSuccessiveNumberOfIdenticalAnnotations();
		
		if(removeSuccessiveAnnotations){
			ArrayList<Integer> columns = new ArrayList<>();
			
			int maximalNumberOfConsecutiveAnnotations = 10;
			
			for(int i = 0 ; i < 37 ; ++i)
				columns.add(i);
			convertFileWithMaximalNumberOfIdenticalConsecutiveAnnotations(fo_path + "csv/all_participants_aa.csv", fo_path + "all_participants_aa_filtered.csv", maximalNumberOfConsecutiveAnnotations, columns);
			
			columns = new ArrayList<>();
			for(int i = 0 ; i < 7 ; ++i)
				columns.add(i);
			
			for(int i = 1 ; i <= 6 ; ++i)
				convertFileWithMaximalNumberOfIdenticalConsecutiveAnnotations(fo_path + "csv/participant_" + i + "_aa.csv",fo_path + "participant_" + i + "_aa_filtered.csv", maximalNumberOfConsecutiveAnnotations, columns);
		}

		
	}
	
	/** Each line corresponds to a given time */
	public void convertInputFile(int maximalTimeInMs, int timeOfOneLineInMs, int nbOfParticipants){

		ArrayList<Integer> columns = new ArrayList<Integer>();	
		int numberOfColumnsByParticipant = 6;
		
		columns.add(0); // label
		columns.add(1); // FUN
		columns.add(2); // Tier
		columns.add(10); // Participant
		columns.add(12); // Onset ms
		columns.add(14); // Duration ms
		
		ArrayList<String> al_tier = new ArrayList<String>();
		al_tier.add("location");
		al_tier.add("gaze");
		al_tier.add("head_face");
		al_tier.add("upper_body");
		al_tier.add("lower_body");
		al_tier.add("game_action");
		
		ArrayList<String> al_participant = new ArrayList<String>();
		al_participant.add("p1");
		al_participant.add("p2");
		al_participant.add("p3");
		al_participant.add("p4");
		al_participant.add("p5");
		al_participant.add("p6");

		ArrayList<String> al_fun = new ArrayList<String>();
		al_fun.add("no");
		al_fun.add("context_focused_hf");
		al_fun.add("context_focused_ub");
		al_fun.add("context-focused_lb");
		al_fun.add("context_focused_lb");
		al_fun.add("self_focused_hf");
		al_fun.add("self_focused_ub");
		al_fun.add("self-focused_lb");
		al_fun.add("self_focused_lb");
		
		
		try {
			ArrayList<ArrayList<String>> al = getInputFileInArrayList(original_csv_path + "all_participants.csv", columns, true);
			
			 int lineNbIn = al.size();
			 
			 int lineNbOut = (int)Math.ceil(maximalTimeInMs / timeOfOneLineInMs) + 1;
			 int colNbOut = numberOfColumnsByParticipant * nbOfParticipants  + 1;
			 
			 String[][] data_aa = new String[lineNbOut][colNbOut];
			
			 int time = 0;
			 for(int i = 0 ; i < lineNbOut ; ++i)
				 
				 for(int j = 0 ; j < colNbOut ; ++j)
					 if(j == 0){
						 data_aa[i][j] = time + "ms";
						 time += timeOfOneLineInMs;
					 }
					 else
						 data_aa[i][j] = "-";
			 
			 /* For each line of the input file */ 
			 for(int li = 0 ; li < lineNbIn ; li++){
				 
				 int tierId = al_tier.indexOf(al.get(li).get(2));
				 int participantId = al_participant.indexOf(al.get(li).get(3));
				 int funId = al_fun.indexOf(al.get(li).get(1));
				 
				 if(tierId == -1 || participantId == -1 || funId == -1)
					 System.err.println("Tier (" + tierId + ", " + al.get(li).get(2) + "), participant (" + participantId + ", " + al.get(li).get(3) + ") or fun (" + funId + ", " + al.get(li).get(1) + ") not found line " + li + " : " + al.get(li) + ")");
				 
				 /* If the tire and participant are valid */
				 else{
					 
					 /* Compute the id of the column in which the annotations must be added for this input line */
					 int outputCol = participantId * numberOfColumnsByParticipant + tierId + 1;
					 
					 int startLine = (int)Math.round(Double.parseDouble(al.get(li).get(4))/timeOfOneLineInMs);
					 String funSuffix = "";
					 
					 switch(funId){
					 	case 1: funSuffix = " (context)";break; 
					 	case 2: funSuffix = " (context)";break; 
					 	case 3: funSuffix = " (context)";break; 
					 	case 4: funSuffix = " (context)";break; 
					 	case 5: funSuffix = " (self)";break; 
					 	case 6: funSuffix = " (self)";break; 
					 	case 7: funSuffix = " (self)";break; 
					 }
					 
					 /* Each annotation must appear on at least one line (thus, the max)
					  * The number of line in the output file is obtained by dividing the duration by the time of one line
					  */
					 int durationInLines =  (int)Math.max(1, Math.round(Double.parseDouble(al.get(li).get(5))/timeOfOneLineInMs));
					 
//					 System.out.println("start: " + startLine + " duration: " + durationInLines);
//					 System.out.println("start: " + al.get(li).get(4) + " duration: " + al.get(li).get(5));
					 
					 for(int i = 0 ; i < durationInLines ; ++i){
						 
						 int outputLine = startLine + i;	
						 
						 String annotationToAdd = al.get(li).get(0) + funSuffix;
						 
						 String d_currentLine = data_aa[outputLine][outputCol];
						 String d_previousLine = null;
						 String d_nextLine = null;

						 String d_previousLine2 = null;
						 String d_nextLine2 = null;
						 
						 if(outputLine > 0){
							 d_previousLine = data_aa[outputLine-1][outputCol];
							
							 if(outputLine > 1)
								 d_previousLine2 = data_aa[outputLine-2][outputCol];
						 }
						 
						 
						 if(outputLine < lineNbOut - 1){
							 d_nextLine = data_aa[outputLine+1][outputCol];
							
							 if(outputLine < lineNbOut - 2)
								 d_nextLine2 = data_aa[outputLine+2][outputCol];
						 }
						 
						 /* If the cell [outputLine][outputCol] is empty 
						  * or if the cell is already equal to the annotation to add
						  * or if the current value of the cell appear on the previous or next line (i.e., if adding the annotation won't totally remove one of the gesture listed in the input file) 
						  */
						 if("-".equals(d_currentLine) 
								 || annotationToAdd.equals(d_currentLine)
								 || (d_previousLine != null	&& d_currentLine.equals(d_previousLine))
								 || (d_nextLine != null && d_currentLine.equals(d_nextLine)))
							 
							 data_aa[outputLine][outputCol] = annotationToAdd;
							 
						 else
							 /* If in this column the previous line is a "-"
							  * or if the previous annotation is already equal to the annotation to add
							  * or if the two previous annotations are the same */
							 if( d_previousLine != null && "-".equals(d_previousLine)
									 || annotationToAdd.equals(d_previousLine)
									 || (d_previousLine2 != null && d_previousLine.equals(d_previousLine2)))
								 
								 	/* Add the annotation in the previous line */
									 data_aa[outputLine-1][outputCol] = annotationToAdd;

						 	/* If in this column the next line is a "-"
							  * or if the next annotation is already equal to the annotation to add
							  * or if the two next annotations are the same */
							 else if (d_nextLine != null && "-".equals(data_aa[outputLine+1][outputCol])
									 || annotationToAdd.equals(d_nextLine)
									 || (d_nextLine2 != null && d_nextLine.equals(d_nextLine2)))
								 
								 /* Add the annotation in the following line */
								 data_aa[outputLine+1][outputCol] = annotationToAdd;
						 
						 	 else							 
								 System.err.println("Warning: unable to add \"" + al.get(li).get(0) + " : " + al.get(li).get(1) + "\" at " + data_aa[outputLine][0] + " in output line number " + outputLine + " from input line number " + (li+2) + " for participant " + (participantId + 1)); 
						 
					 }
					 
				 }
				 
			 }
			 
		
			 
			 String file = csv_path + "all_participants_aa.csv";
			 
			 try{
			     FileWriter fw = new FileWriter(file, false);
			     BufferedWriter output = new BufferedWriter(fw);
   
				 for(int i = 0 ; i < data_aa.length ; ++i){
					 
					 String s = "";
					 for(int j = 0 ; j < data_aa[0].length ; ++j){
//						 System.out.print(data_aa[i][j] + "\t");
						 s += data_aa[i][j] + ";";
					 }
					 
					 
					 output.write(s + "\n");
					 output.flush();
//					 System.out.println();
					 
				 }
			     output.close();
			     
			     for(int p = 0 ; p < nbOfParticipants ; ++p){

				     fw = new FileWriter(csv_path + "participant_" + (p+1) + "_aa.csv", false);
				     output = new BufferedWriter(fw);
	   
					 for(int i = 0 ; i < data_aa.length ; ++i){

						 String s = data_aa[i][0] + ";";
						 for(int j = p * numberOfColumnsByParticipant + 1 ; j < (p+1) * numberOfColumnsByParticipant + 1 ; ++j){
							 s += data_aa[i][j] + ";";
						 }
						 
						 
						 output.write(s + "\n");
						 output.flush();
						 
					 }
				     output.close();
			     }
			 }
			 catch(IOException ioe){
			     System.out.print("Erreur : ");
			     ioe.printStackTrace();
			 }

			
//			for(ArrayList<String> al_s : al){
//				String s = "";
//				
//				for(String s2 : al_s)
//					s += s2 + "\t";
//				
//				System.out.println(s);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Each line corresponds to a new event */
	public void convertInputFileV2(String filename, int maximalTimeInMs, int nbOfParticipants){

		ArrayList<Integer> columns = new ArrayList<Integer>();	
		int numberOfColumnsByParticipant = 6;
		
		columns.add(0); // label
		columns.add(1); // FUN
		columns.add(2); // Tier
		columns.add(10); // Participant
		columns.add(12); // Onset ms
		columns.add(14); // Duration ms
		
		ArrayList<String> al_tier = new ArrayList<String>();
		al_tier.add("location");
		al_tier.add("gaze");
		al_tier.add("head_face");
		al_tier.add("upper_body");
		al_tier.add("lower_body");	
		al_tier.add("game_action");
		
		ArrayList<String> al_participant = new ArrayList<String>();
		al_participant.add("p1");
		al_participant.add("p2");
		al_participant.add("p3");
		al_participant.add("p4");
		al_participant.add("p5");
		al_participant.add("p6");

		ArrayList<String> al_fun = new ArrayList<String>();
		al_fun.add("no");
		al_fun.add("context_focused_hf");
		al_fun.add("context_focused_ub");
		al_fun.add("context-focused_lb");
		al_fun.add("context_focused_lb");
		al_fun.add("self_focused_hf");
		al_fun.add("self_focused_ub");
		al_fun.add("self-focused_lb");
		al_fun.add("self_focused_lb");
		al_fun.add("communication_focused_hf");
		al_fun.add("communication_focused_ub");
		
		
		try {
			List<ArrayList<String>> al = getInputFileInArrayList(original_csv_path + filename, columns, true);
			
			final int timeColumn = 4;
			
			Collections.sort(al, new Comparator<ArrayList<String>>() {

				@Override
				public int compare(ArrayList<String> o1, ArrayList<String> o2) {
					
					if(o1.size() <= 4 || o2.size() <= 4){
						System.err.println("Error: one of the lines contains less that 5 columns.");
						return 0;
					}
					else{
						double d1 = Double.parseDouble(o1.get(timeColumn));
						double d2 = Double.parseDouble(o2.get(timeColumn));
						
						if(d1 < d2)
							return -1;
						else
							return 1;
					}
				}
			});
			
			int simultaneousEvents = 0;
			
			for(int i = 1 ; i < al.size() ; ++i){
				ArrayList<String> li = al.get(i);
				ArrayList<String> lim = al.get(i-1);
				
				if(Double.parseDouble(li.get(timeColumn)) == Double.parseDouble(lim.get(timeColumn)))
					simultaneousEvents++;
				
			}
			
			 int lineNbIn = al.size();
			 int lineNbOut = lineNbIn - simultaneousEvents;
			 int colNbOut = numberOfColumnsByParticipant * nbOfParticipants  + 1;
			 
			 String[][] data_aa = new String[lineNbOut][colNbOut];
			 
			 int currentOutputLine = -1;
			
			 /* For each output line */
			 for(int i = 0 ; i < lineNbIn ; ++i){
				 
				 /* Is the time in the input file line different from the one in the previous line */
				 boolean isSameTime = false;
				 
				 if(i > 0)
					 isSameTime = al.get(i).get(timeColumn).equals(al.get(i-1).get(timeColumn));
				 
				 if(!isSameTime){

					 currentOutputLine++;
					 
					 /* For each output column */
					 for(int j = 0 ; j < colNbOut ; ++j)
						 if(j == 0)
							 data_aa[currentOutputLine][j] = al.get(i).get(timeColumn);
						 else
							 data_aa[currentOutputLine][j] = "-";
				 }
				 
			 }
			 
			 /* For each line of the input file */ 
			 for(int li = 0 ; li < lineNbIn ; li++){
				 
				 int tierId = al_tier.indexOf(al.get(li).get(2));
				 int participantId = al_participant.indexOf(al.get(li).get(3));
				 int funId = al_fun.indexOf(al.get(li).get(1));
				 
				 if(tierId == -1 || participantId == -1 || funId == -1)
					 System.err.println("Tier (" + tierId + ", " + al.get(li).get(2) + "), participant (" + participantId + ", " + al.get(li).get(3) + ") or fun (" + funId + ", " + al.get(li).get(1) + ") not found line " + li + " : " + al.get(li) + ")");
				 
				 /* If the tire and participant are valid */
				 else{
					 
					 /* Compute the id of the column in which the annotations must be added for this input line */
					 int outputCol = participantId * numberOfColumnsByParticipant + tierId + 1;

					 double startTime = Double.parseDouble(al.get(li).get(timeColumn));
					 double endTime =  startTime + Double.parseDouble(al.get(li).get(5));
					 
					 /* Find the starting line */
					 boolean found = false;
					 int startLine = 0;
					 
					 while(!found && startLine < lineNbOut)
						 if(data_aa[startLine][0].equals(al.get(li).get(timeColumn)))
							 found = true;
						 else
							 startLine++;
					 
					 if(startLine >= lineNbOut){
						 System.err.println("Error: line not found: " + al.get(li).get(timeColumn) );
						 System.exit(0);
					 }
							 
					 String funSuffix = "";
					 
					 switch(funId){
					 	case 1: funSuffix = " (context)";break; 
					 	case 2: funSuffix = " (context)";break; 
					 	case 3: funSuffix = " (context)";break; 
					 	case 4: funSuffix = " (context)";break; 
					 	case 5: funSuffix = " (self)";break; 
					 	case 6: funSuffix = " (self)";break; 
					 	case 7: funSuffix = " (self)";break; 
					 	case 8: funSuffix = " (self)";break; 
					 	case 9: funSuffix = " (communication)";break; 
					 	case 10: funSuffix = " (communication)";break; 
					 }
					 
					 /* Find the ending line */
					 found = false;
					 int endLine = startLine;
					 
					 
					 while(!found && endLine < lineNbOut - 1){

						 if(Double.parseDouble(data_aa[endLine+1][0])  <= endTime)
							 endLine++;
						 else
							 found = true;
					 }
					 
					 
					 for(int i = startLine ; i <= endLine; ++i){
						 
						 String annotationToAdd = al.get(li).get(0) + funSuffix;
						 data_aa[i][outputCol] = annotationToAdd;
							 
					 }
					 
				 }
				 
			 }
			 
			 for(int i = 0 ; i < data_aa.length ; ++i)
				 data_aa[i][0] = Math.round(Double.parseDouble(data_aa[i][0])/100)/10.0 + "s";
			 	 
			 String file = csv_path + "v2_all_participants_aa.csv";
			 
			 try{
			     FileWriter fw = new FileWriter(file, false);
			     BufferedWriter output = new BufferedWriter(fw);
   
				 for(int i = 0 ; i < data_aa.length ; ++i){
					 
					 String s = "";
					 for(int j = 0 ; j < data_aa[0].length ; ++j){
//						 System.out.print(data_aa[i][j] + "\t");
						 s += data_aa[i][j] + ";";
					 }
					 
					 
					 output.write(s + "\n");
					 output.flush();
//					 System.out.println();
					 
				 }
			     output.close();
			     
			     /* Create an output file for each of the participants */
			     for(int p = 0 ; p < nbOfParticipants ; ++p){

				     fw = new FileWriter(csv_path + "v2_participant_" + (p+1) + "_aa.csv", false);
				     output = new BufferedWriter(fw);
	   
				     /* For each line */
					 for(int i = 0 ; i < data_aa.length ; ++i){

						 boolean isDifferentFromPreviousLine = false;
						 
						 String s = data_aa[i][0] + ";";
						 for(int j = p * numberOfColumnsByParticipant + 1 ; j < (p+1) * numberOfColumnsByParticipant + 1 ; ++j){
							 
							 /* If the annotation is different from the one in the previous line */
							 if(!isDifferentFromPreviousLine && i > 0 && !data_aa[i][j].equals(data_aa[i-1][j]))
								 isDifferentFromPreviousLine = true;
							 
							 s += data_aa[i][j] + ";";
						 }
						 
						 if(isDifferentFromPreviousLine || i == 0){
							output.write(s + "\n");
						 	output.flush();
					 	}
						 
					 }
				     output.close();
			     }
			 }
			 catch(IOException ioe){
			     System.out.print("Erreur : ");
			     ioe.printStackTrace();
			 }

			
//			for(ArrayList<String> al_s : al){
//				String s = "";
//				
//				for(String s2 : al_s)
//					s += s2 + "\t";
//				
//				System.out.println(s);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Convert the input file by replacing consecutive identical annotations by a different annotation
	 * If a column of the input file contains more than <numberOfMaximalAnnotations> identical annotations "a", these annotations will be replaced by "*a".
	 * 
  	 * When extracting patterns from annotation arrays, successive identical annotations may lead to huge patterns containing mainly these identical annotations. Thus, it may be useful to change these successive annotations (thus, they won't be considered in the patterns)
	 * @param input_file Path of the input CSV file
	 * @param output_file Path of the output CSV file
	 * @param numberOfMaximalConsecutiveAnnotations Maximal number of identical successive annotations
	 * @param columns List of the columns in the input file to consider (only these columns will appear in the output file)
	 */
	public void convertFileWithMaximalNumberOfIdenticalConsecutiveAnnotations(String input_file, String output_file, int numberOfMaximalConsecutiveAnnotations, ArrayList<Integer> columns){
		
		ArrayList<ArrayList<String>> al = null;
		
		try {
			al = getInputFileInArrayList(input_file, columns, false);
		} catch (AbstractException e) {
			e.printStackTrace();
		}
		
		if(al != null){
			
			/* For each column in al */
			for(int c = 0 ; c < al.get(0).size() ; ++c){
				
				int count = 0;
				String previousAnnotation = "";
				String currentAnnotation = ""; 
				
				/* For each line in al */
				for(int l = 0 ; l < al.size() ; ++l){
					
					currentAnnotation = al.get(l).get(c); 
					
					/* If the annotation is still the same */
					if(currentAnnotation.equals(previousAnnotation))
						count++;
					
					/* If the annotations are not the same */
					else{
						
						/* If there is a previous annotation is not an empty one */
						if(!"".equals(previousAnnotation)){
						
							/* If the previous annotation appears consecutively more than <numberOfMaximalAnnotations> */
							if(count > numberOfMaximalConsecutiveAnnotations && !"-".equals(previousAnnotation))
								
								/* Replace these annotations by "*" + previousAnnotation */
								for(int i = 1 ; i <= count ; ++i){
									
									if(!previousAnnotation.equals(al.get(l-i).get(c)))
										System.err.println("Error replacing annotation " + al.get(l-i) + " by " + previousAnnotation );
									
									al.get(l-i).set(c, "_" + previousAnnotation);
								}
							
						}

						previousAnnotation = currentAnnotation;
						count = 1;
					}
					
				}

				/* If the previous annotation appears consecutively more than <numberOfMaximalAnnotations> */
				if(count > numberOfMaximalConsecutiveAnnotations && !"-".equals(currentAnnotation))
					
					/* Replace these annotations by "*" + previousAnnotation */
					for(int i = 1 ; i <= count ; ++i){

						if(!previousAnnotation.equals(al.get(al.size()-i).get(c)))
							System.err.println("Error replacing annotation " + al.get(al.size()-i) + " by " + currentAnnotation);
						
						al.get(al.size()-i).set(c, "_" + currentAnnotation);
					}
				
			}
			
			     FileWriter fw;
				try {
					 fw = new FileWriter(output_file, false);
				     BufferedWriter output = new BufferedWriter(fw);
	   
				     /* For each line */
					 for(int i = 0 ; i < al.size(); ++i){
						 
						 String s = "";
						 
						 /* For each column */
						 for(int j = 0 ; j < al.get(0).size(); ++j){
	//						 System.out.print(data_aa[i][j] + "\t");
							 s += al.get(i).get(j) + ";";
						 }
						 
						 output.write(s + "\n");
						 output.flush();
	//					 System.out.println();
						 
					 }
				     output.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
		}
		
		else
			System.err.println("The input file (" + input_file + ") seems to be invalid");
	}
	
	public void countSuccessiveNumberOfIdenticalAnnotations(){
		
		ArrayList<Integer> columns = new ArrayList<Integer>();
		
		for(int i = 1 ; i <= 36 ; ++i)
			columns.add(i);

		ArrayList<ArrayList<String>> al = null;
		try {
			al = getInputFileInArrayList(fo_path + "all_participants_aa.csv", columns, true);
		} catch (AbstractException e) {
			e.printStackTrace();
		}
		
		if(al != null){
			
			/* Will contain for each annotation the two highest number of consecutive occurrence */
			HashMap<String, TwoOrderedInteger> hash = new HashMap<String, TwoOrderedInteger>();
			
			/* For each column in al */
			for(int c = 0 ; c < al.get(0).size() ; ++c){
				
				int count = 0;
				String previousAnnotation = "";
				String currentAnnotation = "";
				
				/* For each line in al */
				for(int l = 0 ; l < al.size() ; ++l){
					
					currentAnnotation = al.get(l).get(c); 
					
					/* If the annotation is still the same */
					if(currentAnnotation.equals(previousAnnotation))
						count++;
					
					/* If the annotations are not the same */
					else{
						
						/* If the previous annotation is not an empty one */
						if(!"".equals(previousAnnotation)){
						
							/* Get the current highest number of consecutive occurrences of this annotation */
							TwoOrderedInteger occ = hash.get(previousAnnotation);
							
							if(occ != null)
								occ.add(count);
							else
								hash.put(previousAnnotation, new TwoOrderedInteger(count));
						}

						previousAnnotation = currentAnnotation;
						count = 1;
					}
					
				}
				
				/* Get the current highest number of consecutive occurrences of this annotation */
				TwoOrderedInteger occ = hash.get(currentAnnotation);
				
				/* If we have found a highest number of consecutive occurences for this annotations */
				if(occ != null)
						occ.add(count);
				else
					hash.put(currentAnnotation, new TwoOrderedInteger(count));
				
			}
			
		   Iterator<Entry<String,TwoOrderedInteger>> it = hash.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String,TwoOrderedInteger> pair = (Map.Entry<String,TwoOrderedInteger>)it.next();
		        System.out.println(pair.getKey());
		    }

		    System.out.println("---------");
		    
		   it = hash.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String,TwoOrderedInteger> pair = (Map.Entry<String,TwoOrderedInteger>)it.next();
		        System.out.println( pair.getValue().m1);
		    }
		    
		    System.out.println("---------");
			    
		   it = hash.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String,TwoOrderedInteger> pair = (Map.Entry<String,TwoOrderedInteger>)it.next();
		        System.out.println( pair.getValue().m2);
		        it.remove(); // avoids a ConcurrentModificationException
		    }
			
		}
	}
	
	
	public void runInViesa(){

		StandardView sv = StandardView.getInstance();
		Corpus.getCorpus().addObserver(sv);
		SABRE.getInstance().addObserver(sv);
		SABRE.getInstance().setParam(new SABREParameter(gap, desynch, desired_number_of_alignments)); 
		
		Corpus.getCorpus().computeClosePatterns = true;

			
		try {
			
			for(int i = 1 ; i < 8 ; ++i)
				al_annot.add(i);
			
			al_comment.add(0);
			
			Corpus.getCorpus().setColumnFormat(al_comment, al_annot, null);

			AnnotationColumn.pst = new PositiveScoreTable(this.fo_path + "/vito_couples_scores_positifs.csv");
			
			Corpus.getCorpus().add(csv_path , false);
			
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e1) {e1.printStackTrace();}
		catch (UndefinedColumnFormatException e) {e.printStackTrace();} 
		
//		sv.process_extraction();
	}
	
	
	public ArrayList<ArrayList<String>> getInputFileInArrayList(String s_path, ArrayList<Integer> columnsToConsider, boolean h_header)
			throws CSVSeparatorNotFoundException,
			InvalidNumberOfColumnsInInputFilesException,
			InvalidCSVFileNumberOfColumnsException,
			InvalidInputFileException{

		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
			
		ArrayList<Character> separator = new ArrayList<Character>();
		separator.add(';');
		separator.add(',');
//		separator.add(' ');
		separator.add('\t');
		
		/* Get the file name */
		CSVReader reader = null;
		String[] currentLine = null;

		try {

			boolean separatorFound = false;
			int iSeparator = 0;

			/* While the separator have not been found */
			while (!separatorFound && iSeparator < separator.size()) {

				/* Try the next one */
//				reader = new CSVReader(new FileReader(s_path),
//						separator.get(iSeparator));
				reader = new CSVReader(new InputStreamReader(new FileInputStream(s_path), StandardCharsets.UTF_8),
						separator.get(iSeparator));
				currentLine = reader.readNext();
								
				if(currentLine == null){
					reader.close();
					throw new InvalidInputFileException(s_path);
				}

				if (currentLine.length <= 1) {
					iSeparator++;
//					reader = new CSVReader(new FileReader(s_path),
//							separator.get(iSeparator));
					reader = new CSVReader(new InputStreamReader(new FileInputStream(s_path), StandardCharsets.UTF_8),
							separator.get(iSeparator));
				} else
					separatorFound = true;
			}

			if (separatorFound) {
				
				int maxColumn = 0;
				
				for(Integer i : columnsToConsider)
					if(i > maxColumn)
						maxColumn = i;
					else if(i < 0)
						System.err.println("One the column id in input is negative");
			
				if(h_header)
					currentLine = reader.readNext();

				/* For each line */
				do {
					if(maxColumn > currentLine.length){
						String s = "";
						for(int i = 0 ; i < currentLine.length ; ++i)
							s += currentLine[i] + " ; ";
						System.err.println("One line of file " + s_path + " does not contain the required number of columns (size " + currentLine.length + "instead of " + (maxColumn+1) + " for line: " + s + ")");
					}

					ArrayList<String> new_line = new ArrayList<String>();
					
					for(Integer i : columnsToConsider)
						new_line.add(currentLine[i]);
					
					result.add(new_line);
				} while ((currentLine = reader.readNext()) != null);

			} else {
				System.err.println("No proper separator has been found for the file (file name : "
						+ s_path + ")\n Tested separators : " + separator);
				System.err.println("CSVSeparatorNotFoundException");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;

	}
	
	private class TwoOrderedInteger{
		
		/* m1 > m2 */
		public int m1 = 0;
		public int m2 = -Integer.MAX_VALUE;
		
		public TwoOrderedInteger(int i){
			m1 = i;
		}
		
		public void add(int i){
			
			if(i > m1){
				m2 = m1;
				m1 = i;
			}
			else if(i > m2 && i < m1)
				m2 = i;
			
		}
		
		
		
	}

	
	
}
