package tuto;

public class EvaluateAlignment extends AbstractTutoStep {

	public boolean usedShortcutA = false;
	public boolean usedShortcutZ = false;
	public boolean usedShortcutE = false;
	public boolean usedShortcutR = false;
	
	@Override
	public void stepInitialization() {}

	@Override
	public void stepFinalization() {}

	@Override
	public String description() {
		return "Your job in this experiment will be to use the software to find interesting alignments.<br>"
				+ "To do so you will perform several extractions with different value of the parameters.<br>"
				+ "After each extraction you may visualize and <i>evaluate</> some of the obtained alignments (you do not have to evaluate all of them).<br><br>"
				+ "To each alignment is associated one <i>statuts</i>. There exists four possible statuts:<br>"
				+ "1 - <i>Unevaluated alignment</i>: it means that you do not currently have evaluated this alignment;<br>"
				+ "2 - <i>Irrelevant alignment</i>: it means that you do not find the alignment relevant;<br>"
				+ "3 - <i>Relevant and non optimal alignment</i>: it means that you find the alignment relevant but that it could be improved (by adding or removing some annotations);<br>"
				+ "4 - <i>Relevant and optimal alignment</i>: it means that you find the alignment relevant and that it could no be improved by adding or removing annotations.<br><br>"
				+ "To evaluate an alignment you can use the radio buttons in the \"Alignment status\" area or you can use the shortcuts A, Z, E and R.";
	}

	@Override
	public String instructions() {
		return 	"Use at least once each of the four evaluation keyboard shortcut (the next tutorial step will then automatically starts).";
	}

	@Override
	public String resultsComment() {
		return null;
	}

	public boolean isOver() {
		return usedShortcutA && usedShortcutE && usedShortcutZ && usedShortcutR;
	}

}
