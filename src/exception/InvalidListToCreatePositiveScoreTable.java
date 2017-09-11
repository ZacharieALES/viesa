package exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception called when a PositiveScoreTable is created from an invalid List<String[]>
 * (i.e. the list is empty or one line of the list does not contain exactly three elements or the third elements on a line is not a double) 
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class InvalidListToCreatePositiveScoreTable extends AbstractException{

	List<ArrayList<String>> list;
	int row;
	
	public InvalidListToCreatePositiveScoreTable(List<ArrayList<String>> list, int row){
		this.list = list;
		this.row = row;
	}
	
	@Override
	public String defaultMessage() {
		if(list.size() == 0)
			return "The list is empty, cannot create an empty PositiveScoreTable";
		else{
			
			List<String> a_line = list.get(row);
			String line  = a_line.get(0);
			for(int i = 1 ; i < a_line.size(); ++i)
				line += "," + a_line.get(i);
			return "One of the row of the list is invalid: " + line + " (size " + a_line.size()+ "). It should contain exactly three values and the third one must be a real value.";
		}
	}
	
	

}
