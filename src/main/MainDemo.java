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

import javax.naming.ldap.StartTlsRequest;

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
import model.NumericalColumn;

public class MainDemo {
	
	public AAColumnFormat aacf;
	
	static double scoreMin = 4;
	
	static String fo_path = "data/demo/";
	static String csv_path = fo_path + "csv/";

	static double desynch = 0.5;
	static double gap = 2*desynch;
	
	static ArrayList<Integer> al_comment = new ArrayList<Integer>();
	static ArrayList<Integer> al_annot = new ArrayList<Integer>();
	static ArrayList<Integer> al_numeric = new ArrayList<Integer>();
	static ArrayList<String> folders = new ArrayList<String>();

	private static int desired_number_of_alignments = 5;

	public static void main(String[] args){

		AnnotationColumn.pst = new PositiveScoreTable(fo_path + "/scores_demo.csv");
		StandardView sv = StandardView.getInstance();
		Corpus.getCorpus().addObserver(sv);
		SABRE.getInstance().addObserver(sv);
		SABRE.getInstance().setParam(new SABREParameter(gap, desynch)); 

			
		try {
			
			al_comment.add(0);
			
			for(int i = 1 ; i < 3 ; ++i)
				al_annot.add(i);
			
			al_numeric.add(3);
			
			Corpus.getCorpus().setDesiredNumberOfAlignments(desired_number_of_alignments);
			Corpus.getCorpus().setColumnFormat(al_comment, al_annot, al_numeric);
			Corpus.getCorpus().setMaxDistance(1.0);
			
			
			Corpus.getCorpus().add(csv_path , true);
			
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e1) {e1.printStackTrace();}
		catch (UndefinedColumnFormatException e) {e.printStackTrace();} 
		
//		sv.process_extraction();
	}
	
	
	
}
