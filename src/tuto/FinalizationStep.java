package tuto;

public class FinalizationStep extends AbstractTutoStep {


	@Override
	public void stepFinalization() {
		
		/* Disable all the components that can be used by the user */
		getSelectionPanel().jb_process_extraction.setEnabled(true);
		getSelectionPanel().jb_process_extraction_and_clustering.setEnabled(true);
		getSelectionPanel().jb_sim_score_fileChooser.setEnabled(true);
		getSelectionPanel().jb_sim_editor.setEnabled(true);
		getSelectionPanel().jb_tool.setEnabled(true);
		getSelectionPanel().jb_addAA.setEnabled(true);
		getSelectionPanel().jb_removeAA.setEnabled(true);
		getSelectionPanel().jb_removeAllAA.setEnabled(true);
		getSelectionPanel().jtf_gap_score.setEnabled(true);
		getSelectionPanel().jtf_desired_nb_of_alignments.setEnabled(true);
		getSelectionPanel().jtf_sim_scores.setEnabled(true);
		getSelectionPanel().jtf_K.setEnabled(true);
		getSelectionPanel().jtf_maxSim.setEnabled(true);
		
	}

	@Override
	public void stepInitialization() {}

	@Override
	public String description() {
		return "";
	}

	@Override
	public String instructions() {
		return "The tutorial is over. You are now ready to use VIESA to its full potential!";
	}

	@Override
	public String resultsComment() {
		return null;
	}

}
