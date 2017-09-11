package main;

import java.util.ArrayList;

import model.AnnotationColumn;
import model.Corpus;
import View.StandardView;
import exception.InvalidAnnotationIndex;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;

public class MainChanoni {
	
	public static void run(){

		StandardView sv = StandardView.getInstance();
		Corpus.getCorpus().addObserver(sv);
		SABRE.getInstance().addObserver(sv);
		
		PositiveScoreTable st;
		
		String fo_path = "data/Dialogues_parents_enfants/";						
		st = new PositiveScoreTable(fo_path + "couples_scores_positifs.csv");
					

			double desynch = 5;
			int desired_number_of_alignments = 25;
			
			double gap = 2*desynch;
			
			
			//TODO Voir pouquoi retirer un des deux setAnnotationSim fait que la GUI ne s'affiche pas...
			SABRE.getInstance().setParam(new SABREParameter(gap, desynch)); 

			ArrayList<Integer> al_comment = new ArrayList<Integer>();
			ArrayList<Integer> al_annot = new ArrayList<Integer>();

			al_comment.add(0);
			al_comment.add(1);
			al_comment.add(2);

			al_annot.add(3);
			al_annot.add(4);
			al_annot.add(5);
			al_annot.add(6);
			
				try {
					Corpus.getCorpus().setDesiredNumberOfAlignments(desired_number_of_alignments);
					Corpus.getCorpus().setColumnFormat(al_comment, al_annot, null);
					Corpus.getCorpus().setAnnotationSimilarities(st);
					Corpus.getCorpus().setMaxDistance(1.0);
					Corpus.getCorpus().add(fo_path + "/csv", false);
				} catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
					e.printStackTrace();
				} catch (UndefinedColumnFormatException e) {
					e.printStackTrace();
				}
			
//			sv.process_extraction();
//			sv.process_extraction_and_clustering();
			
	}
	
	public static void main(String[] args){
		System.out.println("Chanoni");
		MainChanoni.run();
	}
	
}
