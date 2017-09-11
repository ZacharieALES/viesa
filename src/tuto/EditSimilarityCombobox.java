package tuto;

public class EditSimilarityCombobox extends AbstractEditSimilarityStep	{	
	
	@Override
	public String description() {
		return "It is also possible to set the similarity between two annotations which does not yet appear in the same table. You can do this through the elements in the area named \"Set the similarity of two annotations in different tables\"<br><br>"
				+ "Remark: <br>"
				+ "- setting the value of sim(A,B) is the same than setting the value of sim(B, A);<br>"
				+ "- sim(.,.) corresponds to the similarity between annotation \".\" and itself.";
	}

	@Override
	public String specificCommentToAddEditOperation() {
		return " (this will regroup both annotations in the same table)";
	}

}
