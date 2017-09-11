package main;

import java.util.ArrayList;

import View.StandardView;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;
import model.Corpus;

public class MainCogniSismef {

	public static void run(){

		Main.hasHeader = true;
		PositiveScoreTable st;

		Main.scoreFolder = "data/Cogni-CISMEF";
		st = new PositiveScoreTable(Main.scoreFolder + "/" + Main.SCORES_DEFAULT);

		//		String fo_path = "data/Cogni-CISMEF/dialogues/01";		
		String fo_path = Main.scoreFolder + "/csv";		
		//		String fo_path = "data/Cogni-CISMEF/dialogues/test";					

		Corpus.getCorpus().setAnnotationSimilarities(st);



		ArrayList<Integer> al_comment = new ArrayList<Integer>();
		ArrayList<Integer> al_annot = new ArrayList<Integer>();



		//			al_comment.add(0);
		//			al_comment.add(1);
		//			al_comment.add(2);
		//			al_annot.add(3);
		//			al_comment.add(4);
		//			al_comment.add(5);
		//			al_annot.add(6);
		//			al_comment.add(7);
		//			al_comment.add(8);
		//			al_annot.add(9);
		//			al_annot.add(10);
		//			al_comment.add(11);

		al_comment.add(0);
		al_comment.add(1);
		al_annot.add(2);
		al_annot.add(3);
		al_annot.add(4);
		al_annot.add(5);
		//			al_comment.add(6);
		//			al_comment.add(7);
		//			al_comment.add(8);
		//			al_comment.add(9);
		//			al_comment.add(10);
		//			al_comment.add(11);

		try {
			Corpus.getCorpus().initialAnnotationColumns = al_annot;
			Corpus.getCorpus().initialCommentColumns = al_comment;

			Corpus.getCorpus().setColumnFormat(al_comment, al_annot, null);
			Corpus.getCorpus().setAnnotationSimilarities(st);
			Corpus.getCorpus().add(fo_path, Main.hasHeader);


			int latestDesiredNumberOfAlignments = 25;
			double latestGap = 10;

			SABRE.getInstance().setParam(new SABREParameter(latestGap, latestGap/2)); 
			Corpus.getCorpus().setDesiredNumberOfAlignments(latestDesiredNumberOfAlignments);
			
			StandardView sv = StandardView.getInstance();
			Corpus.getCorpus().addObserver(sv);
			SABRE.getInstance().addObserver(sv);

		} catch (UndefinedColumnFormatException e) {e.printStackTrace();} catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
