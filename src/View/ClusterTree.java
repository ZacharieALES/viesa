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
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import clustering.Cluster;
import clustering.ClusterSet;
import model.Corpus;
import model.Pattern;



@SuppressWarnings("serial")
public class ClusterTree extends JTree{
	
	private JScrollPane jsp_parent;
	static DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
	private DefaultTreeModel clusterTM;
    
	/**
	 * Contain the row that must be colored to a given color.
	 * The colored rows correspond to patterns which are displayed in a table.
	 * Each table has its color so when adding a new row to color we check if any row already has this color. If yes, we remove it.
	 */
	private List<RowToColor> rowToColor = new ArrayList<>();
	
	public ClusterTree(){
		
		super(top);
		
		jsp_parent = new JScrollPane(this);
		jsp_parent.getVerticalScrollBar().setUnitIncrement(10);

		/* Used to avoid displaying blue background when selecting a line */
	    this.setOpaque(true);
	    this.setBackground(Color.white);
	    UIDefaults paneDefaults = new UIDefaults();
	    paneDefaults.put("Tree.selectionBackground",null);
	    
//	    this.putClientProperty("Nimbus.Overrides",paneDefaults);
//	    this.putClientProperty("Nimbus.Overrides.InheritDefaults",false);
		
	    
		this.expandPath(this.getPathForRow(0));
		this.setCellRenderer(new ClusterTreeCellRenderer());
		this.setRootVisible(false);
		/* End: Used to avoid displaying blue background when selecting a line */
		
		clusterTM = new DefaultTreeModel(top);
		this.setModel(clusterTM);

	}
	
	public void reinitialize(){
		this.removeAll();
		this.setModel(null);
		
		top = new DefaultMutableTreeNode("Root");
		clusterTM = new DefaultTreeModel(top);
		this.setModel(clusterTM);
		
//		this.updateUI();
	}
	
	public void setClusterSet(ClusterSet c){

		this.reinitialize();

		for(int i = 0 ; i < c.size() ; i++){
			
			int k = i+1;
			Cluster cluster = c.get(i);

			if(c.representsExtractedAlignments)
				cluster.setName(" Alignment n°" + k);
			else
				cluster.setName(" Cluster n°" + k);

			DefaultMutableTreeNode clusterNode = new DefaultMutableTreeNode(c.get(i));
			
			for(int j = 0 ; j < c.get(i).size() ; j++){
				Pattern p = c.get(i).get(j);	
				clusterNode.add(new DefaultMutableTreeNode(p));
			}
			
			top.add(clusterNode);
			
		}
		
		expandAll(true);
		this.updateUI();
	}
	
	public JScrollPane getJSP(){
		return jsp_parent;
	}
	
	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public void expandAll(boolean expand) {
	    TreeNode root = (TreeNode)this.getModel().getRoot();

	    // Traverse tree from root
	    expandAll(new TreePath(root), expand);
	}
	
	private void expandAll(TreePath parent, boolean expand) {
		
	    // Traverse children
	    TreeNode node = (TreeNode)parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
	        for (@SuppressWarnings("rawtypes")
			Enumeration e=node.children(); e.hasMoreElements(); ) {
	            TreeNode n = (TreeNode)e.nextElement();
	            TreePath path = parent.pathByAddingChild(n);
	            expandAll(path, expand);
	        }
	    }

	    // Expansion or collapse must be done bottom-up
	    if (expand) {
	        this.expandPath(parent);
	    } else {
	        this.collapsePath(parent);
	    }
	}
	
	public class ClusterTreeCellRenderer extends DefaultTreeCellRenderer{
		
		   @Override
		   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
		         boolean expanded, boolean leaf, int row, boolean hasFocus) {
		        
			   if(value instanceof DefaultMutableTreeNode){
			 
				   DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

				   if(node.getUserObject() instanceof Pattern){
				   
					   Pattern p = (Pattern)node.getUserObject();
					   StringBuffer buff = new StringBuffer();

					   buff.append(" (id ");
					   buff.append(p.getIndex());
					   
					   if(Corpus.getCorpus().computeClosePatterns)
						   buff.append(" - freq " + p.closePatterns);
					   
					   buff.append(") ");
					   buff.append(p.getOriginalAA().getFileName());
			   
					   JLabel l = (JLabel)super.getTreeCellRendererComponent(tree, buff.toString(), selected, expanded, leaf, row,
					            hasFocus);
					   
						RowToColor toColor = mustBeColored(row); 
						if(toColor != null){

							l.setBackground(toColor.background);
							l.setForeground(new Color(255, 255, 255));
							l.setOpaque(true);

//							  if (selected)
//							    {
//							      super.setBackground(Color.white);
//							      setForeground(Color.white);
//
//							      if (hasFocus)
//							        setBorderSelectionColor(Color.white);
//							      else
//							        setBorderSelectionColor(Color.white);
//							    }
//
							return l;
						}
						else{
							l.setOpaque(false);
							l.setBorder(BorderFactory.createEmptyBorder());
						}
				   }
				   else if(node.getUserObject() instanceof Cluster){
					   
					   Cluster c = (Cluster)(node.getUserObject());
					   String s = c.getClusterName();
					   if(s == null)
						   s = "Cluster";
					   JLabel l = (JLabel)super.getTreeCellRendererComponent(tree, s, selected, expanded, leaf, row,
					            hasFocus);
					   l.setOpaque(false);
				   }
				   else{
					   JLabel l = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
				            hasFocus);
					   l.setOpaque(false);
				   }
				   
			   }
			   else{
				   JLabel l = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
			            hasFocus);
				   l.setOpaque(false);
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
		
	}

	public void removeSelection() {
		rowToColor = new ArrayList<RowToColor>();
	}

}
