package tuto;

public class FirstExtraction extends AbstractTutoStep {

	@Override
	public void stepInitialization() {
		getSelectionPanel().jb_process_extraction.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jb_process_extraction.setEnabled(false);
	}

	@Override
	public String description() {
		return "The aim of this software is to extract regularities in <i>annotation arrays</i>.<br>"
				+ "An annotation array is a data structure in which: <br>"
				+ "- the order of the lines is important (it usually represent temporality);<br>"
				+ "- the order of the columns is irrelevant (they could be exchanged without altering the meaning of the data).<br><br>"
				+ "In this tutorial we consider two very simple arrays named \"Tuto file 1.csv\" and \"Tuto file 2.csv\".<br>"
				+ "One of them is currently displayed in the above overview and you can click on the name of the other in the list located<br>"
				+ "in the area labeled \"Corpus\" to visualize it.<br><br>"
				+ "You are currently in the tab which enables to modify the extraction parameters. <br>"
				+ "Before explaining how these parameters work, we will perform a first extraction to see the regularities obtained with the <br>"
				+ "default values.";
	}

	@Override
	public String instructions() {
		return "Start the extraction by pressing the button named \"Extract\"";
	}

	@Override
	public String resultsComment() {
		return "You are now in the visualization tab which is automatically displayed at the end of each extraction.<br><br>"
				+ "This tab enables to display the obtained <i>alignments</i>.<br>"
				+ "The term \"alignment\" characterizes two subparts of two different arrays which are similar.<br>"
				+ "You can see four tables (2 on the left, 2 on the right). <br>"
				+ "The two tables on the right represent the two arrays involved in the <b>first</b> alignment. <br>"
				+ "The two tables on the left represent the two arrays involved in the <b>second</b> alignment. <br>"
				+ "The annotations which are part of an alignment are displayed in red.";
	}

	@Override
	public String gotoName() {
		return "Extract patterns";
	}

	@Override
	public void actionsIfSkipped() {}

}
