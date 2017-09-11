package model;

import java.io.Serializable;
import java.util.ArrayList;

import model.AAColumnFormat.ColumnType;

/**
 * Column which contains numerical values. The similarity of two elements a and b in such a column is computed thanks to Math.max(0, maxSim-Math.abs(a-b))
 * i.e.,
 * 		- if |a-b| > maxSim : sim(a,b) = 0
 * 		- if |a-b| < maxSim : sim(a,b) = maxDist - |a-b|
 * @author zach
 *
 */
public class NumericalColumn extends AbstractColumn<Double> implements Serializable{
	
	private static final long serialVersionUID = 7343133127344878290L;
	/** Maximal similarity between two elements (the similarity between two elements a and b in a column of type NumericalColumn is equal to maxDist - |a-b| **/
	public static Double maxSim = null;
	
	public NumericalColumn(){}
	
	public NumericalColumn(ArrayList<Double> values){
		this.values = values;
	}

	@Override
	public double sim(int a, Double b) {
		if(values.get(a) != getEmptyAnnotation() && !b.equals(getEmptyAnnotation())){
			return Math.max(0.0, maxSim-Math.abs(values.get(a)-b));
		}
		else
			return 0.0;
	}

	@Override
	public void addElement(String s) {
		try{
		values.add(Double.parseDouble(s));
		}catch(NumberFormatException e){
			System.err.println("Numerical ColumnError while parsing the cell \"" + s + "\" into a double. The value -Double.MAX_VALUES is added instead");
			
			e.printStackTrace();
			values.add(-Double.MAX_VALUE);
		}
	}

	@Override
	public String toString(int j) {
		return values.get(j).toString();
	}

	@Override
	public Double getEmptyAnnotation() {
		return -Double.MAX_VALUE;
	}

	@Override
	public AbstractColumn<Double> createNewInstance() {
		return new NumericalColumn();
	}

	@Override
	public boolean isCommentColumn() {
		return false;
	}

	@Override
	public boolean isFromType(ColumnType t) {
		return t == ColumnType.NUMERICAL_ANNOTATION;
	}

	@Override
	public ColumnType getType() {
		return ColumnType.NUMERICAL_ANNOTATION;
	}	
	

}
