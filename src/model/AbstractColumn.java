package model;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import model.AAColumnFormat.ColumnType;

public abstract class AbstractColumn<T> implements Serializable{
	
	private static final long serialVersionUID = -5157963625964999170L;
	ArrayList<T> values = new ArrayList<T>();
	
	/**
	 * Similarity between two elements of the column
	 * @param a
	 * @param b
	 * @return
	 */
	public abstract double sim(int a,T b);
	
	/**
	 * Create an empty column of the same type than <this>
	 * @return
	 */
	public abstract AbstractColumn<T> createNewInstance();
	
	/**
	 * Add an element to the column
	 * @param s The string which corresponds to the element
	 */
	public abstract void addElement(String s);


	/**
	 * Test the type of a column
	 * @param t Type tested
	 * @return True if the column is of type <t>
	 */
	public abstract boolean isFromType(ColumnType t);
	
	/**
	 * 
	 * @return Type of the column
	 */
	public abstract ColumnType getType();
	
	/** Return the element which corresponds to an empty annotation for a given type of columns **/
	public abstract T getEmptyAnnotation();
	
	public void add(T t){
		values.add(t);
	}
	
	public abstract String toString(int j);
	
	public abstract boolean isCommentColumn();

	/**
	 * Create a subcolumn of <this> which corresponds to line bounds[0] to line bounds[1] only filled with empty annotations except in rows which index are in nonEmptyCoordinates
	 * @param bounds The rows of <this> which appear in the subcolumns are the one between bounds[0] and bounds[1] included
	 * @param nonEmptyCoordinates Rows of the the column which must not be filled by empty coordinates in the subColumn
	 * @return
	 */
	public AbstractColumn<?> createSubColumn(int[] bounds,
			ArrayList<Integer> nonEmptyCoordinates) {
		
		/* Number of rows in the subColumn */
		int size = bounds[1] - bounds[0] + 1;
		
		/* Create the subColumn */
		AbstractColumn<T> result = createNewInstance();
		
		/* Fill it with empty annotations */
		for(int i = 0 ; i < size ; ++i)
			result.values.add(getEmptyAnnotation());
		
		/* Replace the annotations which must not be empty */
		for(Integer i : nonEmptyCoordinates){
			values.get(i);
			result.values.set(i  - bounds[0], values.get(i));
		}
		
		return result;
	}
	
	public ArrayList<T> getValues(){
		return values;
	}
	
	@XmlElementWrapper
	@XmlElement (name = "column_values")
	public void setValues(ArrayList<T> values){this.values = values;}
	

}
