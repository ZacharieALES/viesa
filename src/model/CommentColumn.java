package model;

import java.io.Serializable;

import model.AAColumnFormat.ColumnType;

public class CommentColumn extends AbstractColumn<String> implements Serializable{

	@Override
	public double sim(int a, String b) {
		return 0;
	}

	@Override
	public void addElement(String s) {
		values.add(s);
	}

	@Override
	public String toString(int j) {
		return values.get(j);
	}

	@Override
	public AbstractColumn<String> createNewInstance() {
		return new CommentColumn();
	}

	@Override
	public String getEmptyAnnotation() {
		return "";
	}

	@Override
	public boolean isCommentColumn() {
		return true;
	}

	@Override
	public boolean isFromType(ColumnType t) {
		return t == ColumnType.COMMENT;
	}

	@Override
	public ColumnType getType() {
		return ColumnType.COMMENT;
	}

}
