package tuto;

import View.StandardView;

public class ChangeDisplayedCluster extends AbstractTutoStep {

	@Override
	public void stepInitialization() {
		
		if(getVisualisationPanel().slider != null)
			getVisualisationPanel().slider.setEnabled(false);
	}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
			return "If you click on a cluster the first patterns it contains will be displayed in the four tables<br>"
					+ "(or in less tables if the cluster contains less that four patterns).";
	}

	@Override
	public String instructions() {
		return "Display the first patterns of a cluster by clicking on it in the cluster list.";
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
