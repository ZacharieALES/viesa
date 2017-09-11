package tuto;

public class ChangeDisplayedPattern extends AbstractTutoStep {

	public boolean displayedIn1stTable = false;
	public boolean displayedIn2ndTable = false;
	public boolean displayedIn3rdTable = false;
	public boolean displayedIn4thTable = false;
	
	@Override
	public void stepInitialization() {}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
		return "You already know that an alignment is composed of two subparts of two different arrays which are similar.<br>"
				+ "We call each of these subparts a <i>pattern</i>.<br><br>"
				+ "Each visualisation table currently represent a pattern.<br>"
				+ "You can change the pattern displayed in a table by clicking on another pattern in the area called \"Clusters\".<br>"
				+ "To that end use the left and right clicks (<img src=\"file:./src/img/mouse_left.png\"> and <img src=\"file:./src/img/mouse_right.png\">) as well as the <img src=\"file:./src/img/ctrl.png\"> key:<br>"
				+ "- <img src=\"file:./src/img/mouse_left.png\">: 1st of the right tables;<br>"
				+ "- <img src=\"file:./src/img/mouse_right.png\">: 2nd of the right tables;<br>"
				+ "- <img src=\"file:./src/img/ctrl.png\"> + <img src=\"file:./src/img/mouse_left.png\">: 1st of the left tables;<br>"
				+ "- <img src=\"file:./src/img/ctrl.png\"> + <img src=\"file:./src/img/mouse_right.png\">: 2nd of the left tables.";
	}

	@Override
	public String instructions() {
		return "Change the pattern displayed in each of the four tables.";
	}

	@Override
	public String resultsComment() {
		return null;
	}
	
	public boolean isOver(){
		return displayedIn1stTable && displayedIn2ndTable && displayedIn3rdTable && displayedIn4thTable;
	}

}
