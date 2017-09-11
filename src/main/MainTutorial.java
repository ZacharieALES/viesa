package main;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import View.StandardView;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;
import model.Corpus;
import tuto.AbstractTutoStep;
import tuto.ChangeDisplayedAlignment;
import tuto.CompleteNonOptimalPatterns;
import tuto.EditDesiredNumberOfAlignmentsStep;
import tuto.EditGap;
import tuto.EditSimilarity;
import tuto.EditSimilarityCombobox;
import tuto.EditSimilarityTable;
import tuto.EvaluateAlignment;
import tuto.FirstExtraction;
import tuto.InitializationStep;
import tuto.OpenEditSimilarityFrame;

public class MainTutorial {



	public static boolean IS_TUTO = false;	
	public static List<AbstractTutoStep> lSteps = new ArrayList<>();
	public static int currentStepId = -1;

	public static void run(){

		MainTutorial.IS_TUTO = true;
		Main.hasHeader = false;

		lSteps.add(new InitializationStep());
		lSteps.add(new FirstExtraction());
		lSteps.add(new ChangeDisplayedAlignment());
		lSteps.add(new EvaluateAlignment());
		lSteps.add(new EditDesiredNumberOfAlignmentsStep());
		lSteps.add(new OpenEditSimilarityFrame());

		EditSimilarityTable est = new EditSimilarityTable(){

			@Override
			public String resultsComment() {
				return "You can observe that annotation 'B' does not appear anymore in the alignments.<br>"
						+ "That is why, setting a similarity to 0 may sometime be a little extreme. <br>"
						+ "An alternative is to decrease sim(B,B) but keep it strictly positive (so that B can still be aligned with itself in the results).";
			}
		};
		
		est.loperations.add(est.new EditOperation(0, "B", "B"));

		lSteps.add(est);

		OpenEditSimilarityFrame oesf = new OpenEditSimilarityFrame(){
			@Override
			public String description() {
				return "Now we will see another way of editing the inter-annotation similarities.";
			};
		};

		lSteps.add(oesf);

		
		EditSimilarityCombobox esc = new EditSimilarityCombobox(){

			@Override
			public String resultsComment() {
				return "As you can observe the alignments are now very big and contain many times annotation \".\".<br>"
						+ "This is due to the fact that this annotation appears a lot in the considered arrays of annotation.<br><br>"
						+ "The obtained alignments are so big that it seems hard to find interesting regularities among them.<br>"
						+ "To avoid this, we will set sim(.,.) back to 0.";
			}
		};

		esc.loperations.add(esc.new EditOperation(5, "A", "B"));
		esc.loperations.add(esc.new EditOperation(1, ".", "."));

		lSteps.add(esc);

		oesf = new OpenEditSimilarityFrame(){
			@Override
			public String description() {
				return "";
			};
		};

		lSteps.add(oesf);
		EditSimilarity es = new EditSimilarity();
		
		es.loperations.add(es.new EditOperation(0, ".", "."));
		es.loperations.add(es.new EditOperation(10, "B", "B"));
		
		lSteps.add(es);
		lSteps.add(new EditGap(10));

//		lSteps.add(new OpenColumnsSelectionFrame());
//		lSteps.add(new EditColumnSelection());
		lSteps.add(new CompleteNonOptimalPatterns());

		PositiveScoreTable st;

		Main.scoreFolder = "data/Tutorial";
		st = new PositiveScoreTable(Main.scoreFolder + "/tuto_scores.csv");

		//		String fo_path = "data/Cogni-CISMEF/dialogues/01";		
		String fo_path = Main.scoreFolder + "/csv";		
		//		String fo_path = "data/Cogni-CISMEF/dialogues/test";					

		Corpus.getCorpus().setAnnotationSimilarities(st);



		ArrayList<Integer> al_comment = new ArrayList<Integer>();
		ArrayList<Integer> al_annot = new ArrayList<Integer>();

		al_comment.add(0);
		al_annot.add(1);
		al_annot.add(2);
		al_annot.add(3);
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


			int desiredNumberOfAlignments = 3;
			double gap = 100;

			SABRE.getInstance().setParam(new SABREParameter(gap, gap/2)); 
			Corpus.getCorpus().setDesiredNumberOfAlignments(desiredNumberOfAlignments);

			StandardView sv = StandardView.getInstance();
			Corpus.getCorpus().addObserver(sv);
			SABRE.getInstance().addObserver(sv);


			MainTutorial.nextStep();
			
			JOptionPane.showMessageDialog(sv, "<html>This tutorial will present the software and its features in several steps.<br><br>"
					+ "At each step you will find at the bottom of the frame:<br> "
					+ "- a description of the current step;<br>"
					+ "- instructions to go to the next step.<br><br>"
					+ "If you have any problem/comment/suggestion related to this software please feel free to contact me: zacharie.ales@univ-avignon.fr</html>");

			
			
		} catch (UndefinedColumnFormatException e) {e.printStackTrace();} catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
			e.printStackTrace();
		}

	}

	public static boolean isOver(){ return currentStepId >= lSteps.size();}

	public static AbstractTutoStep getCurrentStep(){ return isOver() ? null : lSteps.get(currentStepId);}

	public static void nextStep(){

		/* String that will contain the result of the previous step (if any) */
		String resultString = null;

		if(!isOver() && currentStepId >= 0){
			getCurrentStep().stepFinalization();
			resultString = getCurrentStep().resultsComment();
		}

		currentStepId++;

		AbstractTutoStep newStep = getCurrentStep();

		if(newStep != null){
			newStep.updateStepNumber();
			newStep.displayText(resultString);
			newStep.stepInitialization();
		}

	}
}
