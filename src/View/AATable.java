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

package View;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import model.AAColumnFormat.ColumnType;
import model.AnnotatedArray;
import model.Corpus;
import model.Pattern;
import model.PointXMLSerializable;



@SuppressWarnings("serial")
public class AATable extends JTable implements ComponentListener{
	
	private int jspWidth;
	private JScrollPane jsp_parent;
		
	private AATableModel aaTableModel = null;
	
	private Color oddColor = new Color(255, 255, 255);
	private Color evenColor = new Color(232, 231, 248);
	private Color patternColor = new Color(237, 174, 174);
	private Color selectedColor = new Color(208, 208, 237);
	private Color selectedPatternColor = new Color(160, 160, 236);
	private Color tableColor;
	
	public AATable(){
		
		super();
		this.setEnabled(false);
    
		this.setModel(new DefaultTableModel());
		jsp_parent = new JScrollPane(this);
		
		jsp_parent.addComponentListener(this);
		jsp_parent.getVerticalScrollBar().setUnitIncrement(10);
		jsp_parent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp_parent.getViewport().setBackground(new Color(238, 238, 238));
		this.setShowGrid(false);
		this.setIntercellSpacing(new Dimension(0,0));
		
	}
	
	public void setColor(Color c){
		tableColor = c;
	}
	
	/**
	 * Set the size of each column according to their content (columns with longer content will be wider)
	 * @return True if the size of all the columns fill the available area; false otherwise
	 */
	private boolean setColumnsSize(){
		
		boolean isTableFull = true;

		if(aaTableModel != null){

			int nbOfColumns = aaTableModel.getColumnCount();

			int[] columnSize = new int[nbOfColumns];
			ArrayList<Integer> maxColSize = new ArrayList<Integer>();
			int maxSize = 0;
			int maxSize2;
			int gap = 7;
			
	        FontMetrics fm = this.getFontMetrics(this.getFont());
			FontMetrics fm_bold = new AATableRenderer().getFontMetrics(this.getFont().deriveFont(Font.BOLD, 11));
				
			/* For each column */
			for(int i = 0 ; i < aaTableModel.getColumnCount() ; i++){
			
				maxSize2 = 0;
				
				/* For each of the 100 first elements of the column */
				for(int j = 0 ; j < Math.min(aaTableModel.getRowCount(), 100) ; j++){
	
					int size;
					
					AATableModel.AACellModel cell = (AATableModel.AACellModel)(aaTableModel.getValueAt(j, i));
				
					if(cell.isPatternCell())
						size = fm_bold.stringWidth(cell.getData().toString());
					else
						size = fm.stringWidth(cell.getData().toString());
					
					if(size > maxSize2){				
						maxSize2 = size;
						if(size > maxSize)
							maxSize = size;
					}
				}
				
				maxColSize.add(maxSize2 + gap);
				
			}
						
			int currentSize = 0;
			

			int scrollbarWidth = 22;
			int width = this.getJSP().getWidth() - scrollbarWidth;// - (gap * (nbOfColumns + 1));// - 3 ;
			int i = 0;
			Integer min = Collections.min(maxColSize);
			
			/* While all the columns have not their size assigned and while each the remaining columns can have size min */
			while( i < nbOfColumns && min * (nbOfColumns - i) + currentSize <= width){
				
				/* Assign the size of the smallest column */
				int id = maxColSize.indexOf(min);
				columnSize[id] = min;
				
				maxColSize.set(id, Integer.MAX_VALUE);
				currentSize += min;
				
				i++;
				min = Collections.min(maxColSize);
								
			}			
	
			/* The columns which have not be assigned (i.e., the larges columns) are to large. 
			 * Give them a size equal to */
			if(i < nbOfColumns){
				
				/*  Size of the remaining columns */
				int newSize = (width - currentSize) / (nbOfColumns - i);
					
				/* For all columns */
				for(int j = 0 ; j < nbOfColumns ; j++){
					
					/* If it has not been assigned */
					if(maxColSize.get(j) != Integer.MAX_VALUE){
						int v = Math.max(newSize, 3*gap);
						columnSize[j] = v;
						currentSize += v;
					}
				}
				
				
			}

//			/* If there is some space available, remove it from the size */
//			else
//				potentialMinus = width - currentSize;
			
//			/* If there is some space available, put it in the last column */
//			else
//				columnSize[columnSize.length-1] += width - currentSize;
			
			else
				isTableFull = false;
			
						
			/* Set the size of each column */
			for(int j = 0 ; j < nbOfColumns ; j++){
	
				TableColumn column = this.getColumnModel().getColumn(j);
				column.setPreferredWidth(columnSize[j]);
				
			}
	
//			this.getJSP().setPreferredSize(
//			    new Dimension(currentSize + scrollbarWidth - 5,getRowHeight()*this.getRowCount()+5) );
			
//			this.getJSP().setMaximumSize(new Dimension(currentSize + scrollbarWidth - 5,getRowHeight()*this.getRowCount()+5) );
			
			jspWidth = this.getJSP().getWidth();
		}
		
		return isTableFull;
	}

	
	/**
	 * Set the Pattern that the table represents
	 * @param aa The Pattern
	 * @return True if the width of the table fill the available space
	 */
	public boolean setPattern(Pattern p){
		
		if(aaTableModel == null){
			aaTableModel = new AATableModel(p);
			this.setModel(aaTableModel);
			aaTableModel.fireTableDataChanged();
			this.reinitialise();
		}
		else{
			int oldColNb = aaTableModel.getColumnCount();		
			aaTableModel.setPattern(p);	
			aaTableModel.fireTableDataChanged();
	
			if(oldColNb != aaTableModel.getColumnCount()){
				aaTableModel.fireTableStructureChanged();
				this.reinitialise();
			}
		}

		boolean isAreaFilled = this.setColumnsSize();
		aaTableModel.computeDisplayedValue();

		int rowMin = p.getCoordinates().get(0).getX();
		
		for(PointXMLSerializable c : p.getCoordinates())			    			
			if(c.getX() < rowMin)
				rowMin = c.getX();

		scrollToVisible(getRowCount()-1, 0);
		scrollToVisible(rowMin-2, 0);  

		this.getTableHeader().setDefaultRenderer(new ColoredHeaderRenderer());
		this.getTableHeader().setReorderingAllowed(false);
		
		return isAreaFilled;
	}
	
	/**
	 * Set the AnnotatedArray that the table represents
	 * @param aa The AnnotatedArray
	 * @return True if the width of the table fill the available space
	 */
	public boolean setAA(AnnotatedArray aa){
		
		if(aaTableModel == null){
			aaTableModel = new AATableModel(aa);
			this.setModel(aaTableModel);
			aaTableModel.fireTableDataChanged();
			this.reinitialise();
		}
		else{
			int oldColNb = this.getColumnCount();
			aaTableModel.setAA(aa);	
			aaTableModel.computeDisplayedValue();
			aaTableModel.fireTableDataChanged();
			
			if(oldColNb != this.getColumnCount()){
				aaTableModel.fireTableStructureChanged();
				this.reinitialise();
			}
		}

		boolean isAreaFilled = this.setColumnsSize();
		

		this.getTableHeader().setDefaultRenderer(new ColoredHeaderRenderer());
		this.getTableHeader().setReorderingAllowed(false);
		
		return isAreaFilled;

	}

	public void scrollToVisible(int row, int col){
		this.scrollRectToVisible(this.getCellRect(row, col, true));
	}

	
	/*
	 * Resize all row and columns and set new renderer to each column
	 */
	public void reinitialise(){

		this.setFillsViewportHeight(true);
		
//		this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		AATableRenderer rend = new AATableRenderer();

		/* Set the renderer of each column */
		for(int i = 0 ; i < aaTableModel.getColumnCount() ; i++){
			TableColumn column = this.getColumnModel().getColumn(i);
			column.setCellRenderer(rend);
			
		}
		
		if(tableColor != null)
			jsp_parent.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, tableColor));
		
	}
	
	public JScrollPane getJSP(){
		return jsp_parent;
	}
	
    public void emptyTable(){
    	
    	this.setModel(new DefaultTableModel());
    	aaTableModel = null;
    	jsp_parent.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
    	jsp_parent.getViewport().setBackground(new Color(238,238,238));

    }
    
    public Pattern getPattern(){
    	if(aaTableModel != null){
    		return aaTableModel.getPattern();
    	}
    	else
    		return null;
    }
    
    
    
    class AATableModel extends AbstractTableModel {
    	
    	
    	private AnnotatedArray rowData;
    	private ArrayList<Integer> annotationColumnsIndex;
    	private ArrayList<Integer> commentColumnsIndex;
    	private AACellModel[][] cellData;
    	private Pattern pattern = null;
    	private String[] columnNames;
    	private int nbOfAnnotationColumns;
    	private int nbOfCommentsColumns;
    	
    	public AATableModel(AnnotatedArray aa){
    		setAA(aa);
    	}

    	public AATableModel(Pattern p){
    		setPattern(p);
    	}
    	
    	public int getColumnCount(){ 		
    		return nbOfAnnotationColumns + nbOfCommentsColumns;
    	}
    	
    	public Object getValueAt(int row, int col){ 		
			return cellData[row][col];
		}
    	
    	public String getColumnName(int c){
    		return columnNames[c];
    	}
    	
    	public int getRowCount(){  		
    		return rowData.getNumberOfLines();
    	}
    	
    	public boolean isCellEditable(int row, int col){ 
    		return false; 
    	}
    	
    	public void setAA(AnnotatedArray aa){

//    		DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();

    		int headerHeight = 10;
    		if(Corpus.getCorpus().isColumnHeaderDefined())
    			headerHeight = 20;
    	     AATable.this.getTableHeader().setPreferredSize(new Dimension(jsp_parent.getWidth(),headerHeight));
    		
    	      
    		this.annotationColumnsIndex = new ArrayList<>();
    		this.commentColumnsIndex = new ArrayList<>();
    		
    		for(int i = 0 ; i < Corpus.getCorpus().getTotalNumberOfColumns() ; ++i)
    			if(!Corpus.getCorpus().getColumnType(i).equals(ColumnType.COMMENT))
    				annotationColumnsIndex.add(i);
    			else
    				commentColumnsIndex.add(i);
    		
    		int newNbOfAnnotationColumns = aa.getNumberOfAnnotationColumns();
    		int newNbOfCommentsColumns = aa.getNumberOfCommentColumns();
    		
    		/* Compute the new <cellData> */
    		AACellModel[][] newCellData = new AACellModel[aa.getNumberOfLines()][newNbOfAnnotationColumns + newNbOfCommentsColumns];
    		
    		for(int i = 0 ; i < aa.getNumberOfLines() ; i++){
    			
    			for(Integer j : commentColumnsIndex){
//    			for(int j = 0 ; j < newNbOfCommentsColumns ; j ++){
    				
//    				try {
    					newCellData[i][j] = new AACellModel(aa.getAnnotation(i, j), false);
//					} catch (InvalidAnnotationIndex e) {
//						newCellData[i][j] = new AACellModel(" ", false);
//					}
    				
    				if(i%2 == 0)
    					newCellData[i][j].setColor(evenColor);
    				else
    					newCellData[i][j].setColor(new Color(255,255,254));
    				
    			}
    			
    			for(Integer j : annotationColumnsIndex){
//    			for(int j = 0 ; j < newNbOfAnnotationColumns ; j++){

//    				try {
    				newCellData[i][j] = new AACellModel(aa.getAnnotation(i, j), true);
//					newCellData[i][j + newNbOfCommentsColumns] = new AACellModel(aa.getAnnotation(i, j + newNbOfCommentsColumns), true);
//					} catch (InvalidAnnotationIndex e) {
//						newCellData[i][j + newNbOfCommentsColumns] = new AACellModel(" ", true);
//					}  	
    				
    				if(i%2 == 0)
    					newCellData[i][j].setColor(evenColor);	
    				else
    					newCellData[i][j].setColor(new Color(255,255,254));		
//    					newCellData[i][j + newNbOfCommentsColumns].setColor(evenColor);			
    			}
    			
    		}
    		
    		pattern = null;
    		
    		rowData = aa;
    		cellData = newCellData;

    		nbOfAnnotationColumns = aa.getNumberOfAnnotationColumns();
    		nbOfCommentsColumns = aa.getNumberOfCommentColumns();

    		columnNames = new String[nbOfAnnotationColumns + nbOfCommentsColumns];
    		
    		if(Corpus.getCorpus().isColumnHeaderDefined())
    			for(int i = 0 ; i< nbOfAnnotationColumns + nbOfCommentsColumns ; ++i)
    				columnNames[i] = Corpus.getCorpus().getColumnHeader(i);
 //   		else
 //  			for(int i = 0 ; i< nbOfAnnotationColumns + nbOfCommentsColumns ; ++i)
 //   				columnNames[i] = Corpus.getCorpus().getColumnType(i).getName();

    		
    	}
    	
    	public void setPattern(Pattern p){
    		
    		setAA(p.getOriginalAA());
    		pattern = p;


    		int headerHeight = 10;
    		if(Corpus.getCorpus().isColumnHeaderDefined())
    			headerHeight = 20;
    	    
    		AATable.this.getTableHeader().setPreferredSize(new Dimension(jsp_parent.getWidth(),headerHeight));
    	    
    		for(PointXMLSerializable c : p.getCoordinates()){

    			cellData[c.getX()][annotationColumnsIndex.get(c.getY())].isPatternCell(true);
    			cellData[c.getX()][annotationColumnsIndex.get(c.getY())].setColor(patternColor);
    			
	
    			/* Specify that the comments on the same line are in a pattern */
    			for(int i = 0 ; i < getColumnCount() ; i++){
    				
    				/* If the cell is a comment */
    				if(!cellData[c.getX()][i].isAnnotation())
    					cellData[c.getX()][i].isPatternCell(true);
    				
    			}
    		} 		
    	}
    	
    	public Pattern getPattern(){
    		return pattern;
    	}
    	
    	/**
    	 * For each cell, if the displayed data is to large, put it on several lines
    	 */
    	public void computeDisplayedValue(){
    		
//	        FontMetrics fm = AATable.this.getFontMetrics(AATable.this.getFont());
	        
	        /* For each line */
    		for(int i = 0 ; i < cellData.length ; i++){
    			
    			/* For each column */
    			for(int j = 0 ; j < cellData[0].length ; j++){
    				
    				AACellModel cell = cellData[i][j];
	    			
//			        ArrayList<String> cellValueDividedByLines = new ArrayList<String>();
//		        	
//			        /* Index of the first char of v which have not been added into celleValueDividedByLines */
//			        int lastChar = 0;
//		
//			        /* While all the cell's string is not divided into rows of the proper size */
//			        while(lastChar < cell.getData().length()-1){
//		
//			        	/* If the remaining substring is larger than the annotations column size */
//			        	if(fm.stringWidth(cell.getData().substring(lastChar)) > columnSize[j]){
//			        		
//			        		int min = lastChar;
//			        		int max = cell.getData().length();
//			        			
//			        		/* Find the biggest indice <min> such that fm.stringWidth(v.substring(lastChar + 1, min)) < columnSize */ 
//			        		while(min < max){
//			        			int consideredBound = (max-min)/2+min;        			
//			        			if(fm.stringWidth(cell.getData().substring(lastChar, consideredBound)) < columnSize[j])
//			        				min = consideredBound+1;
//			        			else
//			        				max = consideredBound-1;
//			        		}
//			        		
//			        		boolean spaceFound = false;
//			        		int iSpace = min-1;
//			        		
//			        		while(!spaceFound && iSpace > lastChar ){
//			        			if(!" ".equals(cell.getData().substring(iSpace-1, iSpace)))
//			        				iSpace--;
//			        			else
//			        				spaceFound = true;
//			        		}
//			        		
//			        		if(spaceFound){
//			        			cellValueDividedByLines.add(cell.getData().substring(lastChar, iSpace));
//			        			lastChar = iSpace;
//			        		}
//			        		else{
//			        			cellValueDividedByLines.add(cell.getData().substring(lastChar, min));
//			        			lastChar = min;
//			        		}      		
//			        			
//			        	}
//			        	else{
//			        		cellValueDividedByLines.add(cell.getData().substring(lastChar));
//			        		lastChar = cell.getData().length() - 1;
//			        	}
//			        }
//				        
//			        /* If the cell is not empty */
//			        if(cellValueDividedByLines.size() > 0){
//			        
//			        	StringBuffer buff = new StringBuffer();
//			        	
//			        	buff.append("<html><body>");
//			        
//				        for(int k = 0 ; k < cellValueDividedByLines.size() - 1 ; k++){
//				        	buff.append(cellValueDividedByLines.get(k));
//				        	buff.append("<br>");
//				        }
//			        
//				        buff.append(cellValueDividedByLines.get(cellValueDividedByLines.size() - 1));
//				        buff.append("</body></html>");
//				        
//				        /* If the current height of the cell is not big enough to display this cell */
//				        if(cellValueDividedByLines.size() > cell.getRowCount()) {
//				        	for(int l = 0 ; l < cellData[i].length ; l++){
//				        		cellData[i][l].setRowCount(cellValueDividedByLines.size());
//				        	}	
//				        }
//				        
//				        cell.setDisplayedData(buff.toString());
    					
//			        }
    				
    				cell.setDisplayedData(cell.getData());
    				
    			}
    		}
    	}
    	    	
    	public class AACellModel{
    		
    		private String data;
    		private String displayedData;
    		private boolean isPatternCell = false;
    		private boolean isAnnotation;
    		private int rowHeight = 16;
    		private Color color = oddColor;
    		
    		public AACellModel(String data, boolean isAnnotation){
    			this.data = data;
    			this.displayedData = data;
    			this.isAnnotation = isAnnotation;
    			
    		}
    		
    		public String getData(){
    			return data;
    		}
    		
    		public boolean isPatternCell(){
    			return isPatternCell;
    		}
    		
    		public boolean isAnnotation(){
    			return isAnnotation;
    		}
    		
    		public void isPatternCell(boolean isPatternCell){
    			this.isPatternCell = isPatternCell;
    		}
    		
    		public void setDisplayedData(String s){
    			displayedData = s;
    		}
    		
    		public int getRowHeight(){
    			return rowHeight;
    		}
    		
    		public void setColor(Color c){
    			color = c;
    		}
    		
    		public Color getColor(){
    			return color;
    		}
    		
    		public String getDisplayedData(){
    			return displayedData;
    		}
    	}
    	
    	
    }
    

    private class AATableRenderer extends DefaultTableCellRenderer { 
    	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
    		
    		if(value instanceof AATableModel.AACellModel){
    			
    			AATableModel.AACellModel cell = (AATableModel.AACellModel)value;
    			    		
	    		String valueDisplayed = cell.getDisplayedData();
	
				if(getJSP().getWidth() != jspWidth)
					setColumnsSize();
				
	    		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
	    		    
		        /* If it's an annotation */
	    		if(cell.isAnnotation()){
	    			
//	    			if(column == aaTableModel.getColumnCount())
	    				this.setHorizontalAlignment(JLabel.CENTER);
//	    			else
//	    				this.setHorizontalAlignment(JLabel.LEFT);
		    		
					/* If the current annotation is in the pattern */
					if(cell.isPatternCell()){
						this.setFont(this.getFont().deriveFont(Font.BOLD, 11));
					}
	    		}
	    		
	    		/* If it's a comment */
	    		else{

	        		this.setHorizontalAlignment(JLabel.LEFT); 
	        		
	    			/* If we represent a Pattern in an AnnotatedArray */
		    		if(cell.isPatternCell())
		    			this.setFont(this.getFont().deriveFont(Font.BOLD, 11));
	    		}
	    		
	    		/* Set the background */
	    		if(isSelected) {
	    			if(cell.isPatternCell() && cell.isAnnotation())
	    				this.setBackground(selectedPatternColor);
	    			else
	    				this.setBackground(selectedColor);
	    		}
	    		else
	    			this.setBackground(cell.getColor());

	    		if(AATable.this.getRowHeight(row) != cell.getRowHeight())
	    			AATable.this.setRowHeight(row, cell.getRowHeight());
				
				setText(valueDisplayed);
		    	
	    	} 
	    	return this; 
    		
    	}
    	 
    }


	@Override
	public void componentHidden(ComponentEvent arg0) {		
	}

	@Override
	/**
	 * Used because the <componentResized> is not called when the window is maximised
	 */
	public void componentMoved(ComponentEvent arg0) {
		if(aaTableModel != null){
			setColumnsSize();
			aaTableModel.computeDisplayedValue();
		}
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		
		if(aaTableModel != null){
			setColumnsSize();
			aaTableModel.computeDisplayedValue();
		}
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}
	
	public AnnotatedArray getAA(){
		
		if(aaTableModel != null)
			return aaTableModel.rowData;
		else
			return null;
	}
	
	public class ColoredHeaderRenderer extends JLabel implements TableCellRenderer {
		
		public ColoredHeaderRenderer(){
			this.setOpaque(true);
			setBorder(BorderFactory.createEtchedBorder());
		}
		
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {
	 
	    	this.setBackground(Corpus.getCorpus().getColumnType(column).getColor());
	    	
	    	if(value != null){
		    	this.setText(value.toString());
		    	this.setName(value.toString());
	    		this.setHorizontalAlignment(JLabel.CENTER);
	    	}

//	    	System.out.println(UIManager.getLookAndFeel().getDefaults().get("ScrollBar.minimumThumbSize");
//			UIManager.getLookAndFeel().getDefaults().put("ScrollBar.minimumThumbSize", new Dimension(1, 1));

	        return this;
	    }
	 
	}
    
}

