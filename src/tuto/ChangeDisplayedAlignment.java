package tuto;

public class ChangeDisplayedAlignment extends AbstractTutoStep {

	/** Number of alignments displayed */
	public int counter = 0;
	
	/** Number of alignments required to finish this step */
	public int numberOfAlignmentsDisplayedToFinish = 3;
	
	@Override
	public void stepInitialization() {}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
		return "To change the displayed alignment, you can either click on another alignment in the list located in the area called \"Unevaluated alignments\" (we will soon explain what \"unevaluated\" mean) or use the buttons \"Previous/Next alignment\" or use the following shortcuts:<br>"
				+ "- \"Ctrl + left\" to get the previous unevaluated alignment;<br>"
				+ "- \"Ctrl + right\" to get the next unevaluated alignment.";
	}

	@Override
	public String instructions() {
		return "Use three times any keyboard shortcut to change the displayed alignment (the next tutorial step will then automatically starts).";
	}

	@Override
	public String resultsComment() {
		return null;
	}

}
