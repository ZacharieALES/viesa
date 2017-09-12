package tuto;

import model.Corpus;

public class EditMaximalSimilarity extends AbstractTutoStep {

	public int newValue = 20;
	public double newParameterValue = 20.0;
	public boolean hasValueBeenChanged;

	@Override
	public void stepInitialization() {
		getSelectionPanel().jtf_maxSim.setEnabled(true);
		getSelectionPanel().jb_process_extraction.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jtf_maxSim.setEnabled(false);
		getSelectionPanel().jb_process_extraction.setEnabled(false);
	}

	@Override
	public String description() {
		return "You may be wondering what is the difference between standard annotations and numerical annotations.<br>"
				+ "The difference lies in the way the similarity between two annotations is computed:<br>"
				+ "- In standard annotation columns, we have seen that the similarity between the annotations is defined in similarity tables;<br>"
				+ "- In numerical columns, the similarity depends on the value of only one parameter 'M' called <i>maximal similarity</i>.<br><br>"
				+ "More precisely, the similarity between two numerical annotations <i>x</i> and <i>y</i> is equal to M - |x - y|<br>"
				+ "(or 0 if this value is negative).<br><br>"
				+ "As a consequence: <br>"
				+ "\tsim(0, 0) = sim(1, 1) = sim(10, 10) = sim(x, x) = M<br>"
				+ "\tsim(2, 4) = sim(100, 98) = M - 2<br><br>"
				+ "We will now see how to modify the value of M.";
	}

	@Override
	public String instructions() {
		return "- Go to the \"Data selection\" tab;<br>"
				+ "- Set the maximal similarity to " + newValue + " in the \"Maximal similarity\" area;<br>"
				+ "- Start a new extraction.";
	}

	@Override
	public String resultsComment() {
		return "You can see that by increasing the maximal similarity, more numerical annotations are now contained in the patterns.";
	}

	@Override
	public String gotoName() {
		return "Edit the maximal similarity";
	}

	@Override
	public void actionsIfSkipped() {
		Corpus.getCorpus().setMaxDistance(this.newParameterValue);
	}

}
