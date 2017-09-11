package tuto;

public class EditSimilarity extends AbstractEditSimilarityStep	{	

	@Override
	public String specificCommentToAddEditOperation() {
		return " (this will regroup both annotations in the same table)";
	}

	@Override
	public String resultsComment(){
		return "As you can see, annotation \".\" does not appear in the alignments anymore.";
	}

	@Override
	public String description(){
		return "Set sim(.,.) back to 0 and sim(B,B) back to 10.";
	}
}
