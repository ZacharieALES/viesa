package tuto;

public class OpenColumnsSelectionFrame extends AbstractTutoStep {

	private static int idCount = 1;
	
	private int id;
	
	public OpenColumnsSelectionFrame() {
		super();
		id = idCount;
		idCount++;
	}
	
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
				+ "- Click on the button <img src=\"file:./src/img/tool.png\"/> in the \"Corpus\" area to open the frame which enables to edit the columns type.";
	}

	@Override
	public String resultsComment() {
		return null;
	}

	@Override
	public String gotoName() {
		return "Edit columns type n°" + id;
	}

	@Override
	public void actionsIfSkipped() {}

}
