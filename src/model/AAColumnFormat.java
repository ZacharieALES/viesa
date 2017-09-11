//Copyright (C) 2012 Zacharie ALES and Rick MORITZ
//
//This file is part of Viesa.
//
//Viesa is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Viesa is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with Viesa.  If not, see <http://www.gnu.org/licenses/>.

package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import extraction.PositiveScoreTable;


public class AAColumnFormat {
	
	public enum ColumnType{
		COMMENT("Comment", new Color(248,248,209)),
		ANNOTATION("Annotation", new Color(237, 174, 174)),
		NUMERICAL_ANNOTATION("Numerical", new Color(160, 160, 236)),
		NONE("Unused", new Color(0,0,0))
		;
		private String name;
		private Color color;
		ColumnType(String name, Color color){
			this.name = name;
			this.color = color;
		}
		
		public String getName(){
			return name;
		}
		
		public Color getColor(){
			return color;
		}
		
		public ColumnType getNext() {
		    ColumnType[] e = ColumnType.values();
		    int i = 0;
		    for (; e[i] != this; i++)
		        ;
		    i++;
		    i %= e.length;
		    return e[i];
		}
		
		public ColumnType getPrevious() {
		    ColumnType[] e = ColumnType.values();
		    int i = 0;
		    for (; e[i] != this; i++)
		        ;
		    i--;
		    
		    if(i == -1)
		    	i = e.length - 1;
		    
		    i %= e.length;
		    return e[i];
		}
	}
	
	private ArrayList<PositionedColumn> format;

	private ArrayList<String> columnHeader;
	
	public AAColumnFormat(ArrayList<PositionedColumn> columns) throws InvalidArgumentsToCreateAnAAColumnFormat{
		
		initialize(columns);
		
	}
	
	public AAColumnFormat(ArrayList<Integer> commentColumns, ArrayList<Integer> annotationColumns,
			ArrayList<Integer> numericalColumns) throws InvalidArgumentsToCreateAnAAColumnFormat{
		
		ArrayList<PositionedColumn> al_pc = new ArrayList<PositionedColumn>();

		if (commentColumns != null)
			for (Integer i : commentColumns)
				al_pc.add(new PositionedColumn(new CommentColumn(), i));

		if (annotationColumns != null)
			for (Integer i : annotationColumns)
				al_pc.add(new PositionedColumn(new AnnotationColumn(), i));

		if (numericalColumns != null)
			for (Integer i : numericalColumns)
				al_pc.add(new PositionedColumn(new NumericalColumn(), i));

		initialize(al_pc);
		
	}
	
	private void initialize(ArrayList<PositionedColumn> columns) throws InvalidArgumentsToCreateAnAAColumnFormat{
		
		/* Check that <columns> contains at least one column which is not a comment column */
		boolean is_valid = false;
		int i = 0;
		
		while(!is_valid && i < columns.size()){
			
			if(!(columns.get(i).column instanceof CommentColumn))
				is_valid = true;
			
			++i;
		}
		
		if(!is_valid)
			throw new InvalidArgumentsToCreateAnAAColumnFormat("The format should at least contain one column which is not an comment column");
			
		
		/* Add all the columns in a tree to sort them according to their position */
		TreeSet<PositionedColumn> tree = new TreeSet<PositionedColumn>(new Comparator<PositionedColumn>(){
			
			@Override
			public int compare(PositionedColumn p1, PositionedColumn p2){
				return p1.position - p2.position;
			}
		});
		
		for(PositionedColumn p : columns)
			tree.add(p);
		 
		/* Check that each column has a different position */ 
		if(tree.size() != columns.size())
			throw new InvalidArgumentsToCreateAnAAColumnFormat("At least two columns have the same position in the input file.");
			
		format = new ArrayList<PositionedColumn>(tree);
		
	}
	
	public boolean areCompatible(AAColumnFormat aacf){
		return format.equals(aacf.format);
	}
	
	public AbstractColumn<?> getColumn(int i){
		return format.get(i).column;
	}
	
	public PositionedColumn getPositionedColumn(int i){
		return format.get(i);
	}
	
	public int getPositionOfColumnIInInputFile(int i){
		return format.get(i).position;
	}
	
	public boolean containColumnsOfType(ColumnType c){
		boolean found = false;
		
		int i = 0;
		
		while(!found && i < format.size()){
			
			if(format.get(i).column.isFromType(c)){
				found = true;
			}
			
			++i;
		}
		
		return found;
		
	}
	
	public boolean containAnyKindOfAnnotationColumns(){
		boolean found = false;
		
		int i = 0;
		
		while(!found && i < format.size()){
			
			if(!format.get(i).column.isCommentColumn()){
				found = true;
			}
			
			++i;
		}
		
		return found;
		
	}
	
	public int numberOfAnnotationColumns(){
		return format.size();
	}
	
	public int getTotalNumberOfColumns(){
		return format.size();
	}	
	
	public ArrayList<AbstractColumn<?>> createAllEmptyColumns(){
		
		ArrayList<AbstractColumn<?>> result = new ArrayList<AbstractColumn<?>>();
		
		for(PositionedColumn ct : format)
			result.add(ct.column.createNewInstance());
		
		return result;
	}
	
	public ArrayList<String> getColumnHeader(){
		return columnHeader;
	}
	
	public void setColumnHeader(ArrayList<String> ch){
		columnHeader = ch;
	}
}
