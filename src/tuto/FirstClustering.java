package tuto;

public class FirstClustering extends AbstractTutoStep {

	@Override
	public void stepInitialization() {
		getSelectionPanel().jb_process_extraction_and_clustering.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jb_process_extraction_and_clustering.setEnabled(false);
	}

	@Override
	public String description() {
		return "If the number of alignments is to high, it may be hard to identify interesting patterns.<br>"
				+ "In that case, on top of an extraction, you can also perform a clustering of the patterns.<br><br>"
				+ "The idea is that similar patterns will be clustered together. <br>Thus, the biggest clusters obtained will correspond to the most recurrent patterns.";
	}

	@Override
	public String instructions() {
		return "Start the extraction followed by a clustering by pressing the button named \"Extract and cluster\"";
	}

	@Override
	public String resultsComment() {
		return "As you can see, the \"Cluster\" area of the visualisation tab is slightly modified when the clustering step is performed.<br><br>"
				+ "First you can observe that the list now contain clusters instead of alignments.<br>"
				+ "In practice, the difference between a cluster and alignment is that a cluster may contain more or less than two patterns.";
	}

	@Override
	public String gotoName() {
		return "Extract and cluster patterns";
	}

	@Override
	public void actionsIfSkipped() {}

}
