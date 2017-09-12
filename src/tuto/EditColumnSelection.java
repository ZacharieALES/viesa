package tuto;

import java.util.ArrayList;
import java.util.List;

import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import model.AAColumnFormat;
import model.Corpus;
import model.NumericalColumn;
import model.AAColumnFormat.ColumnType;
import model.AnnotationColumn;
import model.CommentColumn;
import model.PositionedColumn;

public class EditColumnSelection extends AbstractTutoStep {

	public int modifiedColId = 0;
	public ColumnType modifiedColType = ColumnType.NONE;
	public ColumnType originalColType = ColumnType.COMMENT;
	public boolean performExtraction = false;
	
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
				+ "- unused: the column is not displayed and not used to extract the patterns;<br>"
				+ "- comment: the column is not used to extract the patterns;"
				+ "- annotation: the column is used to extract the patterns (i.e., some annotations of this column can appear in an alignment);<br>"
				+ "- numerical: this type will be explained in a subsequent step.<br><br>"
				+ "To change the type of a column, click on it.";
	}

	@Override
	public String instructions() {
		String result = "- Change the type of column n°" + (modifiedColId + 1) + " from \"" + originalColType.getName() + "\" to \""+ modifiedColType.getName() + "\";<br>"
				+ "- Validate by pressing the \"OK\" button.";
		
		if(performExtraction)
			result += "<br>- Start a new extraction.";
		
		return result;
	}

	@Override
	public String resultsComment() {
		return "You can see that the first column does not appear anymore in the arrays.";
	}

	@Override
	public String gotoName() {
		return null;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void actionsIfSkipped() {

		AAColumnFormat oldAacf = Corpus.getCorpus().aacf;
		
		try {

			List<PositionedColumn> alPc = new ArrayList<>();
			for(int i = 0 ; i < oldAacf.getTotalNumberOfColumns() ; i++)
				alPc.add(oldAacf.getPositionedColumn(i));
			
			/* PositionedColumn to add; null if the new type is NONE */
			PositionedColumn pc = null;
			
			if(this.modifiedColType.equals(ColumnType.NONE)){

				switch(this.modifiedColType){
				case ANNOTATION:
					pc = new PositionedColumn(new AnnotationColumn(), this.modifiedColId);break;
				case NUMERICAL_ANNOTATION:
					pc = new PositionedColumn(new NumericalColumn(), this.modifiedColId);break;
				case COMMENT:
					pc = new PositionedColumn(new CommentColumn(), this.modifiedColId);break;	
				}
			}
			
			/* If the modified column was not originally in the column format */
			if(originalColType.equals(ColumnType.NONE))
				alPc.add(pc);
			
			/* If the modified column was originally in the column format */
			else{
				
				boolean found = false;
				int colId = 0;
				
				while(!found && colId < alPc.size()){
					
					PositionedColumn pcCurrent = alPc.get(colId);
					
					/* If this is the modified column */
					if(pcCurrent.position == this.modifiedColId){
						found = true;
						
						/* If the new column type is NONE */
						if(pc == null)
							alPc.remove(colId);
						else
							alPc.set(colId, pc);
					}
					
					colId++;
					
				}
				
			}			

			Corpus.getCorpus().setAACF(new AAColumnFormat(alPc), true);
			
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
			e.printStackTrace();
		}
		
	}

}
