package model;

import java.io.Serializable;
import java.util.ArrayList;

import extraction.PositiveScoreTable;
import model.AAColumnFormat.ColumnType;

public class AnnotationColumn extends AbstractColumn<Short> implements Serializable{
	
	private static final long serialVersionUID = -6738436639196250765L;
	/** PositiveScoreTable considered for the element in columns of type AnnotationColumn **/
	public static PositiveScoreTable pst;
	
	public AnnotationColumn(){}
	
	public AnnotationColumn(ArrayList<Short> values){
		this.values = values;
	}
	
	@Override
	public double sim(int a, Short b) {
		try{
			return pst.get(values.get(a), b);
		}
		catch(NullPointerException e){
			System.err.println("The score table has not been initialised.");
			e.printStackTrace();
			return 0.0;
		}
	}

	@Override
	public void addElement(String s) {
		
		/* If the annotation is not in the list of annotations */
		if(Corpus.getCorpus().getAnnotations().indexOf(s) == -1){
			
			// Add the annotation in the corpus list
			Corpus.getCorpus().getAnnotationIndex(s);
//			System.out.println("Message: The annotation '" + s + "' is not contained in the score table");
		}
		
		values.add(Corpus.getCorpus().getAnnotationIndex(s));
	}

	@Override
	public String toString(int j) {
		return Corpus.getCorpus().getAnnotation(values.get(j));
	}

	@Override
	public Short getEmptyAnnotation() {
		return 0;
	}

	@Override
	public AbstractColumn<Short> createNewInstance() {
		return new AnnotationColumn();
	}

	@Override
	public boolean isCommentColumn() {
		return false;
	}

	@Override
	public boolean isFromType(ColumnType t) {
		return t == ColumnType.ANNOTATION;
	}

	@Override
	public ColumnType getType() {
		return ColumnType.ANNOTATION;
	}
	
	

}
