package tuto;

public class OpenColumnsSelectionFrame extends AbstractTutoStep {

	@Override
	public void stepInitialization() {
		getSelectionPanel().jb_tool.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jb_tool.setEnabled(false);
	}

	@Override
	public String description() {
		return "The last parameter you can modify is the choice of the annotation columns <br>"
				+ "(i.e., the columns used to find the alignments).";
	}
	
	@Override
	public String instructions() {
		return "- Go to the \"Data selection\" tab.<br>"
				+ "- Click on the gear in the \"Corpus\" area to open the frame which enables to edit the columns type.";
	}

	@Override
	public String resultsComment() {
		return null;
	}

}
