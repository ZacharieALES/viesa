package model;

public class PositionedColumn{
	
	public AbstractColumn<?> column;
	public int position;
	
	public PositionedColumn(AbstractColumn<?> column, int position){
		this.column = column;
		this.position = position;
	}

}
