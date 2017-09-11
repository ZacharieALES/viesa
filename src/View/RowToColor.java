package View;

import java.awt.Color;


public class RowToColor{
	
	int row;
	Color background;
	
	public RowToColor(int row, Color background){
		this.row = row;
		this.background = background;

	}
	
	@Override
	public boolean equals(Object rc){
		if(rc instanceof RowToColor)
			return row == ((RowToColor)rc).row;
		else
			return false;
	}
	
	
	
}
