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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Controler.StandardViewControler;
import clustering.AbstractClusteringMethod;
import clustering.ClusteringSolution;
import clustering.ROCK;
import clustering.SingleLink;
import exception.AbstractException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import main.MainTutorial;
import model.AnnotatedArray;
import model.Corpus;
import model.CorpusObserver;
import model.SABREObserver;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class StandardView extends JFrame implements CorpusObserver,
		SABREObserver, AWTEventListener {

	public SelectionPanel jf_s = null;
	private StandardViewControler svc;
	private JTabbedPane tab = new JTabbedPane();
	private JDialogProgressBar jdpb;
	
	public JLabel jlTuto1; 
//	public JEditorPane jlTutoStepDescription;
//	public JEditorPane jlTutoStepInstructions;
	public JLabel jlTutoStepDescription;
	public JLabel jlTutoStepInstructions;

	public int panelTutoExtraHeight = 100;
	public int panelTutoExtraWidth = 150;
	public int selectionPaneWidth = 650;
	public int selectionPaneHeight = 700;
	public int visualisationPaneWidth = 900;
	
	public JScrollPane jspTuto = null;

	public static StandardView currentInstance = null;

	public boolean isCtrlPressed = false;


	private boolean processClusteringAfterExtraction = false;

	public StandardView() {

		try{
		System.setProperty("org.graphstream.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		try {
//			for(int i = 0 ; i < UIManager.getInstalledLookAndFeels().length ; ++i)
//				System.out.println(UIManager.getInstalledLookAndFeels()[i].getClassName());
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			
			
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		Toolkit tk = Toolkit.getDefaultToolkit();
		tk.addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);

		svc = new StandardViewControler();
		jf_s = new SelectionPanel(this);

		System.setProperty("org.graphstream.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		this.setTitle("Viesa");

		if(MainTutorial.IS_TUTO)
			this.setSize(selectionPaneWidth + panelTutoExtraWidth, selectionPaneHeight + panelTutoExtraHeight);
		else
			this.setSize(selectionPaneWidth, selectionPaneHeight);

		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

//		if (Toolkit.getDefaultToolkit().getImage(
//				getClass().getClassLoader().getResource("./img/icon.png")) == null)
			// System.out.println("found it");

			// this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("./img/icon.png")));
//			this.setIconImage((new ImageIcon(getClass().getClassLoader()
//					.getResource("img/icon.png"))).getImage());

		this.add(tab, BorderLayout.CENTER);

		tab.add("Data selection", jf_s);
		tab.add("Result visualization", VisualisationPanel.getInstance());

		/* Change the window when changing the tab */
		tab.addChangeListener(new ChangeListener() {

			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane) evt.getSource();

				switch (pane.getSelectedIndex()) {
				case 1:
					if(MainTutorial.IS_TUTO)
						StandardView.this.setSize(visualisationPaneWidth, selectionPaneHeight + panelTutoExtraHeight);
					else
						StandardView.this.setSize(visualisationPaneWidth, selectionPaneHeight);
					break;
				default:

					if(MainTutorial.IS_TUTO)
						StandardView.this.setSize(selectionPaneWidth + panelTutoExtraWidth, selectionPaneHeight + panelTutoExtraHeight);
					else
						StandardView.this.setSize(selectionPaneWidth, selectionPaneHeight);
				}
			}
		});

		/* Confirm the closing */
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

//				StandardView.this.toFront();
//
//				int reponse = JOptionPane.showConfirmDialog(StandardView.this,
//						"Do you really want to quit the application ?",
//						"Confirmation", JOptionPane.YES_NO_OPTION,
//						JOptionPane.QUESTION_MESSAGE);
//
//				if (reponse == JOptionPane.YES_OPTION) {
					StandardView.this.dispose();
					
					MainTutorial.IS_TUTO = false;
					
					SABRE.getInstance().removeObserver(StandardView.this);
					Corpus.getCorpus().removeObserver(StandardView.this);
					System.exit(0);
//				}
			}
		});

		/* JMenu definition */
		JMenu jm_corpus = new JMenu("Corpus");
		JMenuItem jmiCorpusSelection = new JMenuItem("Go back to corpus selection frame");
		JMenuItem jmi_save = new JMenuItem("Save corpus");
		JMenuItem jmi_load = new JMenuItem("Load corpus");

		jm_corpus.add(jmiCorpusSelection);
//		jm_corpus.add(jmi_save);
//		jm_corpus.add(jmi_load);
		
		jmiCorpusSelection.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new SelectCorpusFrame();

				MainTutorial.IS_TUTO = false;
				
				SABRE.getInstance().removeObserver(StandardView.this);
				Corpus.getCorpus().removeObserver(StandardView.this);
				
				dispose();
			}
		});

		jmi_save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = chooser.showOpenDialog(StandardView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {

					StandardView.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
					boolean b_save = true;
					File f = chooser.getSelectedFile();

					/* If the file already exists, ask to replace it */
					if (f.exists()) {

						StandardView.this.toFront();

						int reponse = JOptionPane.showConfirmDialog(
								StandardView.this,
								"A file named \""
										+ f.getPath()
										+ "\" already exists.\nDo you want to replace it?",
								"Confirmation", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);

						if (reponse == JOptionPane.NO_OPTION) {
							b_save = false;
						}
					}

					if (b_save) {

						/* Save the file */
						try {
							FileOutputStream file = new FileOutputStream(f);
							ObjectOutputStream oos = new ObjectOutputStream(
									file);
							oos.writeObject(Corpus.getCorpus());
							oos.flush();
							oos.close();
						} catch (java.io.IOException e2) {
							e2.printStackTrace();
						}
					}

					StandardView.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				}
			}
		});

		jmi_load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = chooser.showOpenDialog(StandardView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {

					StandardView.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));

					File f = chooser.getSelectedFile();
					if (f.exists()) {

						loadFile(f);
					} else
						JOptionPane.showMessageDialog(StandardView.this,
								"Can't open file \""
										+ chooser.getSelectedFile().getPath()
										+ "\", it doesn't exist.", "Warning",
								JOptionPane.WARNING_MESSAGE);

					StandardView.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

			}
		});



		if(MainTutorial.IS_TUTO){

//			jlTutoStepDescription = new JEditorPane();
//			jlTutoStepInstructions = new JEditorPane();
//
//			jlTutoStepDescription.setEditable(false);
//			jlTutoStepInstructions.setEditable(false);
//
//			jlTutoStepDescription.setEditorKit(new WrapEditorKit());
//			jlTutoStepInstructions.setEditorKit(new WrapEditorKit());
//
//			jlTutoStepDescription.setContentType("text/html");
//			jlTutoStepInstructions.setContentType("text/html");
			
//			jlTutoStepDescription.setLineWrap(true);
//			jlTutoStepInstructions.setLineWrap(true);

			jlTutoStepDescription = new JLabel();
			jlTutoStepInstructions = new JLabel();

			jlTuto1 = new JLabel("<html><b>Description (step " + (MainTutorial.currentStepId) + "/" + (MainTutorial.lSteps.size() - 1) + ")</b></html>");
			
			this.addJSPTuto();

		}

		JMenuBar jmb_bar = new JMenuBar();
		setJMenuBar(jmb_bar);
		jmb_bar.add(jm_corpus);

		// //TO REMOVE
		// System.out.println("pouet");
		// loadFile(new File("donnees.ser"));

		// System.out.println(svc.getCorpus().getClusteringSolution().size() +
		// " clusterion solutions");
		//
		// for(ClusteringSolution cs : svc.getCorpus().getClusteringSolution())
		// {
		//
		//
		// HardClusteringSolution hcs = (HardClusteringSolution)cs;
		//
		//
		//
		// double distance = 0.0;
		//
		// for(Cluster c : hcs.getClusterSet().clusters) {
		//
		// for(int i = 0 ; i < c.getPatterns().size() ; ++i) {
		//
		// Pattern p1 = c.getPatterns().get(i);
		//
		// for(int j = 0 ; j < i ; ++j) {
		//
		// Pattern p2 = c.getPatterns().get(j);
		// distance += svc.getCorpus().dissimilarity(p1, p2);
		// }
		// }
		//
		// }
		//
		// System.out.println(hcs.getMethodName() +" : distance = " + distance
		// );
		//
		//
		//
		// }

		//
		//
		//
		// ObjectOutputStream oos = null;
		//
		// ObjectInputStream ois = null;
		//
		// try {
		// // final FileInputStream fichier = new
		// FileInputStream("boolean_motif_a_garder.ser");
		// // ois = new ObjectInputStream(fichier);
		// // final boolean[] b= (boolean[]) ois.readObject();
		//
		// final FileInputStream fichier = new
		// FileInputStream("valeur_a_garder_egal_trois_et_quatre.ser");
		// ois = new ObjectInputStream(fichier);
		// final int[] b= (int[]) ois.readObject();
		//
		// ArrayList<Pattern> patterns = getCA().getPatterns();
		//
		// for(int i = patterns.size() - 1 ; i >= 0 ; --i) {
		//
		// // if(!b[i]) {
		// if(b[i] != 4 && b[i] != 3) {
		// patterns.remove(i);
		// }
		//
		// }
		//
		// svc.getCorpus().computeNeighbors();
		//
		//
		// System.out.println("size: " + patterns.size());
		// System.out.println("size: " + getCA().getPatterns().size());
		// svc.getCorpus().initPatternDissimilarity();
		//
		// for(int i = 0 ; i < patterns.size(); ++i)
		// for(int j = 0 ; j < i ; ++j)
		// getCA().calculateDissimilarity(patterns.get(i),patterns.get(j));
		//
		//
		// svc.getCorpus().al_clusteringSolution.clear();
		//
		// HardClusteringSolution hcs =
		// toRemove("aretes_dans_solution_optimale_K_5.txt");
		// hcs.setMethodName("CP5");
		// svc.getCorpus().al_clusteringSolution.add(hcs);
		//
		// hcs = toRemove("aretes_dans_solution_optimale_K_20.txt");
		// hcs.setMethodName("CP20");
		// svc.getCorpus().al_clusteringSolution.add(hcs);
		//
		// hcs = toRemove("aretes_dans_solution_optimale_K_40.txt");
		// hcs.setMethodName("CP40");
		// svc.getCorpus().al_clusteringSolution.add(hcs);
		//
		// hcs = toRemove("aretes_dans_solution_optimale_K_60.txt");
		// hcs.setMethodName("CP60");
		// svc.getCorpus().al_clusteringSolution.add(hcs);
		//
		// ArrayList<HardClusteringSolution> al_hcs =
		// toRemove2("final_ap_final.txt", "AP");
		// svc.getCorpus().al_clusteringSolution.addAll(al_hcs);
		//
		// al_hcs = toRemove2("final_rock_final.txt", "R");
		// svc.getCorpus().al_clusteringSolution.addAll(al_hcs);
		//
		// updateClusters();
		//
		// // int min = Integer.MAX_VALUE;
		// // int[][] diss = new int[79][79];
		// //
		// // for(int i = 1 ; i < patterns.size() ; ++i) {
		// // for(int j = 0 ; j < i ; ++j) {
		// // diss[i][j] = patterns.get(i).dissimilarity(patterns.get(j),
		// getCA().getSMDissimilarity());
		// //
		// // if(diss[i][j] < min)
		// // min = diss[i][j];
		// // }
		// // }
		// //
		// // System.out.println("0");
		// // for(int i = 1 ; i < patterns.size() ; ++i) {
		// // for(int j = 0 ; j < i ; ++j) {
		// // System.out.print((diss[i][j]+Math.abs(min)) + " ");
		// // }
		// // System.out.println("0");
		// // }
		//
		// } catch (final java.io.IOException e) {
		// e.printStackTrace();
		// } catch (final ClassNotFoundException e) {
		// e.printStackTrace();
		// } finally {
		// try {
		// if (ois != null) {
		// ois.close();
		// }
		// } catch (final IOException ex) {
		// ex.printStackTrace();
		// }
		// }
		//
		//
		//
		// // ArrayList<ArrayList<Integer>> al = new
		// ArrayList<ArrayList<Integer>>();
		// // ArrayList<Integer> al2 = new ArrayList<Integer>();
		// // al2.add(getCA().getPatterns().size());
		// // al.add(al2);
		// //
		// // ArrayList<Pattern> pl = this.getCA().getPatterns();
		// //
		// //
		// // for(int i = pl.size()-1 ; i>=0; --i) {
		// //
		// // Pattern pi = pl.get(i);
		// //
		// // if(pi.toRemove != -1.0) {
		// //
		// // ArrayList<Integer> pouet = new ArrayList<Integer>();
		// // pi.toRemove = -1.0;
		// //
		// // for(int j = 0 ; j < i ; ++j) {
		// // Pattern pj = pl.get(j);
		// // if(pi.isIncludedIn(pj) && pj.isIncludedIn(pi)) {
		// // pouet.add(pj.toRemove_id);
		// // pj.toRemove = -1.0;
		// // }
		// // }
		// //
		// // if(pouet.size() != 0){
		// // pouet.add(pi.toRemove_id);
		// // al.add(pouet);
		// // }
		// // }
		// // }
		// //
		// // System.out.println(al);
		// //
		// // try{
		// // File f = new File("al.ser");
		// // FileOutputStream file = new FileOutputStream(f);
		// // ObjectOutputStream oos = new ObjectOutputStream(file);
		// // oos.writeObject(al);
		// // oos.flush();
		// // oos.close();
		// // }
		// // catch(java.io.IOException e2){
		// // e2.printStackTrace();
		// // }
		// //END TO REMOVE

		this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_VERT);		
		setVisible(true);
		
		currentInstance = this;
		Corpus.getCorpus().addObserver(this);
		SABRE.getInstance().addObserver(this);
		}
		catch(Exception e){e.printStackTrace();}

	}

//	private static class StandardViewHolder {
//		private final static StandardView sv = new StandardView();
//	}
//	
	

	public static StandardView getInstance() {
		return currentInstance;
	}
//	public static StandardView getInstance() {
//		return StandardViewHolder.sv;
//	}

	public void openSelectionFrame() {

		if (jf_s == null)
			jf_s = new SelectionPanel(this);
		else
			jf_s.setVisible(true);
	}

	public void updatePatterns() {
	}

	public void updateClusters(ClusteringSolution cs) {
		
		VisualisationPanel.getInstance().updateClusters(cs);
	}
	
	public StandardViewControler getControler() {
		return svc;
	}

	public void loadFile(File f) {
		try {

			FileInputStream file = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(file);

			Corpus c = (Corpus) ois.readObject();
			ois.close();

			c.addObserver(StandardView.this);
			//SABRE observer ?

			// TODO revoir le processus de mise à jour

			StandardView.this.setTitle(f.getName());

			/*
			 * Set the path if the similarity matrix has an empty path (possible
			 * in older versions)
			 */
			if (c.getAnnotationSimilarities().getPath() == null)
				c.getAnnotationSimilarities().setPath("");

			System.out.println(c.getPatternSize());

			// svc.getCorpus().getClusteringSolution().clear();
			//
			// String id = "d1_36";
			//
			// importHardClusteringSolution("corpus_a_evaluer/ap_" + id +
			// ".txt", "Affinity propagation");
			// importHierarchicalClusteringSolution("corpus_a_evaluer/chameleon_"
			// + id + ".txt", "Chameleon");
			// importHierarchicalClusteringSolution("corpus_a_evaluer/rock_" +
			// id + ".txt", "ROCK");
			// importHierarchicalClusteringSolution("corpus_a_evaluer/usc_" + id
			// + ".txt", "SC - Jordan & Weiss");
			// importHierarchicalClusteringSolution("corpus_a_evaluer/sm_" + id
			// + ".txt", "SC - Shi & Malik");
			// importHierarchicalClusteringSolution("corpus_a_evaluer/jw_" + id
			// + ".txt", "USC");
			// importHierarchicalClusteringSolution("corpus_a_evaluer//sl_" + id
			// + ".txt", "Single-Link");

			// StringBuffer buff = new StringBuffer();
			// for( int i = 0 ; i < c.getPatterns().size() ; i++ ){
			// for( int j = 0 ; j <= i ; j++){
			// Pattern p1 = c.getPatterns().get(i);
			// Pattern p2 = c.getPatterns().get(j);
			// buff.append(c.dissimilarity(p1, p2));
			// if(j!=i)
			// buff.append(" ");
			// }
			// buff.append("\n");
			// }
			//
			// System.out.println(buff.toString());
			//
			// try
			// {
			//
			// String outputFileDiss = "data/Resultats/result-" +
			// DateString.date() +"-dissimilarity.txt";
			// FileWriter fwDiss = new FileWriter(outputFileDiss, true);
			// BufferedWriter outputDiss = new BufferedWriter(fwDiss);
			// outputDiss.write(buff.toString());
			// outputDiss.close();
			// }
			// catch(IOException ioe){
			// System.out.print("Erreur : ");
			// ioe.printStackTrace();
			// }

			for (AnnotatedArray aa : Corpus.getCorpus().getAA())
				updateAddAA(aa);

			if (Corpus.getCorpus().getPatternSize() > 0)
				updatePatterns();

			for(ClusteringSolution cs : Corpus.getCorpus().getClusteringSolutions())
				updateClusters(cs);
			
			endOfClusteringProcess();

			updateSABREParameters();

			tab.setSelectedIndex(1);

			// CorpusAlignment corpus = svc.getCorpus();
			//
			// StringBuffer buff = new StringBuffer();
			// for( int i = 0 ; i < corpus.getPatterns().size() ; i++ ){
			// for( int j = 0 ; j <= i ; j++){
			// Pattern p1 = corpus.getPatterns().get(i);
			// Pattern p2 = corpus.getPatterns().get(j);
			// buff.append(corpus.dissimilarity(p1, p2));
			// if(j!=i)
			// buff.append(" ");
			// }
			// buff.append("\n");
			// }
			//
			// System.out.println(buff.toString());
			//
			// try
			// {
			//
			// String outputFileDiss = "data/Resultats/result-" +
			// DateString.date() +"-dissimilarity.txt";
			// FileWriter fwDiss = new FileWriter(outputFileDiss, true);
			// BufferedWriter outputDiss = new BufferedWriter(fwDiss);
			// outputDiss.write(buff.toString());
			// outputDiss.close();
			// }
			// catch(IOException ioe){
			// System.out.print("Erreur : ");
			// ioe.printStackTrace();
			// }
			//
		} catch (java.io.IOException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}

	}

	public JTabbedPane getTab() {
		return tab;
	}

	@Override
	public void abstractException(AbstractException e) {
		JOptionPane
				.showMessageDialog(
						this, e.defaultMessage(), "Warning",
						JOptionPane.WARNING_MESSAGE);
	}

	public void endOfClusteringProcess() {
		jdpb.setVisible(false);
		VisualisationPanel.getInstance().updateEndOfClusteringProcess();
		if(Corpus.getCorpus().getClusteringSolutions().size() > 0)
			tab.setSelectedIndex(1);
			
		this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH );
		
	}

	public void cancelProcess() {

		endOfClusteringProcess();
	}



	public void process_extraction_and_clustering() {

		VisualisationPanel.getInstance().reinitialize();
		jdpb = new JDialogProgressBar(this);
		jdpb.showThis();
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		this.processClusteringAfterExtraction = true;
		svc.executeExtraction();
	}

	public void process_extraction() {

		VisualisationPanel.getInstance().reinitialize();
		jdpb = new JDialogProgressBar(this);
		jdpb.showThis();
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		this.processClusteringAfterExtraction = false;
		svc.executeExtraction();
	}

	@Override
	public void endOfExtractionProcess() {
			
		if (this.processClusteringAfterExtraction) {
			svc.executeClustering(createClusteringMethods());
		} else {
			jdpb.setVisible(false);
			tab.setSelectedIndex(1);
			this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH );
			VisualisationPanel.getInstance().displayFirstFourPatterns();
		}
	}
	
	public ArrayList<AbstractClusteringMethod> createClusteringMethods(){

		ArrayList<AbstractClusteringMethod> clusteringToPerform = new ArrayList<AbstractClusteringMethod>();
		clusteringToPerform.add(new ROCK());
		clusteringToPerform.add(new SingleLink());
		return clusteringToPerform;
	}

	@Override
	public void updateScoreSimilarities(PositiveScoreTable p) {
		jf_s.updateScoreSimilarities(p);
	}

	@Override
	public void updateSABREParameters() {
		jf_s.updateSABREParameters(SABRE.getInstance().getParam());
	}

	public int getClusterNb() {
		return jf_s.getClusterNb();
	}

	@Override
	public void updateAddAA(AnnotatedArray aa) {

		jf_s.addAA(aa);
		VisualisationPanel.getInstance().addAA(aa);

	}

	@Override
	public void updateRemoveAA(int id) {

		jf_s.removeAA(id);
		VisualisationPanel.getInstance().removeAA(id);

	}

	@Override
	public void update(Observable arg0, Object arg1) {
	}

	@Override
	public void updateSwingWorker(SwingWorker<?, ?> sw) {
		jdpb.setSwingWorker(sw);
	}

	@Override
	public void updateMaxSimilarity(double d) {
		jf_s.updateMaxSimilarity(d);
	}

	@Override
	public void updateDesiredNumberOfAlignments(int v) {
		jf_s.updateDesiredNumberOfAlignments(v);
		
	}

	@Override
	public void removeClusteringSolution(int i) {
		VisualisationPanel.getInstance().removeClusteringSolution(i);
	}

	@Override
	public void clearClusters() {
		VisualisationPanel.getInstance().reinitialize();
	}
	


	@Override
	public void eventDispatched(AWTEvent event) {

		if (event instanceof KeyEvent) {
			KeyEvent keyEvent = (KeyEvent) event;
			if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL) {
				if (keyEvent.getID() == KeyEvent.KEY_PRESSED)
					isCtrlPressed = true;
				else if (keyEvent.getID() == KeyEvent.KEY_RELEASED)
					isCtrlPressed = false;
			}
		}
	}
	
	public boolean isCtrlPressed(){
		return isCtrlPressed;
	}

	public void addJSPTuto() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		JPanel jpTuto = new JPanel(new MigLayout("", "[]", "[][]10[][]"));
		JLabel jlTuto2 = new JLabel("<html><b>How to go to the next step</b></html>");

		jpTuto.add(jlTuto1, "wrap");
		jpTuto.add(jlTutoStepDescription, "wrap");
		jpTuto.add(jlTuto2, "wrap");
		jpTuto.add(jlTutoStepInstructions);

		jspTuto = new JScrollPane(jpTuto,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

//		jspTuto = new ScrollPane(jpTuto,
//				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
//                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		jspTuto.setPreferredSize(new Dimension(this.selectionPaneWidth - 20, this.selectionPaneHeight/3));
		jspTuto.setMaximumSize(new Dimension(width - 20, this.selectionPaneHeight/3));
		jspTuto.setMinimumSize(new Dimension(this.selectionPaneWidth  - 20, this.selectionPaneHeight/3));

//        jspTuto.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//        jspTuto.setVerticalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		jspTuto.setMaximumSize(new Dimension(width, height/2));

		this.add(jspTuto, BorderLayout.SOUTH);

	}


}
