package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import View.StandardView;
import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;
import model.AAColumnFormat.ColumnType;
import model.Corpus;
import tuto.AbstractTutoStep;
import tuto.ChangeDisplayedAlignment;
import tuto.ChangeDisplayedCluster;
import tuto.ChangeDisplayedClusterSet;
import tuto.ChangeDisplayedClusteringMethod;
import tuto.ChangeDisplayedPattern;
import tuto.ChangeTablesOrientation;
import tuto.EditColumnSelection;
import tuto.EditDesiredNumberOfAlignmentsStep;
import tuto.EditGap;
import tuto.EditMaximalSimilarity;
import tuto.EditSimilarity;
import tuto.EditSimilarityCombobox;
import tuto.EditSimilarityTable;
import tuto.FinalizationStep;
import tuto.FirstClustering;
import tuto.FirstExtraction;
import tuto.InitializationStep;
import tuto.OpenColumnsSelectionFrame;
import tuto.OpenEditSimilarityFrame;

public class MainTutorial {



	public static boolean IS_TUTO = false;	
	public static List<AbstractTutoStep> lSteps = new ArrayList<>();
	public static int currentStepId = -1;
	public static final int desiredNumberOfClusters = 3;
	public static final int gap = 100;

	public static void run(int startingStep){

		/* Set the tutorial corpus */
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

		try {

			StandardView sv = new StandardView();
//			StandardView sv = StandardView.getInstance();

			Corpus.getCorpus().setColumnFormat(al_comment, al_annot, null);
			Corpus.getCorpus().setAnnotationSimilarities(st);
			Corpus.getCorpus().removeAllAA();
			Corpus.getCorpus().add(fo_path, Main.hasHeader);

			SABRE.getInstance().setParam(new SABREParameter(gap, gap/2, desiredNumberOfClusters)); 
			sv.jf_s.jtf_K.setText("3");

			Corpus.getCorpus().setMaxDistance(5.0);

			MainTutorial.nextStep();
			
			for(int i = 1 ; i < startingStep ; i++){
				MainTutorial.getCurrentStep().actionsIfSkipped();
				MainTutorial.getCurrentStep().stepFinalization();
				MainTutorial.nextStep(false);
			}

			if(startingStep == 0)
				JOptionPane.showMessageDialog(sv, "<html>This tutorial will present the software and its features in several steps.<br><br>"
						+ "At each step you will find at the bottom of the frame:<br> "
						+ "- a description of the current step;<br>"
						+ "- instructions to go to the next step.<br><br>"
						+ "If you have any problem/comment/suggestion related to this software please feel free to contact me: zacharie.ales@univ-avignon.fr</html>");



		} catch (UndefinedColumnFormatException e) {e.printStackTrace();} 
		catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
			e.printStackTrace();
		}

	}

	public static boolean isOver(){ return currentStepId >= lSteps.size();}

	public static AbstractTutoStep getCurrentStep(){ return isOver() ? null : lSteps.get(currentStepId);}

	
	/**
	 * Go to the next tutorial step (by default, display the previous step result
	 */
	public static void nextStep(){
		nextStep(true);
	}
	
	/**
	 * Display the next tutorial step
	 * @param displayPreviousStepResult True if the result of the previous step must be displayed; false otherwise (e.g., when we jump to a given step, it is not desirable to display the previous result)
	 */
	public static void nextStep(boolean displayPreviousStepResult){

		/* String that will contain the result of the previous step (if any) */
		String resultString = null;

		if(!isOver() && currentStepId >= 0 && displayPreviousStepResult){
			getCurrentStep().stepFinalization();
			resultString = getCurrentStep().resultsComment();
		}

		currentStepId++;

		AbstractTutoStep newStep = getCurrentStep();

		if(newStep != null){
			newStep.updateStepNumber();
			newStep.displayText(resultString);	
			newStep.stepInitialization();
			
//			StandardView.getInstance().remove(StandardView.getInstance().jspTuto);
//			StandardView.getInstance().addJSPTuto();
			
//			StandardView.getInstance().jspTuto.getVerticalScrollBar().setValue(0);
			
//			StandardView.getInstance().jspTuto.repaint();
//			StandardView.getInstance().repaint();
//			StandardView.getInstance().validate();
//			StandardView.getInstance().jspTuto.validate();
		}
	}

	/**
	 * Initialize the tutorial step
	 * @return A HashMap that will contain the steps at which it will be possible to directly go. An HashMap item Integer corresponds to the number of the step (starts at 0), the String corresponds to the description of the step.  
	 */
	public static List<StepWrapper> initialize() {
		MainTutorial.IS_TUTO = true;
		Main.hasHeader = false;
		currentStepId = -1;
		
		lSteps = new ArrayList<>();

		/* Set the tutorial steps */
		lSteps.add(new InitializationStep());

		lSteps.add(new FirstExtraction());
		lSteps.add(new ChangeDisplayedAlignment());
		lSteps.add(new ChangeDisplayedPattern());
		lSteps.add(new ChangeTablesOrientation());

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
				return "As you can observe the alignments are now very big and contain many annotations \".\".<br>"
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
		lSteps.add(new FirstClustering());
		lSteps.add(new ChangeDisplayedCluster());
		lSteps.add(new ChangeDisplayedClusterSet());
		lSteps.add(new ChangeDisplayedClusteringMethod());

		lSteps.add(new OpenColumnsSelectionFrame());
		lSteps.add(new EditColumnSelection());

		lSteps.add(new OpenColumnsSelectionFrame(){
			@Override
			public String description(){return "Three buttons are dedicated to the management of the arrays contained in the corpus:<br>"
					+ "- <img src=\"file:./src/img/plus.png\"/>: enables to add arrays into the corpus;<br>"
					+ "- <img src=\"file:./src/img/minus.png\"/>: enables to remove the array currently selected in the list from the corpus;<br>"
					+ "- <img src=\"file:./src/img/trash.png\"/>: remove all the arrays from the corpus.<br>"
					+ "These buttons are not used in the context of the tutorial but they will be useful on other corpuses.<br><br>"
					+ "We will now explain the role of the column type called \"Numerical\".";}
		});

		EditColumnSelection ecs = new EditColumnSelection(){
			@Override
			public String description(){
				return "The column type \"Numerical\" is simply used when a column contains numerical values.";
			}

			@Override
			public String resultsComment() {
				return "You can see that numerical values appear in some alignments.";
			}
		};
		ecs.modifiedColId = 4;
		ecs.modifiedColType = ColumnType.NUMERICAL_ANNOTATION;
		ecs.originalColType = ColumnType.NONE;
		ecs.performExtraction = true;

		lSteps.add(ecs);

		lSteps.add(new EditMaximalSimilarity());

		lSteps.add(new FinalizationStep());
		
		List<StepWrapper> result = new ArrayList<>();
		
		
		for(int i = 0 ; i < lSteps.size() ; i++){
			
			AbstractTutoStep ats = lSteps.get(i);
			
			if(ats.gotoName() != null)
				result.add(new StepWrapper(i, ats.gotoName()));
		}
		
		return result;


	}
	
	public static class StepWrapper{
		
		public int stepNb;
		public String stepDescription;
		
		public StepWrapper(int stepNb, String stepDescription){
			this.stepNb = stepNb;
			this.stepDescription = stepDescription;
		}
		
		@Override
		public String toString(){
			return "Step n°" + stepNb + " - " + stepDescription;
		}
	}
}
