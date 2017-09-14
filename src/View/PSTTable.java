package View;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import main.MainTutorial;
import model.AnnotationColumn;
import model.Corpus;
import tuto.AbstractEditSimilarityStep;
import tuto.AbstractTutoStep;
import tuto.EditSimilarity;
import tuto.EditSimilarityTable;

public class PSTTable extends JTable{

	private static final long serialVersionUID = 1L;
	private JScrollPane jsp_parent;

	PSTTableModel pstTableModel;

	Color cellRemovedColor = new Color(237, 174, 174);
	Color rowModifiedColor = new Color(160, 160, 236);
	public static Color black = new Color(0, 0, 0);

	public List<ArrayList<String>> rowdata;

	public List<String> annotations; 

	public boolean[][] cellModified;

	public List<Integer> annotationIndex;
	PSTEditor pste;

	public PSTTable(List<Integer> annotationIndex, PSTEditor pste){

		super();

		this.pste = pste;
		this.annotationIndex = annotationIndex;

		//		System.out.println("Create table for: " + this.annotationIndex);

		cellModified = new boolean[annotationIndex.size()+1][annotationIndex.size()+1];

		jsp_parent = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp_parent.getVerticalScrollBar().setUnitIncrement(10);


		//		jsp_parent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		pstTableModel = new PSTTableModel(annotationIndex);
		this.setModel(pstTableModel);	

		PSTTableRenderer rend = new PSTTableRenderer();

		int gap = 11;
		int width = this.getJSP().getWidth() - (gap * (this.getColumnCount() + 1));
		int size = Math.max(width/this.getColumnCount(), 4*gap);

		int maxSize = -1;
		int sizeUpperBound = 300;

		for(int i = 1 ; i < this.getColumnCount() ; i++){

			TableColumn column = this.getColumnModel().getColumn(i);

			int columnWidth = Math.min(sizeUpperBound, ((Double)(1.2 * StandardView.getInstance().jf_s.jb_addAA.getGraphics().getFontMetrics().stringWidth(Corpus.getCorpus().getAnnotation(annotationIndex.get(i-1))))).intValue());

			//					System.out.println(Corpus.getCorpus().getAnnotation(annotationIndex.get(i-1)) + " : " + columnWidth);

			if(columnWidth > maxSize)
				maxSize = columnWidth;

			column.setMinWidth(columnWidth);

			column.setCellRenderer(rend);

		}	


		TableColumn column = this.getColumnModel().getColumn(0);
		column.setMinWidth(maxSize);
		column.setCellRenderer(rend);

		//		this.addFocusListener(new FocusListener() {
		//			
		//			@Override
		//			public void focusLost(FocusEvent e) {
		//			}
		//			
		//			@Override
		//			public void focusGained(FocusEvent e) {
		//				final JTextComponent jtc = (JTextComponent)c;
		//				jtc.requestFocus();
		//				//jtc.selectAll();
		//				SwingUtilities.invokeLater(new Runnable()
		//				{
		//				    public void run()
		//				    {
		//				        jtc.selectAll();
		//				    }
		//				});
		//			}
		//		});
		JTableHeader header = getTableHeader();
		header.setDefaultRenderer(new HeaderRenderer(this));


		setShowGrid(false);
		setIntercellSpacing(new Dimension(0,0));
		repaint();


		/* Set the size of the JDialog */
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int screen_height = (int)screenSize.getHeight();
		int screen_width = (int)screenSize.getWidth();

		/* +1 because the first column contains the name of the annotations */
		//		int table_width = Math.min(screen_width,  0 + 85 * (annotationIndex.size()+1));
		int table_height = Math.min(screen_height,21 + 23*(annotationIndex.size()));

		this.setPreferredScrollableViewportSize(this.getPreferredSize());
		this.setFillsViewportHeight(true);

		//		getJSP().setMinimumSize(new Dimension(table_width, table_height));
		//		getJSP().setPreferredSize(new Dimension(table_width, table_height));


	}

	public JScrollPane getJSP(){
		return jsp_parent;
	}


	@SuppressWarnings("serial")
	private class PSTTableModel extends AbstractTableModel{
		private String[] columnNames;

		public PSTTableModel(List<Integer> index){

			rowdata = new ArrayList<ArrayList<String>>();
			ArrayList<String> currentLine;

			/* For each line in the table */
			for(short i = 0 ; i < annotationIndex.size() ; ++i){

				currentLine = new ArrayList<String>();
				currentLine.add(Corpus.getCorpus().getAnnotation(annotationIndex.get(i)));

				/* For each non empty column in this line */
				for(short j = 0; j <= i ; ++j){

					/* If the value associated to annotations <i> and <j> is greater than 0 */
					double v = AnnotationColumn.pst.get(annotationIndex.get(i).shortValue(), annotationIndex.get(j).shortValue());

					if(v > 0.0)
						currentLine.add(((Double)v).toString());
					else
						currentLine.add("-");
				}
				for(int j = i+1 ; j < annotationIndex.size() ; ++j)
					currentLine.add("");
				rowdata.add(currentLine);
			}

			//			/* For each annotation which does not appear in the table */
			//			for(int i = AnnotationColumn.pst.size() ; i < Corpus.getCorpus().getNumberOfAnnotations() ; ++i){
			//
			//				currentLine = new ArrayList<String>();
			//				currentLine.add(Corpus.getCorpus().getAnnotation(i));
			//
			//				/* For each column in the table */
			//				for(int j = 1; j <= i ; ++j)
			//					currentLine.add("-");
			//				for(int j = i+1 ; j < Corpus.getCorpus().getNumberOfAnnotations() ; ++j)
			//					currentLine.add("");
			//				rowdata.add(currentLine);
			//			}

			columnNames = new String[annotationIndex.size() + 1];

			for(int i = 1 ; i < annotationIndex.size() + 1 ; ++i)
				columnNames[i] = Corpus.getCorpus().getAnnotation(annotationIndex.get(i-1));

		}

		@Override
		public int getColumnCount() {
			if(rowdata != null && rowdata.size() > 0)
				return rowdata.get(0).size();
			else
				return 0;
		}

		@Override
		public int getRowCount() {
			return rowdata.size();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			return rowdata.get(arg0).get(arg1);
		}

		public String getColumnName(int c){
			return columnNames[c];
		}

		@Override
		public void setValueAt(Object value, int row, int col) {

			if(col != 0 && !value.equals(rowdata.get(row).get(col))){

				cellModified[row][col] = true;

				String a1 = getModel().getValueAt(row, 0).toString();
				String a2 = getModel().getValueAt(col - 1, 0).toString();

				Double d = null;
				try{
					d = Double.parseDouble((String)value);
				}
				catch(Exception e){}

				//				System.out.println("PSTTable a1/a2/d: " + a1 + "/" + a2 + "/" + d);


				//				if(d != null)
				//					StandardView.getInstance().log("Positive score table temporary modification of sim(" + a1 + "," + a2 + ") from " + rowdata.get(row).get(col) + " to " + value);

				AbstractTutoStep ads = MainTutorial.getCurrentStep();

				/* If it is not the tuto */
				if(!MainTutorial.IS_TUTO 

						/* Or if the current tuto step is null */
						|| ads == null 

						/* Or if the current tuto step is an edition of the table */
						|| (ads instanceof EditSimilarityTable || ads instanceof EditSimilarity)

						/* and it corresponds to the expected edition */
						&& ((AbstractEditSimilarityStep)ads).removeEditOperationIfExpected(a1, a2, d)
						){

					/* Edit the table */
					rowdata.get(row).set(col, value.toString());

					if(MainTutorial.IS_TUTO)
						pste.activateOKButton();


				}

				/* Otherwise put back the previous value */
				else{
					rowdata.get(row).set(col, rowdata.get(row).get(col).toString());

					AbstractEditSimilarityStep est = (AbstractEditSimilarityStep)ads;
					JOptionPane.showMessageDialog(pste,est.errorMessage());
				}


				fireTableCellUpdated(row, col);
			}

		}

		public boolean isCellEditable(int row, int col){
			if(col == 0 || col > row + 1)
				return false;
			else
				return true; 
		}

	}

	@SuppressWarnings("serial")
	private class PSTTableRenderer extends DefaultTableCellRenderer{

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){ 


			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 

			if(column > row + 1)
				this.setBackground(new Color(215, 215, 215));
			else if(cellModified[row][column])
				this.setBackground(rowModifiedColor);
			else if( row % 2 == 0 )  
				this.setBackground(new Color(232, 231, 248));
			else
				this.setBackground(new Color(255,255,255));	

			this.setForeground(black);

			String valueDisplayed;

			/* Bug from java, getTableCellRendererComponent is sometime called with value == null
			 * http://stackoverflow.com/questions/3054775/jtable-strange-behavior-from-getaccessiblechild-method-resulting-in-null-point
			 * https://josm.openstreetmap.de/ticket/6301
			 */
			try{
				valueDisplayed = value.toString();
				if(column > 0)
					this.setHorizontalAlignment(JLabel.CENTER); 
				else
					this.setHorizontalAlignment(JLabel.LEFT); 

				setText(valueDisplayed);
			}catch(NullPointerException e){}

			return this; 

		}

	}

	/**
	 * Class used to center the column headers 
	 * @author zach
	 *
	 */
	private static class HeaderRenderer implements TableCellRenderer {

		DefaultTableCellRenderer renderer;

		public HeaderRenderer(JTable table) {
			renderer = (DefaultTableCellRenderer)
					table.getTableHeader().getDefaultRenderer();
			renderer.setHorizontalAlignment(JLabel.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int col) {
			return renderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, col);
		}
	}

	@Override
	public boolean editCellAt(int row, int column, EventObject e)
	{
		boolean result = super.editCellAt(row, column, e);
		selectAll(e);

		return result;
	}


	/*
	 * Select the text when editing on a text related cell is started
	 */
	private void selectAll(EventObject e)
	{
		final Component editor = getEditorComponent();

		if (editor == null
				|| ! (editor instanceof JTextComponent))
			return;

		if (e == null || e instanceof KeyEvent || e instanceof ActionEvent)
		{
			((JTextComponent)editor).selectAll();
			return;
		}

		if (e instanceof MouseEvent)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					((JTextComponent)editor).selectAll();
				}
			});
		}
	}


}