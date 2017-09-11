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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import exception.CSVSeparatorNotFoundException;
import extraction.PositiveScoreTable;
import extraction.SABREParameter;
import main.MainTutorial;
import model.AAColumnFormat;
import model.AnnotatedArray;
import model.AnnotationColumn;
import model.Corpus;
import net.miginfocom.swing.MigLayout;
import tuto.EditDesiredNumberOfAlignmentsStep;
import tuto.EditGap;
import tuto.OpenEditSimilarityFrame;
import util.springbox.AAReader;

@SuppressWarnings("serial")
public class SelectionPanel extends JPanel{
	
	
	private AAList aal = new AAList();
	private AATable aat = new AATable();
	public JButton jb_process_extraction_and_clustering = new JButton("Extract and cluster");
	public JButton jb_process_extraction = new JButton("Extract");
	
	private JLabel jl_corpus = new JLabel("Corpus");
	private JLabel jl_table = new JLabel("Overview");
	public JTextField jtf_gap_score = new JTextField("", 30);
	public JTextField jtf_desired_nb_of_alignments = new JTextField("", 30);
	public JTextField jtf_sim_scores = new JTextField("", 30);
	public JTextField jtf_K = new JTextField("", 30);
	public JTextField jtf_maxSim = new JTextField("", 30);
	public JIconButton jb_sim_score_fileChooser;
	public JIconButton jb_sim_editor;
	public JIconButton jb_tool;
	public JIconButton jb_addAA;
	public JIconButton jb_removeAA;
	public JIconButton jb_removeAllAA;
	
	private StandardView sv;
	
	//TODO quand on change le type de colonnes,  vérifier les fichiers qui seront perdus et l'indiquer à l'utilisateur
	
	/** List of string which contain the values of the csv file of the last annotated array which has been added for the current column format 
	 * (null as long as no column format is defined) */
	private List<String[]> lastEntries = null;

	private JPanel jp_clustering = new JPanel(new MigLayout("fillx", "", "0[]0[]0"));
	
	public SelectionPanel(StandardView f_sw){
		
		sv = f_sw;
		this.setLayout(new MigLayout("fill", "[50%][50%]", ""));		
		
		aal.getJSP().setPreferredSize(new Dimension(243, 270));
		aal.getJSP().setMinimumSize(new Dimension(243, 270));
		
		aal.addMouseListener(new MouseListener(){
			
			public void mouseReleased(MouseEvent arg0){
				
				ListSelectionModel lsm = aal.getSelectionModel();
				
				if(!lsm.isSelectionEmpty()){
					
		            int minIndex = lsm.getMinSelectionIndex();
		            
		            AnnotatedArray aa = (AnnotatedArray)aal.getModel().getElementAt(minIndex);
		            jl_table.setText("Overview: " + aa.getFileName());
		            aat.setAA((AnnotatedArray)aal.getModel().getElementAt(minIndex));
		            
				}
			}
			
			public void mousePressed(MouseEvent arg0){}
			public void mouseExited(MouseEvent arg0){}
			public void mouseEntered(MouseEvent arg0){}
			public void mouseClicked(MouseEvent arg0){}
			
		});
		
		aal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/* *** AnnotatedArrays pane definition *** */
		JHelpButton jb_helpSelectAA = new JHelpButton(sv, "<html>Buttons used to manage the annotated arrays in the corpus:<br>"
				+ "(+) : Add annotated arrays (if the corpus does not contain any array, also enables to define the csv file format);<br>"
				+ "(-) : Remove the selected annotated array;<br>"
				+ "(trash) : Remove all the annotated arrays;<br>"
				+ "(settings): Modify the csv file format (only possible if the corpus contains at least one annotated array).<br><br>"
				+ "When defining the csv file format, each column of the input file is associated one type among the following four:<br>"
				+ "- unused: the column is ignored;<br>"
				+ "- comment: the column is visible but not used to extract the patterns;<br>"
				+ "- numerical annotation: the column is visible and used to extract the patterns. "
				+ "In this column, the similarity between two annotations x and y is equal to max(0, maximal_similarity - |x-y|);<br>"
				+ "- non numerical annotation: the column is visible and used to extract the patterns. "
				+ "The similarity between two annotations in this column is computed according to the similarity score table.<br>"
				+ "If the score between two annotations is not specified in the score table it is considered to be 0.<br>"
				+ "</html>");
		jb_addAA = new JIconButton("img/plus.png");
		jb_removeAA = new JIconButton("img/minus.png");
		jb_removeAllAA = new JIconButton("img/trash.png");
		jb_tool = new JIconButton("img/tool.png");
		JPanel jp_selectAA = new JPanel(new MigLayout("fill", "[fill]push[][][][]", "[][]"));
		
		jp_selectAA.add(jl_corpus);
		jp_selectAA.add(jb_addAA);
		jp_selectAA.add(jb_removeAA);
		jp_selectAA.add(jb_removeAllAA);
		jp_selectAA.add(jb_tool);
		jp_selectAA.add(jb_helpSelectAA, "wrap");
		jp_selectAA.add(aal.getJSP(), "spanx 6");
		
		jb_addAA.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				
				int returnVal = chooser.showOpenDialog(sv);
				if(returnVal == JFileChooser.APPROVE_OPTION){		

					int previousSize = aal.getModel().getSize();

					try {
						AAReader reader = new AAReader(getFirstCSVFile(chooser.getSelectedFiles()));
						List<String[]> myEntries = reader.read();
						boolean update = false;
						
						/* If the column format is not defined */
						if(!Corpus.getCorpus().isColumnFormatDefined() || Corpus.getCorpus().getAASize() == 0){
							CSVColumnsSelector csvcs = new CSVColumnsSelector(sv, myEntries);
							AAColumnFormat aacf = csvcs.showThis();
							
							/* If the dialog window in which the aacf must be defined has not been closed prematurely */
							if(aacf != null){
								Corpus.getCorpus().setAACF(aacf, true);
							}
						}
				
						if(Corpus.getCorpus().isColumnFormatDefined()){
							
							/* Add the annotated arrays */
							sv.getControler().addAA(chooser.getSelectedFiles(), false);
			
							if( previousSize == 0 || !update) {
								AnnotatedArray aa = (AnnotatedArray)aal.getModel().getElementAt(0);
								aat.setAA(aa);
								jl_table.setText("Overview: " + aa.getFileName());
							}
							
							/* If the annotated array is added update the last added entries */
							lastEntries = myEntries;
								
						}
						
						activateDesactivateSettingsAndProcessButtons();
							
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (CSVSeparatorNotFoundException e2) {

						JOptionPane
								.showMessageDialog(
										sv, e2.defaultMessage(), "Warning",
										JOptionPane.WARNING_MESSAGE);
					}
					
				}
			}
		});
		
		jb_removeAA.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(!aal.isSelectionEmpty()){
					int selectedIndex = aal.getSelectedIndex();
					sv.getControler().removeAA((AnnotatedArray)aal.getModel().getElementAt(selectedIndex));
					
					if(aal.getModel().getSize() > 0){				
						if(selectedIndex > 0){
							aal.setSelectedIndex(selectedIndex-1);
						}
						else{
							aal.setSelectedIndex(0);
						}
					}
					
					activateDesactivateSettingsAndProcessButtons();
				}
			}
		});
		
		jb_removeAllAA.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){		
				for(int i = Corpus.getCorpus().getAASize()-1 ; i >= 0 ; i--){
					sv.getControler().removeAA((AnnotatedArray)aal.getModel().getElementAt(i));
				}
				
				activateDesactivateSettingsAndProcessButtons();
			}
		});
		
		jb_tool.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){		
				
				boolean readyToDisplayTool = false;
				
				if(lastEntries != null)
					readyToDisplayTool = true;
				
				int i = 0;
				
				while(!readyToDisplayTool && i < Corpus.getCorpus().getAA().size()){
					
					AAReader reader;
					try {
						reader = new AAReader(Corpus.getCorpus().getAA().get(i).getFullPath());
						lastEntries = reader.read();
					} catch (Exception e1) {}
					
					++i;
				}
				

				if(lastEntries != null && Corpus.getCorpus().getAA().size() > 0){

					if(MainTutorial.IS_TUTO)
						MainTutorial.nextStep();
					
					CSVColumnsSelector csvcs = new CSVColumnsSelector(sv, lastEntries);
					AAColumnFormat aacf = csvcs.showThis();
					
					if(aacf != null)
						Corpus.getCorpus().setAACF(aacf, true);

					if(!MainTutorial.IS_TUTO)
						activateDesactivateSettingsAndProcessButtons();
				}
				else{
					JOptionPane.showMessageDialog(sv, "<html>This button enables to change the column format.<br>"
							+ "(i.e., the choice of the columns of the input files that contain annotations, numerical annotations, comments or that will be ignored).<br><br>To use this button you must first define the format by adding csv files to the corpus.<br><br>If the corpus already contains annotated arrays their csv files cannot be reached. Remove them all and add again the arrays to change the column format.");
				}
				
			}
		});
		
		
		/* *** End * Selected AnnotatedArrays pane *** */


		/* *** Parameters panel definition *** */		
		JHelpButton jb_help_sim_score = new JHelpButton(sv, "<html>The positive score table contains the similarity score of all the similar couples of annotations "
				+ "(only relevant if non numerical annotation columns are considered)<br>" +
				"The similarity between two annotations is equal to 0 if it is not specified in this table.<br><br>" +
				"The left button enables to set the input file of the positive score table.<br>" +
				"The right button enables to edit the scores of the current table.<br><br>" +
				"The input csv file must satisfy the following format: <br>- the first line contains headers that will be ignored;<br>"
				+ "- the remaining lines contain values in its three first columns<br>"
				+ "(column 1: first annotation, column 2: second annotation, column 3: similarity between these two annotations, "
				+ "the remaining columns are ignored).<br><br>"
				+ "Example:<br>Annotation 1, Annotation 2, Similarity, Comment<br>P, P, 2, The similarity between annotations P and P is equal to 2<br>P, Q, 1, The similarity between annotations P and Q is equal to 1</html>");
		JHelpButton jb_help_K = new JHelpButton(sv, "Desired number of clusters. An alignments is a set of two patterns extracted together.<br><br>"
				+ "Solutions with this number of clusters will be preferentially displayed.");
		jb_sim_score_fileChooser = new JIconButton("img/folder.png");
		jb_sim_editor = new JIconButton("img/edit.png");

		/* The Text field are disabled as long as Annotated Arrays have not been added to the corpus */
		jtf_maxSim.setEnabled(false);
		jtf_sim_scores.setEnabled(false);
		jtf_gap_score.setEnabled(false);
		jtf_desired_nb_of_alignments.setEnabled(false);
		jtf_K.setEnabled(false);
		jb_sim_score_fileChooser.setEnabled(false);
		jb_sim_editor.setEnabled(false);

		jp_clustering.setBorder(BorderFactory.createTitledBorder("2 - Pattern clustering"));
		
		JPanel jp_extraction = new JPanel(new MigLayout("fill", "[fill]", ""));
		jp_extraction.setBorder(BorderFactory.createTitledBorder("1 - Pattern extraction"));
		jtf_sim_scores.setEditable(false);
		
		jb_sim_score_fileChooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
				int returnVal = chooser.showOpenDialog(sv);
				
				if(returnVal == JFileChooser.APPROVE_OPTION){				
					sv.getControler().setAnnotationSimilarities(chooser.getSelectedFile());
					jb_sim_editor.setEnabled(true);
				}
			}
		});
		

		
		jb_sim_editor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				try{

					if(MainTutorial.IS_TUTO 
							&& MainTutorial.getCurrentStep() != null
							&& MainTutorial.getCurrentStep() instanceof OpenEditSimilarityFrame
							)
						MainTutorial.nextStep();
						
					PSTEditor psted = new PSTEditor(sv);
					PositiveScoreTable pst = psted.showThis();
					
					if(pst != null){
						sv.getControler().setAnnotationSimilarities(pst);

						if(!MainTutorial.IS_TUTO)
							activateDesactivateSettingsAndProcessButtons();

					}
					
				} catch (Exception e1) {
					e1.printStackTrace();
					
				}	
			}
		});
		
		JLabel jl_maxSim = new JLabel("Maximal similarity");
		JHelpButton jhb_maxSim = new JHelpButton(sv, "<html>Maximal similarity between two numerical annotations (only used if numerical annotation columns are considered)<br>"
				+ "The similarity between two annotations x and y in a numerical annotation column is equal to max(0, maximal_similarity - |x-y|).</html>");
		jtf_maxSim.addFocusListener(new FocusListener(){

			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				setMaxSim(Double.parseDouble(jtf_maxSim.getText()));	
			}
		});
		
		//controle de clavier
	aat.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			//TODO 
		
			
			public void keyReleased(KeyEvent e) {
				if(e.isControlDown()){
					//ctrl +c
					if(e.getKeyCode() == KeyEvent.VK_C){
						int col = aat.getSelectedColumn();
					    int row = aat.getSelectedRow();
					    if (col != -1 && row != -1) {
					        Object value = aat.getValueAt(row, col);
					        String data;
					        if (value == null) {
					            data = "";
					        } else {
					            data = ((AATable.AATableModel.AACellModel ) value).getData();
					        }//end if

					        final StringSelection  selection  = new StringSelection(data);     

					        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					        clipboard.setContents(selection, selection);
					    }//end if
					
						
						
					}
				}
				
				
			}
		
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		jtf_maxSim.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				setMaxSim(Double.parseDouble(jtf_maxSim.getText()));	
			
			}
		});
		
		JLabel jl_gap_score = new JLabel("Gap cost");
		JHelpButton jhb_gap_score = new JHelpButton(sv, "The cost of having a gap of one line between two annotations in a pattern. The higher this cost, the smaller the patterns extracted.");
		jtf_gap_score.addFocusListener(new FocusListener(){

			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				setDesynchCost(Double.parseDouble(jtf_gap_score.getText())/2.0);	
	
				if(MainTutorial.IS_TUTO && MainTutorial.getCurrentStep() != null && MainTutorial.getCurrentStep() instanceof EditGap){
				
					EditGap cdnas = ((EditGap)MainTutorial.getCurrentStep());
					if(Double.parseDouble(jtf_gap_score.getText()) == cdnas.newParameterValue)
							cdnas.hasValueBeenChanged = true;
					else
						JOptionPane.showMessageDialog(SelectionPanel.this, "Please set the gap cost to " + cdnas.newParameterValue + " before starting the extraction.");
				}		}
			
		});
		
		jtf_gap_score.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			
				setDesynchCost(Double.parseDouble(jtf_gap_score.getText())/2.0);	
			
			}
		});
		
		JLabel jl_desired_number_of_alignments = new JLabel("Number of alignments");
		JHelpButton jhb_desired_number_of_alignments = new JHelpButton(sv, "<html><br>Desired number of alignments.<br><br>Let x be this number. The methods will extract the x alignments in the corpus with the best score.<br>More alignments can be obtained if there are several alignments with the lowest score.<br><br>Example:<br>If we seek 4 alignments and we obtain 6 alignments with the following scores: 1, 2, 2, 3, 4, 5.<br>Alignments of score 2, 2, 3, 4, 5 are kept.</html>");
		//		JHelpButton jhb_min_score = new JHelpButton(sv, "Score above which a pattern is found. The lower this score, the higer the number of extracted patterns.");
		jtf_desired_nb_of_alignments.addFocusListener(new FocusListener(){

			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				setDesiredNumberOfAlignments(Integer.parseInt(jtf_desired_nb_of_alignments.getText()));

				if(MainTutorial.IS_TUTO && MainTutorial.getCurrentStep() != null && MainTutorial.getCurrentStep() instanceof EditDesiredNumberOfAlignmentsStep){
					
					EditDesiredNumberOfAlignmentsStep cdnas = ((EditDesiredNumberOfAlignmentsStep)MainTutorial.getCurrentStep());
					if(Integer.parseInt(jtf_desired_nb_of_alignments.getText()) == cdnas.newParameterValue)
							cdnas.hasValueBeenChanged = true;
					else
						JOptionPane.showMessageDialog(SelectionPanel.this, "Please set the desired number of alignments to " + cdnas.newParameterValue + " before starting the extraction.");
				}
			}
			
		});
		
		jtf_desired_nb_of_alignments.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setDesiredNumberOfAlignments(Integer.parseInt(jtf_desired_nb_of_alignments.getText()));
			}
		});
		
		JPanel jp_gap = new JPanel(new MigLayout("fill", "[fill]push[10]", ""));
		jp_gap.add(jl_gap_score, "wrap");
		jp_gap.add(jtf_gap_score, "growx");
		jp_gap.add(jhb_gap_score, "wrap");
		jp_extraction.add(jp_gap, "growx");
		
		JPanel jp_desired_number_of_alignments = new JPanel(new MigLayout("fill", "[fill]push[10]", ""));
		jp_desired_number_of_alignments.add(jl_desired_number_of_alignments, "wrap");
		jp_desired_number_of_alignments.add(jtf_desired_nb_of_alignments, "growx");
		jp_desired_number_of_alignments.add(jhb_desired_number_of_alignments, "wrap");
		jp_extraction.add(jp_desired_number_of_alignments, "growx,wrap");
		
		JPanel jp_maxSim = new JPanel(new MigLayout("fill", "[fill]push[10]", ""));
		jp_maxSim.add(jl_maxSim, "wrap");
		jp_maxSim.add(jtf_maxSim, "growx");
		jp_maxSim.add(jhb_maxSim, "wrap");

		jp_extraction.add(jp_maxSim, "growx");  
			
		JPanel jp_sim_scores = new JPanel(new MigLayout("fill", "[fill]push[10][10]", ""));
//		jp_temp.setMaximumSize(new Dimension(4000, 30));
		jp_sim_scores.add(new JLabel("Similarity scores"), "wrap");
		jp_sim_scores.add(jtf_sim_scores, "growx");	
		jp_sim_scores.add(jb_sim_score_fileChooser, "");
		jp_sim_scores.add(jb_sim_editor, "");
		jp_sim_scores.add(jb_help_sim_score, "wrap");
		jp_extraction.add(jp_sim_scores, "spanx 2, growx, wrap");
//		jp_extraction.add(jp_sim_scores, "growx, wrap");

		
		JPanel jp_K = new JPanel(new MigLayout("fill", "[fill]push[10]", ""));
		jp_K.add(new JLabel("Number of clusters"), "wrap");
		jp_K.add(jtf_K, "growx");	
		jp_K.add(jb_help_K, "wrap");
		jp_clustering.add(jp_K, "growx, wrap");
		
		/* End *** Parameter panel definition */
		
		/* *** Process pane */
		JPanel jp_process = new JPanel(new MigLayout("", "[][][]"));
		JHelpButton jhb_process = new JHelpButton(sv, "Start the pattern extraction. In addition, the right button will cluster the obtained patterns (thanks to several heuristics)");
		jp_process.add(jb_process_extraction_and_clustering);
		jp_process.add(jb_process_extraction);
		jp_process.add(jhb_process);
		
		jb_process_extraction_and_clustering.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){		
				sv.process_extraction_and_clustering();			
			}
		});
		
		jb_process_extraction.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				if(MainTutorial.IS_TUTO){
					
					if(MainTutorial.getCurrentStep() != null){
						
						if(MainTutorial.getCurrentStep() instanceof EditDesiredNumberOfAlignmentsStep 
								&& !((EditDesiredNumberOfAlignmentsStep)MainTutorial.getCurrentStep()).hasValueBeenChanged
								){
							JOptionPane.showMessageDialog(SelectionPanel.this, "Please change the desire number of alignments before starting a new extraction");
						}
						else if(MainTutorial.getCurrentStep() instanceof EditGap 
								&& !((EditGap)MainTutorial.getCurrentStep()).hasValueBeenChanged
								){
							JOptionPane.showMessageDialog(SelectionPanel.this, "Please change the gap cost before starting a new extraction");
						}
						else{
							MainTutorial.nextStep();
							sv.process_extraction();
						}
					}
				}
				else
					sv.process_extraction();		
			}
		});
		
		/* *** End * Process pane */
		
		this.add(jp_selectAA, "cell 0 0, spany 2, grow");
		this.add(jp_extraction, "cell 1 0, grow");
		
		this.add(jp_clustering, "cell 1 1, grow");
		this.add(jl_table, "cell 0 2");
		this.add(aat.getJSP(), "cell 0 3, grow, spanx 2");
		this.add(jp_process, "cell 0 4");
	
		aat.setCellSelectionEnabled(true);
		aat.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	
	protected void setDesynchCost(double d) {
		
		sv.getControler().setDesynchCost(d);
		sv.getControler().setGapCost(2*d);	
		
	}
	
	protected void setMaxSim(double d) {
	
		sv.getControler().setMaxSim(d);	
		
	}

	private void activateDesactivateSettingsAndProcessButtons() {
		
		if(Corpus.getCorpus().isColumnFormatDefined() && Corpus.getCorpus().getAA().size() > 0){
			jtf_gap_score.setEnabled(true);
			jtf_K.setEnabled(true);
			jtf_desired_nb_of_alignments.setEnabled(true);
						
			if(jtf_K.getText().equals(""))
				jtf_K.setText("1");
			
			if(Corpus.getCorpus().getAA(0).getNumericalAnnotations().size() > 0){
				jtf_maxSim.setEnabled(true);
				
				if(jtf_maxSim.getText().equals("")){
					jtf_maxSim.setText("1");
					setMaxSim(1.0);
				}
			}
			else
				jtf_maxSim.setEnabled(false);
			
			if(Corpus.getCorpus().getAA(0).getAnnotations().size() > 0){
				jtf_sim_scores.setEnabled(true);
				jb_sim_score_fileChooser.setEnabled(true);
				
				if(AnnotationColumn.pst != null)
					jb_sim_editor.setEnabled(true);
				
				
			}
			else{
				jtf_sim_scores.setEnabled(false);
				jb_sim_score_fileChooser.setEnabled(false);
				jb_sim_editor.setEnabled(false);
			}
		}
		else{

			jtf_gap_score.setEnabled(false);
			jtf_K.setEnabled(false);
			jtf_maxSim.setEnabled(false);
			jtf_desired_nb_of_alignments.setEnabled(false);
			jtf_sim_scores.setEnabled(false);
			jb_sim_score_fileChooser.setEnabled(false);
			jb_sim_editor.setEnabled(false);
		}
		
		jb_process_extraction_and_clustering.setEnabled(isReadyToProcess());
		jb_process_extraction.setEnabled(isReadyToProcess());
		
	}

	public void addAA(AnnotatedArray aa){
		
		aal.addAA(aa);		
		
		if(aal.getModel().getSize() == 1){
			jl_table.setText("Overview: " + aa.getFileName());
			this.aat.setAA(aa);
		}
		
		jl_corpus.setText("Corpus (" + aal.getModel().getSize() + ")");
		aal.repaint();
		
		if(!MainTutorial.IS_TUTO)
			activateDesactivateSettingsAndProcessButtons();
	}
	
	public void removeAA(int id){
		
		/* If the aa is displayed in the table, clear the table */
		if(aat.getAA() != null && aal.getAA(id).getFileName().equals(aat.getAA().getFileName())){
			
			jl_table.setText("Overview");
			aat.emptyTable();
		}
		
		aal.removeAA(id);
		
	
		jl_corpus.setText("Corpus (" + aal.getModel().getSize() + ")");

		if(!MainTutorial.IS_TUTO)
			activateDesactivateSettingsAndProcessButtons();
	}
	
	
	// TODO Ajouter une nouvelle tab de visualisation sans graphe et avec 3 tables supplémentaires à la place
	//Quand on click gauche ça met dans première table
	// Quand on click droit ça affiche un menu contextuel pour choisir laquelle des 4 autres tables on veut
	public void setDesiredNumberOfAlignments(int score){

		sv.getControler().setDesiredNumberOfAlignments(score);
		
		if(!MainTutorial.IS_TUTO)
			activateDesactivateSettingsAndProcessButtons();
		//		StandardView.getInstance().log("Set desired number of alignments to " + score);
	}
	
	public boolean isReadyToProcess(){
		
		return Corpus.getCorpus().isReadyToProcess();
			
	}
	
	public String getFirstCSVFile(File f, boolean goIntoChildrenFolders){
		
		String result = "";
		
		if(f.exists()){
			if(f.isDirectory()){
				if(goIntoChildrenFolders)
					
					/* Set goIntoChildrenFolders to false to ensure that no children folders will be opened */
					result = getFirstCSVFile(f.listFiles(), false);
				
			}
			
			else{
				
				/* Regexp to test that the end of the file end by "csv" */
				java.util.regex.Pattern csvRegexp = java.util.regex.Pattern.compile(".*csv");
	
				Matcher fileName = csvRegexp.matcher(f.getName());
				
				if(fileName.matches()){
					result = f.getAbsolutePath();
				}
				
			}
		}
		
		return result;

	}
	
	/**
	 * Returns the path of the first csv file in the list <f> or in the files directly in a folder in <f> 
	 * @param f
	 * @param goIntoNextLevelFolders 
	 * @return
	 */
	public String getFirstCSVFile(File[] f){
		return getFirstCSVFile(f, true);
	}
	
	/**
	 * Returns the path of the first csv file in the list <f>
	 * @param f List of files
	 * @param goIntoChildrenFolders True if the files in the folders contained in f must be considered
	 * @return
	 */
	private String getFirstCSVFile(File[] f, boolean goIntoChildrenFolders){
		
		String result = "";
		int i = 0;
		
		while("".equals(result) && i < f.length){
			result = getFirstCSVFile(f[i], goIntoChildrenFolders);		
			i++;
		}
		
		return result;
		
	}
	
	public void updateScoreSimilarities(PositiveScoreTable p) {
		String[] s= p.getPath().split("/");
		
		jtf_sim_scores.setText(s[s.length-1]);

		if(!MainTutorial.IS_TUTO)
			activateDesactivateSettingsAndProcessButtons();		
	}

	public void updateSABREParameters(SABREParameter p) {
		jtf_gap_score.setText(((Double)(p.gap_cost)).toString());

		updateDesiredNumberOfAlignments(p.desired_number_of_alignments);

		if(!MainTutorial.IS_TUTO)
			activateDesactivateSettingsAndProcessButtons();	
	}
	
	public void updateDesiredNumberOfAlignments(int v){
		jtf_desired_nb_of_alignments.setText(((Integer)v).toString());
	}

	public int getClusterNb() {
		
		int result;
		
		try {
			result = Integer.parseInt(jtf_K.getText());
		} catch (NumberFormatException nfe) {
			result = -1;
		}
		
		return result;
	}

	public void updateMaxSimilarity(double d) {
		jtf_maxSim.setText(((Double)d).toString());
	}
	
}
