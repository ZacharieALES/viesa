package tuto;

import View.StandardView;

public class ChangeDisplayedClusteringMethod extends AbstractTutoStep {

	@Override
	public void stepInitialization() {
		getVisualisationPanel().cscb.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getVisualisationPanel().cscb.setEnabled(false);
	}

	@Override
	public String description() {
			return "Above the slider, you can see a selection box.<br>"
					+ "It allows to change the way the patterns are clustered (different methods are possible).<br><br>"
					+ "Initially the possible choices are:<br>"
					+ "- Extracted alignments: contain the list of the extracted alignments (this is the list displayed when the clustering step is skipped).<br>"
					+ "- ROCK and Single-Link: two different methods used to cluster the patterns.<br>"
					+ "Other clustering methods may be added in the future.";
	}

	@Override
	public String instructions() {
		return "Use the selection box to change the way the patterns are clustered.";
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
