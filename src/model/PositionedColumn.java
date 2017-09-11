package model;

/**
 * Represents a column type and its position in the initial csv files
 * @author zach
 *
 */
public class PositionedColumn{
	
	public AbstractColumn<?> column;
	public int position;
	
	public PositionedColumn(AbstractColumn<?> column, int position){
		this.column = column;
		this.position = position;
	}

}
