package tuto;

public class ChangeTablesOrientation extends AbstractTutoStep {

	public boolean horizontalClicked = false;
	public boolean verticalClicked = false;
	
	@Override
	public void stepInitialization() {
		getVisualisationPanel().jb_switchOrientation12.setEnabled(true);
		getVisualisationPanel().jb_switchOrientationAB.setEnabled(true);
	}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
		return "You can change the tables orientation (horizontal or vertical) by clicking on the icon <img src=\"file:./src/img/vertical2.png\"> or <img src=\"file:./src/img/horizontal2.png\">.<br>";
	}

	@Override
	public String instructions() {
		return "- Change once the orientation from vertical to horizontal;<br>"
				+ "- Change once the orientation from horizontal to vertical.";
	}

	@Override
	public String resultsComment() {
		return null;
	}
	
	public boolean isOver(){
		return horizontalClicked && verticalClicked;
	}

	@Override
	public String gotoName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void actionsIfSkipped() {
		getVisualisationPanel().jb_switchOrientation12.setEnabled(true);
		getVisualisationPanel().jb_switchOrientationAB.setEnabled(true);
	}

}
