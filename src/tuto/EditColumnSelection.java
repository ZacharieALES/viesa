package tuto;

public class EditColumnSelection extends AbstractTutoStep {

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
		return 	"This window is used to modify the columns <i>type</i> of the input csv files.<br>"
				+ "The possible types of a column are:<br>"
				+ "1 - annotation: the column is used to extract the patterns (i.e., some annotations of this column can appear in an alignment);<br>"
//				+ "- unused: the column is not displayed and not used to extract the patterns;<br>"
				+ "2 - comment: the column is not used to extract the patterns.<br><br>"
				+ "To change the type of a column, click on it.";
	}

	@Override
	public String instructions() {
		return "- Change the type of one or several columns;<br>"
				+ "- Validate by pressing the \"OK\" button;<br>"
				+ "- Start a new extraction.";
	}

	@Override
	public String resultsComment() {
		return "You can see that the color of the header of the comment columns is yellow and that no annotation from these columns appear in the alignments.";
	}

}
