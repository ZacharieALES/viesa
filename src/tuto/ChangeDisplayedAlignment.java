package tuto;

public class ChangeDisplayedAlignment extends AbstractTutoStep {

	public boolean normalClickOnAlignmentPerformed = false;
	public boolean ctrlClickOnAlignmentPerformed = false;
	
	@Override
	public void stepInitialization() {}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
		return "To change the displayed alignment, you can click on another alignment in the area called \"Clusters\".<br>"
				+ "If you press the <img src=\"file:./src/img/ctrl.png\"> key while clicking, the alignment will appear in the two tables on the left; otherwise it will appear in two tables on the right.";
	}

	@Override
	public String instructions() {
		return "- Display an alignment in the two tables on the left;<br>"
				+ "- Display an alignment in the two tables on the right.";
	}

	@Override
	public String resultsComment() {
		return null;
	}
	
	public boolean isOver(){
		return normalClickOnAlignmentPerformed && ctrlClickOnAlignmentPerformed;
	}

}
