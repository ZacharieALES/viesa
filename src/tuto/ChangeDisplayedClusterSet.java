package tuto;

import View.StandardView;

public class ChangeDisplayedClusterSet extends AbstractTutoStep {

	public static final int numberOfClusters = 4;

	@Override
	public void stepInitialization() {
		
		if(getVisualisationPanel().slider != null)
			getVisualisationPanel().slider.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		if(getVisualisationPanel().slider != null)
			getVisualisationPanel().slider.setEnabled(false);
	}

	@Override
	public String description() {
			return "You can observe a horizontal slider above the cluster list.<br>"
					+ "It enables to change the number of clusters in which the patterns are grouped.<br><br>"
					+ "If you find that the clusters contain:<br>"
					+ "- too many patterns, you can decrease its value.<br>"
					+ "- too few patterns, you can increase its value.";
	}

	@Override
	public String instructions() {
		return "Use the slider to set the number of clusters to " + numberOfClusters;
	}

	@Override
	public String resultsComment() {
		return "As a result, the list now contains " + numberOfClusters + " clusters.<br>"
				+ "Remark: The number of clusters initially displayed after clicking on the button \"Extract and cluster\" can be modified in the selection tab<br>"
				+ "(its value does not impact the results, it just enables to select how many clusters will be displayed initially).";
	}

	@Override
	public String gotoName() {
		return null;
	}

	@Override
	public void actionsIfSkipped() {}

}
