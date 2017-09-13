package main;

import java.util.ArrayList;

// TODO Create a default PositiveScoreTable with 1a similarity of 10 between identical annotations and 0 otherwise

import model.Corpus;
import View.StandardView;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;

public class MainDogma {

	public static void run(){

		StandardView sv = StandardView.getInstance();
		Corpus.getCorpus().addObserver(sv);
		SABRE.getInstance().addObserver(sv);

		PositiveScoreTable st;

		String fo_path = "data/corpus-dogma";						
		st = new PositiveScoreTable(fo_path + "/couples_scores_positifs.csv");


		double desynch = 100;
		int desired_number_of_alignments = 25;

		double gap = 2*desynch;

		SABRE.getInstance().setParam(new SABREParameter(gap, desynch, desired_number_of_alignments)); 
		Corpus.getCorpus().setAnnotationSimilarities(st);

		ArrayList<Integer> al_comment = new ArrayList<Integer>();
		ArrayList<Integer> al_annot = new ArrayList<Integer>();

		al_comment.add(0);
		al_comment.add(2);

		al_annot.add(1);

		try {
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
}
