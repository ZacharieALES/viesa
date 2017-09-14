package tuto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import extraction.SABRE;
import model.AnnotationColumn;
import model.Corpus;

public abstract class AbstractEditSimilarityStep extends AbstractTutoStep {

	public List<EditOperation> loperations = new ArrayList<>();
	
	@Override
	public void stepInitialization() {
		getSelectionPanel().jb_process_extraction.setEnabled(true);
	}

	@Override
	public void stepFinalization() {
		getSelectionPanel().jb_process_extraction.setEnabled(false);
		
	}

	public String errorMessage(){
		
		String result = "All the similarities have already been correctly modified. Please click on the button labelled \"OK\" and start a new extraction.";
		if(loperations.size() > 0){
			result = "It seems that you did not select an expected similarity.<br>The expected ";
			
			if(loperations.size() > 1)
				result += "similarities that you have to set are: ";
			else
				result += "similarity that you have to set is: ";
			
			for(EditOperation eo: loperations)
				result += "<br>- sim(" + eo.a1 + "," + eo.a2 + ") which must be set to the value " + eo.valueToSet;
		}
		
		return "<html>" + result + "</html>";
	}
	
	public class EditOperation{
		
		public String a1, a2;
		public double valueToSet;
		public boolean hasBeenSet = false;
		
		public EditOperation(double valueToSet, String a1, String a2){
			this.valueToSet = valueToSet;
			this.a1 = a1;
			this.a2 = a2;
		}
	}
	
	/**
	 * Test if the entered value correspond to an expected edit operation
	 * @param a1
	 * @param a2
	 * @param d
	 * @return True if a corresponding edit operation has been found; false otherwise.
	 */
	public boolean removeEditOperationIfExpected(String a1, String a2, double d){
		
		Iterator<EditOperation> it = loperations.iterator();
		boolean found = false;
		
		while(it.hasNext() && !found){
			EditOperation eo = it.next();
			
			if(eo.valueToSet == d &&
					(eo.a1.equals(a1) && eo.a2.equals(a2)
					|| eo.a1.equals(a2) && eo.a2.equals(a1))
					){
				found = true;
				loperations.remove(eo);
			}
				
		}
		
		return found;
	}
	
	public abstract String specificCommentToAddEditOperation();
	
	@Override
	public String instructions() {
		
		String result = "";
		
		for(EditOperation eo: loperations){
			result += "- Set sim(" + eo.a1 + "," + eo.a2 + ") to the value " + eo.valueToSet;
			
			if(!eo.a1.equals(eo.a2))
				result += "" + specificCommentToAddEditOperation();
			
			result +=  "<br>";
		}
		
		return  result
				+ "- Confirm the modifications by clicking on the button labelled \"OK\".<br>"
				+ "- Start a new extraction.";
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
	public void actionsIfSkipped() {
		for(EditOperation eo: this.loperations){
			int firstIndex = Corpus.getCorpus().getAnnotationIndex(eo.a1);
			int secondIndex = Corpus.getCorpus().getAnnotationIndex(eo.a2);
			AnnotationColumn.pst.setSimilarity(firstIndex, secondIndex, eo.valueToSet);
		}
	}

}
