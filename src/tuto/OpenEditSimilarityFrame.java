package tuto;

public class OpenEditSimilarityFrame extends AbstractTutoStep {


	@Override
	public void stepInitialization() {
		getSelectionPanel().jb_sim_editor.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jb_sim_editor.setEnabled(false);
	}


	@Override
	public String description() {
		return "The next parameter that we will modify is the inter-annotation similarity.<br>"
				+ "To compute the score of an alignment, VIESA has to know the similarity between each couple of annotations (ex: sim(A,A) = 10, sim(A,B) = 5, ...).<br><br>"
				+ "Remarks:<br>"
				+ "- If the similarity between two annotations is not specified by the user it will considered to be 0 (so these annotations will never be aligned together in an alignment).<br>"
				+ "- Annotations in different columns cannot be aligned together so specifying their similarity will not affect the results<br>"
				+ "(e.g., if annotation 'A' only appear in the first column and annotation 'C' only appear in the second column, modifying the value of sim(A,C) will not alter the obtained alignments).";
	}
	
	@Override
	public String instructions() {
		return "- Go to the \"Data selection\" tab.<br>"
				+ "- Click on the pencil in the \"Similarity scores\" area to open the frame which enables to edit the similarity.";
	}

	@Override
	public String resultsComment() {
		return null;
	}

}
