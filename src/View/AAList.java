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
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import model.AnnotatedArray;

@SuppressWarnings("serial")
public class AAList extends JList<AnnotatedArray>{
	
	private JScrollPane jsp_parent;
	
	private Color oddColor = new Color(255, 255, 255);
	private Color evenColor = new Color(232, 231, 248);
	private Color selectedColor = new Color(208, 208, 237);
	private Color blackColor = new Color(0,0,0);
	
	/**
	 * Contain the row that must be colored to a given color.
	 * The colored rows correspond to annotated arrays which are displayed in a table.
	 * Each table has its color so when adding a new row to color we check if any row already has this color. If yes, we remove it.
	 */
	private List<RowToColor> rowToColor = new ArrayList<>();
	
	public AAList(){
		
		jsp_parent = new JScrollPane(this);
		jsp_parent.getVerticalScrollBar().setUnitIncrement(10);

		
		this.setModel(new DefaultListModel<AnnotatedArray>());
		this.setCellRenderer(new AAListCellRenderer());
		
	}
	
	public AAList(List<AnnotatedArray> aa_arg){

		this.setModel(new DefaultListModel<AnnotatedArray>());
		for(AnnotatedArray aa : aa_arg)
			this.addAA(aa);
		
		jsp_parent = new JScrollPane(this);
		jsp_parent.getVerticalScrollBar().setUnitIncrement(10);

		
		this.setCellRenderer(new AAListCellRenderer());
	}
	
	public AnnotatedArray getAA(int id){

		DefaultListModel<AnnotatedArray> aalm = (DefaultListModel<AnnotatedArray>)this.getModel();
		return aalm.elementAt(id);
		
	}
	
	public void addAA(AnnotatedArray aa){
		
		DefaultListModel<AnnotatedArray> aalm = (DefaultListModel<AnnotatedArray>)this.getModel();
		aalm.addElement(aa);		
	}
	
	public void removeAA(int id){
		
		DefaultListModel<AnnotatedArray> aalm = (DefaultListModel<AnnotatedArray>)this.getModel();
		aalm.removeElementAt(id);
	}
	
	public void reinitialize(){

		DefaultListModel<AnnotatedArray> aalm = (DefaultListModel<AnnotatedArray>)this.getModel();
		aalm.removeAllElements();
		
	}
	
	public JScrollPane getJSP(){
		return jsp_parent;
	}
	
	public class AAListCellRenderer extends DefaultListCellRenderer{
		
		@Override
	    public Component getListCellRendererComponent(
	            JList<?> list,
	    	Object value,   // value to display
	    	int index,      // cell index
	    	boolean iss,    // is the cell selected
	    	boolean chf){    // the list and the cell have the focus
	       
	    	if(value instanceof AnnotatedArray)
	    		super.getListCellRendererComponent(list, ((AnnotatedArray)value).getFileName(), index, iss, chf);
	    	else
	    		super.getListCellRendererComponent(list, value, index, iss, chf);

			this.setForeground(blackColor);
	    	
			RowToColor toColor = mustBeColored(index); 
			if(toColor != null){
				this.setBackground(toColor.background);
				this.setForeground(new Color(255, 255, 255));
			}
			else{ 
				if(iss)
					this.setBackground(selectedColor);
		    	else
			        if( index %2 == 0 )
			        	this.setBackground(evenColor);
			        else
			            this.setBackground(oddColor);
			}
	        
	    	return this;
	    }
		
	}
	
	public RowToColor mustBeColored(int row){
		RowToColor result = null;
		int i =0;
		
		while(result == null && i < rowToColor.size()){
			
			if(rowToColor.get(i).row == row)
				result = rowToColor.get(i);
			
			++i;
		}
		
		return result;
		
	}
	
	private void checkRowToColor(int row, Color background){
		
		/* Remove the row to color which correspond to the same row
		 * (since one row can only have one color) */
		for(int i = rowToColor.size() - 1 ; i >= 0 ; --i)
			if(row == rowToColor.get(i).row)
				rowToColor.remove(i);
			
	}
	
	public void checkColorToColor(int row, Color background, boolean repaint){
		
		/* Remove the row to color which correspond to the same color
		 * (since one color corresponds to a table and only one pattern can be displayed in a table) */
		for(int i = rowToColor.size() - 1 ; i >= 0 ; --i)
			if(rowToColor.get(i).background.equals(background)){
				rowToColor.remove(i);
				if(repaint)
					this.repaint();
			}
	}
	
	public void addRowToColor(int row, Color background){
		
		checkRowToColor(row, background);
		checkColorToColor(row, background, false);
			
		rowToColor.add(new RowToColor(row, background));
		this.repaint();
		
	}}
