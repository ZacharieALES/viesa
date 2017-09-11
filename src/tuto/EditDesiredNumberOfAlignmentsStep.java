package tuto;

public class EditDesiredNumberOfAlignmentsStep extends AbstractTutoStep {

	public boolean hasValueBeenChanged = false;
	public int newParameterValue = 5;
	
	@Override
	public void stepInitialization() {
		getSelectionPanel().jtf_desired_nb_of_alignments.setEnabled(true);
		getSelectionPanel().jb_process_extraction.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jtf_desired_nb_of_alignments.setEnabled(false);
		getSelectionPanel().jb_process_extraction.setEnabled(false);
	}

	@Override
	public String description() {
		return "Now that you know how to extract and visualize alignments and patterns, we will see what are the parameters and how do they influence the extraction results.<br><br>"
				+ "The first parameter is the desired number of alignments which enables to control the number of alignments obtained during an extraction.<br><br>"
				+ "To each extracted alignment is associated a score. <br>"
				+ "For a given extraction, if the desired number of alignments is equal to 2, only the 2 alignments with the highest score will be obtained.<br><br>"
				+ "- Remark: <br>"
				+ "The number of obtained alignments may be higher than the value of this parameter if several alignments have the same score. <br>"
				+ "For example if the desired number of alignments is equal to 3 and if " + newParameterValue + " alignments of respective score 10, 8, 5, 5, 2 are found, then the 4 alignments of highest score (10, 8, 5 and 5) will be returned.";
	}

	@Override
	public String instructions() {
		return "- Go the \"Data selection\" tab<br>"
				+ "- Change the value of the desired number of alignments to " + newParameterValue + "<br>"
				+ "- Start the extraction";
	}

	@Override
	public String resultsComment() {
		return "You can see that the number of obtained alignments is now higher";
	}

}
