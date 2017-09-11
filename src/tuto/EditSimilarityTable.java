package tuto;

public class EditSimilarityTable extends AbstractEditSimilarityStep{

	@Override
	public String description() {
		return "This frame is used to edit the inter-annotation similarities.<br>"
				+ "You can first see that the similarity of A, B, C, D, E and F with themselves is equal to 10.<br>"
				+ "All the other inter-annotation similarities (which does not appear here) are equal to 0.<br>"
				+ "In particular, the dot annotation \".\" has a similarity of 0 with all annotations even itself (that is why it never appears in the alignments).<br><br>"
				+ "One first way to edit the similarities is to modify an existing value by double clicking on it.<br><br>"
//				+ "Remarks:<br>"
//				+ "- Each time you validate some similarity modifications, a custom score table file is saved in the corpus folder;<br>"
//				+ "- When a given corpus is open, the last corresponding custom score table file is automatically used (you can manually select another file if needed)."
				;
	}

	@Override
	public String specificCommentToAddEditOperation() {
		return " (to do so double click in the proper table on the value you want to modify)";
	}

}
