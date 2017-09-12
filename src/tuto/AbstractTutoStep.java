package tuto;

import View.SelectionPanel;
import View.StandardView;
import View.VisualisationPanel;
import main.MainTutorial;

/**
 * Represent a step of the tutorial version of the software
 * @author zach
 *
 */
public abstract class AbstractTutoStep {

	/** Action performed at the beginning of the step (e.g., brighten that should be used during the step) */
	public abstract void stepInitialization();
	
	/** Action performed at the end of the step (e.g., darken a button used during this step) */
	public abstract void stepFinalization();
	
	/** Returns a string which describes the feature associated to this step */
	public abstract String description();

	/** Returns a string which describes how to go to the next step */
	public abstract String instructions();

	/** Returns a string which describes the result of the step and that will be displayed after the step is performed */
	public abstract String resultsComment();
	
	/** Returns the name of the step if it can directly be jumped to; returns null otherwise
	 * For example it is not possible to jump directly to an EditSimilarity step as it first requires the OpenEditSimilarity step.
	 */
	public abstract String gotoName();
	
	/** Contains the actions that must be performed if the step is skipped because the user jumped to a subsequent step */
	public abstract void actionsIfSkipped();
	
	
	public SelectionPanel getSelectionPanel(){
		return StandardView.getInstance().jf_s;
	}
	
	public VisualisationPanel getVisualisationPanel(){
		return VisualisationPanel.getInstance();
	}
	
	public void displayText(String resultOfPreviousStep){
		
		String description = description();
		
		if(resultOfPreviousStep != null)
			description = resultOfPreviousStep + "<br><br>" + description;
		
		StandardView.getInstance().jlTutoStepDescription.setText("<html>" + description + "</html>");
		
		StandardView.getInstance().jlTutoStepInstructions.setText("<html>" + instructions() + "</html>");
	}

	public void updateStepNumber() {
		StandardView.getInstance().jlTuto1.setText("<html><b>Description (step " + (MainTutorial.currentStepId) + "/" + (MainTutorial.lSteps.size()-1) + ")</b></html>");
	}
	
}
