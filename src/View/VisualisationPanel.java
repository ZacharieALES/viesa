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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import clustering.Cluster;
import clustering.ClusterSet;
import clustering.ClusteringSolution;
import clustering.HardClusteringSolution;
import clustering.HierarchicalClusteringSolution;
import main.MainTutorial;
import model.AnnotatedArray;
import model.Corpus;
import model.Pattern;
import net.miginfocom.swing.MigLayout;
import tuto.ChangeDisplayedAlignment;
import tuto.ChangeDisplayedPattern;
import tuto.ChangeTablesOrientation;
import tuto.FirstExtraction;

//@SuppressWarnings("serial")
public class VisualisationPanel extends JPanel{

	private static final long serialVersionUID = -6955571429335477410L;

	//	private AAList corpusList = new AAList();
	private ClusterTree clusterTree = new ClusterTree();

	private AATable table1 = new AATable();
	private AATable table2 = new AATable();
	private AATable tableA = new AATable();
	private AATable tableB = new AATable();

	private List<Color> tableColor = new ArrayList<>();

	private JSlider slider = null;

	private JLabel jl_o1 = new JLabel(": to use it");
	private JLabel jl_o2 = new JLabel(": to use it");
	private JLabel jl_oA = new JLabel(": to use it");
	private JLabel jl_oB = new JLabel(": to use it");

	public JIconButton jb_switchOrientation12 = new JIconButton("img/horizontal2.png");
	public JIconButton jb_switchOrientationAB = new JIconButton("img/horizontal2.png");

	private JPanel aaTablePanel12 = new JPanel(new MigLayout("fill", "[50%]0[50%]", "0[]0[grow]0"));
	private JPanel aaTablePanelAB = new JPanel(new MigLayout("fill", "[50%]0[50%]", "0[]0[grow]0"));

	private JPanel jp_l1 = new JPanel(new MigLayout("fillx", "[][]push[]", "[]"));
	private JPanel jp_l2 = new JPanel(new MigLayout("fillx", "[][]push", "[]"));
	private JPanel jp_lA = new JPanel(new MigLayout("fillx", "[][][][]push[]", "[]"));
	private JPanel jp_lB = new JPanel(new MigLayout("fillx", "[][][][]push", "[]"));

	private JPanel jp_cluster = new JPanel(new MigLayout("fill", "", "[][][grow]"));

	private TitledBorder tb_table12 = BorderFactory.createTitledBorder("Overview");
	private TitledBorder tb_tableAB = BorderFactory.createTitledBorder("Overview");
	private TitledBorder tb_cluster = BorderFactory.createTitledBorder("Clusters");

	/**
	 * If true, table1 and table2 are displayed horizontally otherwise they are displayed vertically
	 */
	private boolean isVertical12 = false;

	/** Idem with tableA and tableB */
	private boolean isVerticalAB = false;


	private CSComboBox cscb = new CSComboBox();

	private VisualisationPanel(){

		this.setLayout(new MigLayout("fill", "[grow][grow]", "0[40%!][60%!]"));

		//		/* *** Corpus list pane */		
		//		corpusList.getJSP().setMinimumSize(new Dimension(130, 60));
		////		corpusList.getJSP().setPreferredSize(new Dimension(200, 200));
		//		corpusList.addMouseListener(new MouseAdapter(){
		//			
		//			public void mouseReleased(MouseEvent arg0){
		//
		//				int index = corpusList.locationToIndex(arg0.getPoint());
		//				
		//				boolean isLeftClick = SwingUtilities.isLeftMouseButton(arg0);
		//				displayCorpusSelection(index, isLeftClick);
		//				
		//			}	
		//			
		//		});
		//		
		//		/* *** End * Corpus list pane */		

		/* *** Cluster list pane */
		cscb.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e){		
				displayCBSelection();
			}

		});


		//		tableColor.add(new Color(255, 85, 85));
		//		tableColor.add(new Color(33, 103, 120));
		//		tableColor.add(new Color(95, 211, 141));
		//		tableColor.add(new Color(255,153,85));

		tableColor.add(new Color(0,158,206));
		tableColor.add(new Color(156,207,49));
		tableColor.add(new Color(255,158,0));
		tableColor.add(new Color(206,0,0));

		tableA.setColor(tableColor.get(0));
		tableB.setColor(tableColor.get(1));
		table1.setColor(tableColor.get(2));
		table2.setColor(tableColor.get(3));

		tableA.emptyTable();
		tableB.emptyTable();
		table1.emptyTable();
		table2.emptyTable();

		slider = new JSlider(JSlider.HORIZONTAL);

		slider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){

				if(!getSlider().getValueIsAdjusting()){

					if(cscb.getSelectedItem() instanceof HierarchicalClusteringSolution){	

						clusterTree.removeSelection();
						HierarchicalClusteringSolution hcs = (HierarchicalClusteringSolution)cscb.getSelectedItem();

						clusterTree.setClusterSet(hcs.getClusterSet(slider.getValue()));
						tb_cluster.setTitle("Clusters (" + getSelectedClusterSet().size() + ")");

						/* Necessary to update the title */
						jp_cluster.repaint();
					}
				}
			}
		});


		//TODO voir si utile
		FocusListener fl = new FocusListener() {
			@Override public void focusGained(FocusEvent e) {
				e.getComponent().repaint();
			}
			@Override public void focusLost(FocusEvent e) {
				e.getComponent().repaint();
			}
		};

		clusterTree.addFocusListener(fl);
		//	      corpusList.addFocusListener(fl);

		clusterTree.getJSP().setMinimumSize(new Dimension(10, 100));
		//		clusterTree.getJSP().setPreferredSize(new Dimension(300, 200));
		clusterTree.addMouseListener(new MouseAdapter(){

			public void mouseReleased(MouseEvent arg0){

				int index = clusterTree.getRowForLocation(arg0.getX(), arg0.getY());

				/* Left Click */
				if(SwingUtilities.isLeftMouseButton(arg0))
					displayClusterSelection(index, true, arg0);

				/* Right click */
				else
					displayClusterSelection(index, false, arg0);

			}	

		});
		/* *** End * Cluster list pane */



		//		JPanel jp_corpus = new JPanel(new MigLayout("fill"));
		//		jp_corpus.setBorder(tb_corpus);
		//		jp_corpus.add(corpusList.getJSP(), "grow");

		jp_cluster.setBorder(tb_cluster);
		jp_cluster.add(clusterTree.getJSP(), "cell 0 0, spany 3, grow");

		JPanel jp_selection = new JPanel(new MigLayout("fill", "", ""));
		//		jp_selection.add(jp_corpus, "cell 0 0, grow");
		jp_selection.add(jp_cluster, "cell 0 0, spanx 2, grow");


		/* Begin *** adTablePanel */		
		jb_switchOrientationAB.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				isVerticalAB = !isVerticalAB;

				if(isVerticalAB){
					jb_switchOrientationAB.setImage("img/horizontal2.png");
					changeTableOrientation(!isVerticalAB, false);
				}
				else{
					jb_switchOrientationAB.setImage("img/vertical2.png");
					changeTableOrientation(!isVerticalAB, false);
				}
			}
		});

		jb_switchOrientation12.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				isVertical12 = !isVertical12;

				if(isVertical12){
					jb_switchOrientation12.setImage("img/horizontal2.png");
					changeTableOrientation(!isVertical12, true);
				}
				else{
					jb_switchOrientation12.setImage("img/vertical2.png");
					changeTableOrientation(!isVertical12, true);
				}
			}
		});

		JIconButton jib_left12 = new JIconButton("img/mouse_left.png");
		JIconButton jib_right12 = new JIconButton("img/mouse_right.png");
		JIconButton jib_leftAB = new JIconButton("img/mouse_left.png");
		JIconButton jib_rightAB = new JIconButton("img/mouse_right.png");
		JIconButton jib_ctrlA = new JIconButton("img/ctrl.png");
		JIconButton jib_ctrlB = new JIconButton("img/ctrl.png");

		jp_lA.add(jib_ctrlA);
		jp_lA.add(new JLabel(" +"));
		jp_lA.add(jib_leftAB);
		jp_lA.add(jl_oA);
		jp_lA.add(jb_switchOrientationAB);
		//		jp_l1.setMaximumSize(new Dimension(3000, 10));

		jp_lB.add(jib_ctrlB);
		jp_lB.add(new JLabel(" +"));
		jp_lB.add(jib_rightAB);
		jp_lB.add(jl_oB);

		aaTablePanelAB.setBorder(tb_tableAB);

		aaTablePanelAB.add(jp_lA, "spanx 2, growx, wrap");
		aaTablePanelAB.add(tableA.getJSP(), "spanx 3, grow, wrap");
		aaTablePanelAB.add(jp_lB, "spanx 2, growx, wrap");
		aaTablePanelAB.add(tableB.getJSP() ,"spanx 3, grow, wrap");

		jp_l1.add(jib_left12);
		jp_l1.add(jl_o1);
		jp_l1.add(jb_switchOrientation12);
		//		jp_l1.setMaximumSize(new Dimension(3000, 10));

		jp_l2.add(jib_right12);
		jp_l2.add(jl_o2);

		aaTablePanel12.setBorder(tb_table12);


		aaTablePanel12.add(jp_l1, "spanx 2, growx, wrap");
		aaTablePanel12.add(table1.getJSP(), "spanx 3, grow, wrap");
		aaTablePanel12.add(jp_l2, "spanx 2, growx, wrap");
		aaTablePanel12.add(table2.getJSP() ,"spanx 3, grow, wrap");

		this.add(jp_selection, "grow");
		//		this.add(jp_selection, "spany 2, grow");
		this.add(aaTablePanel12, "spany 2, grow, wrap");
		this.add(aaTablePanelAB, "grow, wrap");


	}

	private static class VisualisationPanel2Holder{
		private final static VisualisationPanel instance = new VisualisationPanel();		
	}

	public static VisualisationPanel getInstance(){
		return VisualisationPanel2Holder.instance;
	}

	public void addAA(AnnotatedArray aa){
		//		corpusList.addAA(aa);
		//		tb_corpus.setTitle("Corpus (" + corpusList.getModel().getSize() + ")");
	}

	public void removeAA(int id){
		//
		//		/* If the aa is displayed in one of the table, clear the table */
		//		if(table1.getAA() != null && corpusList.getAA(id).getFileName().equals(table1.getAA().getFileName())){
		//			jl_o1.setText(": to use it");
		//			table1.emptyTable();
		//		}
		//
		//		if(table2.getAA() != null && corpusList.getAA(id).getFileName().equals(table2.getAA().getFileName())){
		//			jl_o2.setText(": to use it");
		//			table2.emptyTable();
		//		}
		//
		//		if(tableA.getAA() != null && corpusList.getAA(id).getFileName().equals(tableA.getAA().getFileName())){
		//			jl_oA.setText(": to use it");
		//			tableA.emptyTable();
		//		}
		//
		//		if(tableB.getAA() != null && corpusList.getAA(id).getFileName().equals(tableB.getAA().getFileName())){
		//			jl_oB.setText(": to use it");
		//			tableB.emptyTable();
		//		}
		//		
		//		corpusList.removeAA(id);
		//
		//		if(corpusList.getModel().getSize() == 0)
		//			tb_corpus.setTitle("Corpus");
	}
	//
	//	public void displayCorpusSelection(int index, boolean isLeftClick){
	//		corpusList.setSelectedIndex(index);
	//		
	//		AnnotatedArray aa = (AnnotatedArray)corpusList.getModel().getElementAt(index);
	//
	//		AnnotatedArray aa1 = table1.getAA();
	//		AnnotatedArray aa2 = table2.getAA();
	//		AnnotatedArray aaA = tableA.getAA();
	//		AnnotatedArray aaB = tableB.getAA();
	//
	//		/* If it was displayed previously in a table, remove it */
	//		if(aa1 == aa){
	//			table1.emptyTable();
	//			jl_o1.setText(": to use it");
	//		}
	//		if(aa2 == aa){
	//			table2.emptyTable();
	//			jl_o2.setText(": to use it");
	//		}
	//		if(aaA == aa){
	//			tableA.emptyTable();
	//			jl_oA.setText(": to use it");
	//		}
	//		if(aaB == aa){
	//			tableB.emptyTable();
	//			jl_oB.setText(": to use it");
	//		}
	//		
	//		if(!StandardView.getInstance().isCtrlPressed()){
	//			tb_table12.setTitle("Overview");
	//			
	//			if(!isLeftClick){
	//				
	//				table2.setAA(aa);
	//				jl_o2.setText(aa.getFileName());
	//				this.corpusList.addRowToColor(index, tableColor.get(3));
	//				this.clusterTree.checkColorToColor(index, tableColor.get(3), true);
	//				
	//			}
	//			else{
	//				
	//				table1.setAA(aa);
	//				jl_o1.setText(aa.getFileName());
	//				this.corpusList.addRowToColor(index, tableColor.get(2));
	//				this.clusterTree.checkColorToColor(index, tableColor.get(2), true);
	//			}
	//		}
	//		else{
	//			tb_tableAB.setTitle("Overview");
	//			
	//			if(!isLeftClick){
	//				tableB.setAA(aa);
	//				jl_oB.setText(aa.getFileName());
	//				this.corpusList.addRowToColor(index, tableColor.get(1) );
	//				this.clusterTree.checkColorToColor(index, tableColor.get(1), true);
	//			}
	//			else{
	//				tableA.setAA(aa);
	//				jl_oA.setText(aa.getFileName());
	//				this.corpusList.addRowToColor(index, tableColor.get(0));
	//				this.clusterTree.checkColorToColor(index, tableColor.get(0), true);
	//			}
	//			
	//		}
	//	}
	//	
	// TODO enable to display previous and next pattern in a table

	public void displayClusterSelection(int index, boolean isLeftClick, MouseEvent arg0){

		/* Enable to select even with a right click */
		clusterTree.setSelectionRow(index);

		if(clusterTree.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode){

			DefaultMutableTreeNode cNode = (DefaultMutableTreeNode)clusterTree.getLastSelectedPathComponent();

			if(cNode.getUserObject() instanceof Pattern){

				/* If it is not the tutorial 
				 * or if it is not the step during which the user must click on an alignment/cluster 
				 * or if the first clusters are automatically displayed
				 */
				if(!MainTutorial.IS_TUTO || !(MainTutorial.getCurrentStep() instanceof ChangeDisplayedAlignment) || isAutomaticallyDisplayingClusters){

					if(MainTutorial.IS_TUTO && MainTutorial.getCurrentStep() instanceof ChangeDisplayedPattern){

						ChangeDisplayedPattern cda = (ChangeDisplayedPattern)MainTutorial.getCurrentStep();

						if(!isAutomaticallyDisplayingClusters)
							if(isLeftClick)
								if(StandardView.getInstance().isCtrlPressed)
									cda.displayedIn3rdTable = true;
								else
									cda.displayedIn1stTable = true;
							else
								if(StandardView.getInstance().isCtrlPressed)
									cda.displayedIn4thTable = true;
								else
									cda.displayedIn2ndTable = true;

						if(cda.isOver())
							MainTutorial.nextStep();

					}					

					Pattern p = (Pattern)cNode.getUserObject();

					StringBuffer labelText = new StringBuffer();

					int id = p.getIndex();

					if(id >= 0){
						labelText.append(" (");
						labelText.append(p.getIndex());
						labelText.append(") ");
					}

					labelText.append(p.getOriginalAA().getFileName());

					JPanel panel;
					JLabel mainLabel;

					/* Table in which the pattern will be displayed */
					AATable mainTable;
					AATable secondaryTable;

					Pattern mainPattern;
					Pattern secondaryPattern;

					JIconButton jbMainSwitchButton;
					boolean isMainSwitchVertical;

					int indexInRowToColor = -1;

					if(!StandardView.getInstance().isCtrlPressed()){

						panel = aaTablePanel12;
						jbMainSwitchButton = jb_switchOrientation12;
						isMainSwitchVertical = isVertical12;

						if(isLeftClick){
							mainLabel = jl_o1;
							mainTable = table1;
							secondaryTable = table2;
							indexInRowToColor = 2;
						}
						else{
							mainLabel = jl_o2;
							mainTable = table2;
							secondaryTable = table1;
							indexInRowToColor = 3;
						}

						mainPattern = mainTable.getPattern();
						secondaryPattern = secondaryTable.getPattern();

					}
					else{

						panel = aaTablePanelAB;
						jbMainSwitchButton = jb_switchOrientationAB;
						isMainSwitchVertical = isVerticalAB;

						if(isLeftClick){
							mainLabel = jl_oA;
							mainTable = tableA;
							secondaryTable = tableB;
							indexInRowToColor = 0;
						}
						else{
							mainLabel = jl_oB;
							mainTable = tableB;
							secondaryTable = tableA;
							indexInRowToColor = 1;
						}

						mainPattern = mainTable.getPattern();
						secondaryPattern = secondaryTable.getPattern();


					}

					//				/* If it was displayed previously in a table, remove it */
					//				if(p == secondaryPattern){
					//					secondaryTable.emptyTable();
					//					secondLabel.setText(": to use it");
					//				}
					//				if(p == thirdPattern){
					//					thirdTable.emptyTable();
					//					thirdLabel.setText(": to use it");
					//				}
					//				if(p == fourthPattern){
					//					fourthTable.emptyTable();
					//					fourthLabel.setText(": to use it");
					//				}

					/* If p is not already displayed in the main Table */
					if(p != mainPattern){

						/* If the pattern does not fill the available space */
						if(!mainTable.setPattern(p) && !isMainSwitchVertical){
							boolean isEnabled = jbMainSwitchButton.isEnabled();
							jbMainSwitchButton.setEnabled(true);
							jbMainSwitchButton.doClick();
							jbMainSwitchButton.setEnabled(isEnabled);

						}

						mainLabel.setText(labelText.toString());

						Double d = Corpus.getCorpus().safeSimilarity(p, secondaryPattern);
						String s;

						if(d != null)
							s = "Overview (similarity: " + d + ")";
						else
							s = "Overview";

						panel.setBorder(BorderFactory.createTitledBorder(s));

					}

					/* Color the corresponding row even if the pattern is already displayed (it could be displayed from a previous clustering solution) */
					this.clusterTree.addRowToColor(index, tableColor.get(indexInRowToColor));
					//				this.corpusList.checkColorToColor(index, tableColor.get(indexInRowToColor), true);
				}

				/* If it is the tutorial and the user is supposed to click on an alignment */
				else
					JOptionPane.showMessageDialog(StandardView.getInstance(), "<html>You just clicked on a <i>pattern</i> instead of on an alignment.<br>"
							+ "An alignment is composed of two subparts of two different arrays which are similar.<br>"
							+ "Each of these subparts is called a pattern.<br><br>"
							+ "Please now click on an alignment instead of a pattern.");
			}	   
			else if(cNode.getUserObject() instanceof Cluster){

				/* If it is not the tutorial 
				 * or if it is not the step during which the user must click on a pattern 
				 */
				if(!MainTutorial.IS_TUTO || !(MainTutorial.getCurrentStep() instanceof ChangeDisplayedPattern)){

					Cluster c = (Cluster)cNode.getUserObject();
					int size = c.size();

					if(MainTutorial.IS_TUTO && MainTutorial.getCurrentStep() instanceof ChangeDisplayedAlignment
							&& !isAutomaticallyDisplayingClusters){

						ChangeDisplayedAlignment cda = (ChangeDisplayedAlignment)MainTutorial.getCurrentStep();

						if(StandardView.getInstance().isCtrlPressed)
							cda.ctrlClickOnAlignmentPerformed = true;
						else
							cda.normalClickOnAlignmentPerformed = true;

						if(cda.isOver())
							MainTutorial.nextStep();

					}

					boolean previous = StandardView.getInstance().isCtrlPressed;

					boolean ctrlTableFirst = StandardView.getInstance().isCtrlPressed();

					boolean previousValue = isAutomaticallyDisplayingClusters;
					isAutomaticallyDisplayingClusters = true;
					if(size > 0){
						StandardView.getInstance().isCtrlPressed = ctrlTableFirst;	
						displayClusterSelection(index+1, true, arg0);
						if(size > 1){
							displayClusterSelection(index+2, false, arg0);

							if(size > 2){
								StandardView.getInstance().isCtrlPressed = !ctrlTableFirst;
								displayClusterSelection(index+3, true, arg0);

								if(size > 3)
									displayClusterSelection(index+4, false, arg0);
							}
						}

					}
					isAutomaticallyDisplayingClusters = previousValue;

					StandardView.getInstance().isCtrlPressed = previous; 
				}

				/* If it is the tutorial step during which the user must click on a pattern */
				else
					JOptionPane.showMessageDialog(StandardView.getInstance(), "<html>You just clicked on an alignment instead of on a pattern.<br>"
							+ "Reminder: An alignment is composed of two patterns.<br><br>"
							+ "Please now click on an alignment instead of a pattern.");
			}
		}

	}


	public void updateClusters(ClusteringSolution cs2){

		if(cs2 != null){

			/* If it is the second clustering solution added, display a combobox that will enable to choose the displayed clustering solution */
			if(cscb.getItemCount() == 1){
				jp_cluster.remove(clusterTree);
				jp_cluster.add(cscb, "cell 0 0, growx");
				jp_cluster.add(clusterTree.getJSP(), "cell 0 1, spany 2, grow");
			}

			/* Add the clustering solution to the combobox */
			cscb.addCS(cs2);
		}

	}

	public void updateEndOfClusteringProcess(){

		if(cscb.getItemCount() > 0){			

			/* At the end of the clustering process, display the proper clustering solution
			 * i.e., a solution which contains StandardView.getInstance().getClusterNb() clusters
			 * if it exists
			 */
			int nbOfCluster = StandardView.getInstance().getClusterNb();
			int solutionToDisplay = -1;
			int sliderValue = -1;

			int i = 0;

			while(solutionToDisplay == -1 && i < cscb.getItemCount()){ 

				ClusteringSolution current_cs = cscb.getModel().getElementAt(i);

				if(current_cs instanceof HardClusteringSolution){
					HardClusteringSolution csh = (HardClusteringSolution)current_cs;

					if(csh.getClusterSet().size() == nbOfCluster){
						solutionToDisplay = i;
					}	
				}
				else if(current_cs instanceof HierarchicalClusteringSolution){
					HierarchicalClusteringSolution csh = (HierarchicalClusteringSolution) current_cs;
					for(int j = 0 ; j < csh.getClusterSets().size() ; ++j){
						if(solutionToDisplay == -1){	

							if(csh.getClusterSets().get(j).clusters.size() == nbOfCluster){
								solutionToDisplay = i;
								sliderValue = j;
							}
						}
					}
				}

				++i;

			}

			if(solutionToDisplay != -1){
				cscb.setSelectedIndex(solutionToDisplay);
				displayCBSelection(sliderValue);
			}
			else
				displayCBSelection();

			displayFirstTwoClusters();
		}

		else{
			cscb.reinitialize();
			clusterTree.reinitialize();
			tb_cluster.setTitle("Clusters");

		}

		jp_cluster.repaint();
	}

	boolean isAutomaticallyDisplayingClusters = false;

	public void displayFirstTwoClusters(){

		Object root = clusterTree.getModel().getRoot();

		if(clusterTree.getModel().getChildCount(root) > 0){
			Object firstChild = clusterTree.getModel().getChild(root, 0);

			int numberOfClusters = clusterTree.getModel().getChildCount(root);
			int numberOfPatternsInFirstCluster = clusterTree.getModel().getChildCount(firstChild);

			//		System.out.println("Number of clusters: " + numberOfClusters);
			if(numberOfClusters > 0){
				boolean previous = StandardView.getInstance().isCtrlPressed;
				StandardView.getInstance().isCtrlPressed = false;
				isAutomaticallyDisplayingClusters = true;
				displayClusterSelection(0, true, null);

				if(numberOfClusters > 1){
					StandardView.getInstance().isCtrlPressed = true;
					displayClusterSelection(numberOfPatternsInFirstCluster + 1, false, null);
				}

				StandardView.getInstance().isCtrlPressed = previous;
				isAutomaticallyDisplayingClusters = false;
			}
		}
	}

	public void displayCBSelection(){

		clusterTree.removeSelection();
		displayCBSelection(slider.getValue());
	}

	/**
	 * Display the CSComboBox selection in the cluster tree
	 */
	public void displayCBSelection(int sliderPosition){

		Object selectedElement = cscb.getSelectedItem();

		if(selectedElement instanceof HardClusteringSolution){

			HardClusteringSolution selectedHCS = (HardClusteringSolution)selectedElement;

			clusterTree.setClusterSet(selectedHCS.getClusterSet());

			//System.out.println(selectedHCS.getMethodName() + " - " + selectedHCS.getClusterSet().size() + " : " + sw.getControler().getCorpus().dunnIndex(selectedHCS.getClusterSet()));
			this.removeSlider();

			tb_cluster.setTitle("Clusters (" + selectedHCS.getClusterSet().size() + ")");

		}
		else if(selectedElement instanceof HierarchicalClusteringSolution){
			HierarchicalClusteringSolution selectedHCS = (HierarchicalClusteringSolution)selectedElement;

			ClusterSet firstCS = selectedHCS.getClusterSet(0);

			if(firstCS != null){

				slider.setMinimum(0);
				slider.setMaximum(selectedHCS.size()-1);

				/* If the slider position is not valid for the clustering solution */
				if(sliderPosition < 0 || sliderPosition > selectedHCS.size()-1)
					sliderPosition = 0;

				slider.setValue(sliderPosition);

				ClusterSet csToDisplay = selectedHCS.getClusterSet(sliderPosition);

				/* Display in the tree the solution represented by the slider */
				clusterTree.setClusterSet(csToDisplay);

				/* If the hierarchical clustering solution contains more than one partition */
				if(selectedHCS.size() > 1){
					this.addSlider(selectedHCS);

				}
				else{
					this.removeSlider();

				}

				tb_cluster.setTitle("Clusters (" + selectedHCS.getClusterSet(slider.getValue()).size() + ")");

			}
		}
		/* If the object to display is unknown */
		else{

			this.removeSlider();

			clusterTree.reinitialize();
			tb_cluster.setTitle("Clusters");		

		}

		/* Necessary to update the title */
		jp_cluster.repaint();
	}

	public JSlider getSlider(){
		return slider;
	}

	public boolean contains(JPanel jp, Component c){

		Component[] cs = jp.getComponents();
		boolean result = false;
		int i = 0;

		while(!result && i < cs.length){

			if(cs[i] == c)
				result = true;

			i++;
		}

		return result;
	}

	public void reinitialize(){	

		cscb.reinitialize();
		clusterTree.reinitialize();
		table1.emptyTable();
		table2.emptyTable();
		tableA.emptyTable();
		tableB.emptyTable();
		tb_table12.setTitle("Overview");

	}

	public ClusterSet getSelectedClusterSet(){

		ClusterSet cs = null;

		Object selectedElement = cscb.getSelectedItem();

		if(selectedElement instanceof HierarchicalClusteringSolution){
			HierarchicalClusteringSolution hcs = (HierarchicalClusteringSolution)selectedElement;
			cs = hcs.getClusterSet(slider.getValue());
		}
		if(selectedElement instanceof HardClusteringSolution){

			HardClusteringSolution selectedHCS = (HardClusteringSolution)selectedElement;
			cs = selectedHCS.getClusterSet();
		}

		return cs;
	}

	/**
	 * Add the slider from the panel if it is not
	 */
	private void addSlider(HierarchicalClusteringSolution cs){

		int size = cs.size();

		/* HashTable which will contain the labels of the slider */
		Hashtable<Integer, JLabel> sliderLabelTable = new Hashtable<Integer, JLabel>();

		/* gap between two labels */
		//		double gap = (double)size / nbOfLabel;
		int gap = size / 10 + 1;

		int currentLabel = 0;

		while(currentLabel <= size - 1){

			/* Get the displayed value of the label (ie: the cluster n��<currentLabel> size) */
			Integer clusterSize = cs.getClusterSet((int)currentLabel).size();
			sliderLabelTable.put(currentLabel, new JLabel(clusterSize.toString()));

			currentLabel += gap;

		}

		slider.setMajorTickSpacing(gap);
		slider.setPaintTicks(true);
		slider.setLabelTable(sliderLabelTable);
		slider.setPaintLabels(true);

		/* If the slider is not in the panel */
		if(!contains(jp_cluster, slider)){

			jp_cluster.remove(clusterTree.getJSP());
			jp_cluster.add(slider, "cell 0 1, grow");
			jp_cluster.add(clusterTree.getJSP(), "cell 0 2, grow");
			this.validate();
		}
	}

	/**
	 * Remove the slider from the panel if it is
	 */
	private void removeSlider(){

		/* If the slider is in the panel */
		if(contains(jp_cluster, slider)){
			jp_cluster.remove(slider);
			jp_cluster.remove(clusterTree.getJSP());
			jp_cluster.add(clusterTree.getJSP(), "cell 0 1, grow, spany 2");

			/* Necessary to increase the size of the clusterTree */
			this.validate();
		}

	}

	private void changeTableOrientation(boolean becomeHorizontal, boolean is12Table){

		JPanel jpMain = aaTablePanelAB;
		JPanel jp1 = jp_lA;
		JPanel jp2 = jp_lB;
		AATable jt1 = tableA;
		AATable jt2 = tableB;

		if(is12Table){
			jpMain = aaTablePanel12;
			jp1 = jp_l1;
			jp2 = jp_l2;
			jt1 = table1;
			jt2 = table2;
		}

		if(MainTutorial.IS_TUTO && MainTutorial.getCurrentStep() instanceof ChangeTablesOrientation){
			ChangeTablesOrientation cto = (ChangeTablesOrientation)MainTutorial.getCurrentStep();

			if(becomeHorizontal)
				cto.verticalClicked = true;
			else
				cto.horizontalClicked = true;

			if(cto.isOver())
				MainTutorial.nextStep();

		}


		jpMain.removeAll();

		if(becomeHorizontal){
			jpMain.add(jp1, "spanx 2, growx, wrap");
			jpMain.add(jt1.getJSP(), "spanx 3, grow, wrap");
			jpMain.add(jp2, "spanx 2, growx, wrap");
			jpMain.add(jt2.getJSP() ,"spanx 3, grow, wrap");
		}
		else{

			jpMain.add(jp1, "growx");
			jpMain.add(jp2, "growx, wrap");
			jpMain.add(jt1.getJSP(), "grow");
			jpMain.add(jt2.getJSP() ,"grow");
		}

		this.validate();
		jt1.componentResized(null);	
		jt2.componentResized(null);
	}

	public void maximize(){
		//		aaTablePanel.updateUI();
		table1.componentResized(null);	
		table2.componentResized(null);
		tableA.componentResized(null);	
		tableB.componentResized(null);
	}

	public void removeClusteringSolution(int i) {
		if(cscb.getItemCount() > i)
			cscb.remove(i);
	}

}
