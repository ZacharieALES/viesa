package tuto;

public class CompleteNonOptimalPatterns extends AbstractTutoStep {

	@Override
	public void stepInitialization() {}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
		return "In this frame you can visualize the alignments that you evaluated as \"relevant but non optimal\".<br>"
				+ "The shortcuts to change the visible alignment are the same than in the visualization panel (Ctrl+left, Ctrl+right).<br><br>"
				+ "Alignments evaluated as non optimal but relevant can be improved by adding or removing annotations.<br>"
				+ "In this frame you can specify for each of these alignments, the annotations that should be removed or added to improve its quality.<br>"
				+ "To do so, simply click on the corresponding annotations.<br>"
				+ "The modified annotations will appear in color.<br><br>"
				+ "To finish the work on a corpus, you have to edit each alignment and close the window.<br><br>"
				+ "Tip:<br>"
				+ "- If you have a lot of alignment, you can also find useful the shortcuts Ctrl+Shift+left and Ctrl+Shift+right which enable to respectively see the next and previous <b>unedited</b> alignment.";
	}

	@Override
	public String instructions() {
		return "- Edit all the alignments;<br>"
				+ "- Close the window.";
	}

	@Override
	public String resultsComment() {
		return null;
	}

}
