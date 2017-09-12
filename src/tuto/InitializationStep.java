package tuto;

import main.MainTutorial;

public class InitializationStep extends AbstractTutoStep {

	@Override
	public void stepInitialization() {
		
		/* Disable all the components that can be used by the user */
		getSelectionPanel().jb_process_extraction.setEnabled(false);
		getSelectionPanel().jb_process_extraction_and_clustering.setEnabled(false);
		getSelectionPanel().jb_sim_score_fileChooser.setEnabled(false);
		getSelectionPanel().jb_sim_editor.setEnabled(false);
		getSelectionPanel().jb_tool.setEnabled(false);
		getSelectionPanel().jb_addAA.setEnabled(false);
		getSelectionPanel().jb_removeAA.setEnabled(false);
		getSelectionPanel().jb_removeAllAA.setEnabled(false);
		getSelectionPanel().jtf_gap_score.setEnabled(false);
		getSelectionPanel().jtf_desired_nb_of_alignments.setEnabled(false);
		getSelectionPanel().jtf_sim_scores.setEnabled(false);
		getSelectionPanel().jtf_K.setEnabled(false);
		getSelectionPanel().jtf_maxSim.setEnabled(false);

		getVisualisationPanel().jb_switchOrientation12.setEnabled(false);
		getVisualisationPanel().jb_switchOrientationAB.setEnabled(false);
		getVisualisationPanel().cscb.setEnabled(false);
		
		MainTutorial.nextStep();
	}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
		return "Initialization of the tutorial";
	}

	@Override
	public String instructions() {
		return "Please wait";
	}

	@Override
	public String resultsComment() {
		return null;
	}

	@Override
	public String gotoName() {
		return null;
	}

	@Override
	public void actionsIfSkipped() {}

}
