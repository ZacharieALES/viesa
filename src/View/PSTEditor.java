package View;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import exception.InvalidListToCreatePositiveScoreTable;
import extraction.PositiveScoreTable;
import main.Main;
import main.MainTutorial;
import model.AnnotationColumn;
import model.Corpus;
import net.miginfocom.swing.MigLayout;
import tuto.AbstractEditSimilarityStep;
import tuto.AbstractTutoStep;
import tuto.EditColumnSelection;
import tuto.EditSimilarity;
import tuto.EditSimilarityCombobox;

/**
 * Frame which enable to edit the positive score table. The modifications can be
 * saved only in viesa or in both viesa and a file.
 * 
 * @author zach
 *
 */
public class PSTEditor extends JDialog {

	private static final long serialVersionUID = 3009343889307416197L;
	List<PSTTable> lPsttable;

	JButton jb_ok;
	JPanel jpTable;

	public List<ArrayList<String>> previousdata;
	StandardView parent;

	private PositiveScoreTable result;

	public PSTEditor(StandardView parent) {

		super(parent, "Similarity scores", true);

		this.parent = parent;

		this.setLocationRelativeTo(null);

		if(!MainTutorial.IS_TUTO || !(MainTutorial.getCurrentStep() instanceof AbstractEditSimilarityStep))
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		jpTable = new JPanel(new MigLayout());

		/* Find the annotation cluster and create the tables */
		reinitializeTablePanel();

		/* Main panel of the JDialog */
		JPanel jp = new JPanel();
		jp.setLayout(new MigLayout("fill"));

		// psttable = new PSTTale(AnnotationColumn.pst);

		// psttable.getJSP().setMinimumSize(new Dimension(table_width,
		// Math.min(screen_height,table_height)));

		FocusListener fl = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				e.getComponent().repaint();
			}

			@Override
			public void focusLost(FocusEvent e) {
				e.getComponent().repaint();
			}
		};

		for (PSTTable table : lPsttable) {
			table.addFocusListener(fl);
			table.getJSP().addFocusListener(fl);
		}

		/* Create the data */
		previousdata = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentLine;

		/* For each line in the table */
		for (short i = 1; i < AnnotationColumn.pst.size(); ++i) {

			currentLine = new ArrayList<String>();
			currentLine.add(Corpus.getCorpus().getAnnotation(i));

			/* For each non empty column in this line */
			for (short j = 1; j <= i; ++j) {

				/*
				 * If the value associated to annotations <i> and <j> is greater
				 * than 0
				 */
				double v = AnnotationColumn.pst.get(i, j);

				if (v > 0.0)
					currentLine.add(((Double) v).toString());
				else
					currentLine.add("-");
			}
			for (int j = i + 1; j < Corpus.getCorpus().getNumberOfAnnotations(); ++j)
				currentLine.add("");
			previousdata.add(currentLine);
		}

		/* For each annotation which does not appear in the table */
		for (int i = AnnotationColumn.pst.size(); i < Corpus.getCorpus().getNumberOfAnnotations(); ++i) {

			currentLine = new ArrayList<String>();
			currentLine.add(Corpus.getCorpus().getAnnotation(i));

			/* For each column in the table */
			for (int j = 1; j <= i; ++j)
				currentLine.add("-");
			for (int j = i + 1; j < Corpus.getCorpus().getNumberOfAnnotations(); ++j)
				currentLine.add("");
			previousdata.add(currentLine);
		}

		jb_ok = new JButton("OK");
		// JButton jb_ok_plus_save = new JButton("OK + Save as");
		JButton jb_cancel = new JButton("Cancel");
		JHelpButton jhb_help = new JHelpButton(parent,
				"<html>This window is used to modify the similarity scores. <br><br>"
						+ "The cells of the table can be edited by double-clicking on them.<br><br>" + "Buttons:<br>"
						+ "- OK : save the modification in the current session;<br>"
						+ "- OK and save as: also enable to save the scores in a file.</html>");

		jb_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String newTableName = "";

				/*
				 * Get the name of the new table created by the validation of
				 * the modifications by the user
				 */
				String newTablePath = Main.getCustomTableName(Main.getLatestCustomTableNumber() + 1);
				File f = new File(newTablePath);

				// if(!MainTutorial.IS_TUTO)
				// {
				// /* Save the file */
				// try {
				// PositiveScoreTable.saveInFile(f, getCorrespondingALAL());
				// } catch (InvalidListToCreatePositiveScoreTable e1) {
				// e1.printStackTrace();
				// }
				// }

				/* Get the canonical name of the table */
				String[] s = newTablePath.split("/");
				newTableName = (s[s.length - 1]);

				PSTEditor.this.confirm(newTableName);
			}
		});

		// jb_ok_plus_save.addActionListener(new ActionListener(){
		// public void actionPerformed(ActionEvent e){
		//
		// JFileChooser chooser = new JFileChooser();
		// chooser.setMultiSelectionEnabled(false);
		// chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		//
		// int returnVal = chooser.showOpenDialog(PSTEditor.this.parent);
		//
		// String tableName = "Custom table";
		//
		// if(returnVal == JFileChooser.APPROVE_OPTION){
		// try {
		// PositiveScoreTable.saveInFile(chooser.getSelectedFile(),
		// getCorrespondingALAL());
		// tableName = chooser.getSelectedFile().getName();
		// Corpus.getCorpus().results.addAction(new
		// ValidateColumnsChangesAction());
		// } catch (InvalidListToCreatePositiveScoreTable e1) {
		// PSTEditor.this.parent.abstractException(e1);
		// }
		// }
		//
		// PSTEditor.this.confirm(tableName);
		// }
		// });

		jb_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});

		/* Tables JPanel */
		JScrollPane jspTables = new JScrollPane(jpTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspTables.getVerticalScrollBar().setUnitIncrement(10);
		jspTables.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		/* Combobox JPanel */
		JPanel jpCB = new JPanel(new MigLayout("", "[]", "[][][][]"));

		jpCB.setBorder(BorderFactory.createTitledBorder("Set the similarity of two annotations in different tables"));

		String[] annotations = new String[Corpus.getCorpus().getNumberOfAnnotations() - 1];
		for (int i = 1; i < Corpus.getCorpus().getNumberOfAnnotations(); ++i)
			annotations[i - 1] = Corpus.getCorpus().getAnnotation(i);

		final JComboBox<String> jcb1 = new JComboBox<>(annotations);
		jcb1.setSelectedIndex(1);

		final JComboBox<String> jcb2 = new JComboBox<>(annotations);
		jcb2.setSelectedIndex(2);

		final JTextField jtf = new JTextField("0.0");
		jtf.setMinimumSize(new Dimension(100, 10));

		JButton jbAddSimilarity = new JButton("Add inter-tables similarity");
		jbAddSimilarity.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					/* Try to parse the string */
					Double d = Double.parseDouble(jtf.getText());

					int i1 = jcb1.getSelectedIndex();
					int i2 = jcb2.getSelectedIndex();

					String a1 = Corpus.getCorpus().getAnnotation(i1 + 1);
					String a2 = Corpus.getCorpus().getAnnotation(i2 + 1);

					AbstractTutoStep ads = MainTutorial.getCurrentStep();

					// System.out.println("Try to set similarity of: " +
					// Corpus.getCorpus().getAnnotation(i1 + 1) + "," +
					// Corpus.getCorpus().getAnnotation(i2 + 1) + " to " + d);

					/* If it is not the tuto */
					if (!MainTutorial.IS_TUTO

							/* Or if the current tuto step is null */
							|| ads == null

					/*
					 * Or if the current tuto step is an edition of through a
					 * combobox
					 */
							|| ((ads instanceof EditSimilarityCombobox || ads instanceof EditSimilarity)

									/*
									 * and it corresponds to an expected edition
									 */
									&& (((AbstractEditSimilarityStep) ads).removeEditOperationIfExpected(a1, a2, d)))) {

						AnnotationColumn.pst.setSimilarity(i1 + 1, i2 + 1, d);
						AnnotationColumn.pst.setSimilarity(i1 + 1, i2 + 1, d);
						reinitializeTablePanel();

						if (MainTutorial.IS_TUTO && ((AbstractEditSimilarityStep) ads).loperations.size() == 0)
							activateOKButton();
					} else if (MainTutorial.IS_TUTO) {
						AbstractEditSimilarityStep est = (AbstractEditSimilarityStep) ads;
						JOptionPane.showMessageDialog(PSTEditor.this, est.errorMessage());
					}

					// System.out.println("1: " +
					// (((EditSimilarityCombobox)ads).valueToSet == d));
					// System.out.println("2: " +
					// ((EditSimilarityCombobox)ads).annotation1.equals(a1));
					// System.out.println("3: " +
					// ((EditSimilarityCombobox)ads).annotation2.equals(a2));
					// System.out.println("4: " +
					// ((EditSimilarityCombobox)ads).annotation1.equals(a2));
					// System.out.println("5: " +
					// ((EditSimilarityCombobox)ads).annotation2.equals(a1));
					//

				} catch (NumberFormatException e2) {
				}
			}
		});

		jpCB.add(jcb1);
		jpCB.add(jcb2);
		jpCB.add(jtf);
		jpCB.add(jbAddSimilarity);

		/* Button JPanel */
		JPanel jp_button = new JPanel(new MigLayout("", "push[][]", ""));
		jp_button.add(jb_ok);
		// jp_button.add(jb_ok_plus_save);
		jp_button.add(jb_cancel);
		jp_button.add(jhb_help);

		jp.add(jspTables, "growx, wrap");
		jp.add(jpCB, "wrap");
		jp.add(jp_button, "wrap");

		if (MainTutorial.IS_TUTO && MainTutorial.getCurrentStep() != null
				&& MainTutorial.getCurrentStep() instanceof AbstractEditSimilarityStep) {

			AbstractTutoStep ads = MainTutorial.getCurrentStep();

			JPanel jpTuto = new JPanel(new MigLayout("", "[]", "[][]10[][]"));

			JLabel jlTutoStepDescription = new JLabel();
			JLabel jlTutoStepInstructions = new JLabel();

			JLabel jlTuto1 = new JLabel("<html><b>Description (step " + (MainTutorial.currentStepId) + "/"
					+ (MainTutorial.lSteps.size() - 1) + ")</b></html>");
			JLabel jlTuto2 = new JLabel("<html><b>How to go to the next step</b></html>");

			jlTutoStepDescription.setText("<html>" + ads.description() + "</html>");
			jlTutoStepInstructions.setText("<html>" + ads.instructions() + "</html>");

			jb_ok.setEnabled(false);
			jb_cancel.setEnabled(false);

			jpTuto.add(jlTuto1, "wrap");
			jpTuto.add(jlTutoStepDescription, "wrap");
			jpTuto.add(jlTuto2, "wrap");
			jpTuto.add(jlTutoStepInstructions);

			jp.add(jpTuto);

		}

		this.setContentPane(jp);

	}

	/**
	 * Find the annotation clusters and create the tables
	 */
	protected void reinitializeTablePanel() {

		/*
		 * Cluster the annotations (an annotation has a similarity > 0 with at
		 * least one annotation of its cluster)
		 */
		Union_find uf = new Union_find(Corpus.getCorpus().getNumberOfAnnotations());

		for (short i = 1; i < AnnotationColumn.pst.size(); ++i) {

			/* For each non empty column in this line */
			for (short j = 1; j <= i; ++j) {

				/*
				 * If the value associated to annotations <i> and <j> is greater
				 * than 0
				 */
				double v = AnnotationColumn.pst.get(i, j);

				if (v > 0.0) {
					uf.union(i, j);
					// System.out.println("Union: " +
					// Corpus.getCorpus().getAnnotation(i) + ", " +
					// Corpus.getCorpus().getAnnotation(j));
				}
			}
		}

		ArrayList<ArrayList<Integer>> clusteredAnnotations = uf.getClusters();

		int biggestTableSize = 0;

		lPsttable = new ArrayList<>();

		for (ArrayList<Integer> cluster : clusteredAnnotations)
			/*
			 * If the cluster is not empty and does not contains the empty
			 * annotation and if it is reduced to one node then its similarity
			 * with itself is not equal to 0.0
			 */
			if (cluster.size() > 0 && cluster.get(0) != 0 && (cluster.size() > 1
					|| AnnotationColumn.pst.get(cluster.get(0).shortValue(), cluster.get(0).shortValue()) > 0.0)) {
				lPsttable.add(new PSTTable(cluster, this));

				if (cluster.size() > biggestTableSize) {
					biggestTableSize = cluster.size();
				}
			}

		/* Set the size of the JDialog */
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int screen_height = (int) screenSize.getHeight();
		int screen_width = (int) screenSize.getWidth();

		/* +1 because the first column contains the name of the annotations */
		int table_width = screen_width;// Math.min(screen_width, 85 *
										// (biggestTableSize + 1));
		int table_height = screen_height - 50;// Math.min(screen_height,23*(cumulativeSize
												// - 1));

		this.setSize(table_width, table_height);

		jpTable.removeAll();
		for (PSTTable table : lPsttable)
			jpTable.add(table.getJSP(), "wrap");

		jpTable.revalidate();
		jpTable.repaint();
	}

	public PositiveScoreTable showThis() {
		this.setVisible(true);
		return result;
	}

	public List<ArrayList<String>> getCorrespondingALAL() {

		List<ArrayList<String>> data = new ArrayList<>();

		for (PSTTable table : lPsttable)
			for (short i = 0; i < table.annotationIndex.size(); ++i)
				for (short j = 1; j <= i + 1; ++j) {

					String s = table.rowdata.get(i).get(j);

					/*
					 * If the value associated to annotations <i> and <j> is a
					 * double greater than 0
					 */
					try {

						/* Try to parse the string */
						Double.parseDouble(s);

						ArrayList<String> newValues = new ArrayList<>();
						newValues.add(table.rowdata.get(i).get(0));
						newValues.add(table.rowdata.get(j - 1).get(0));
						newValues.add(s);

						data.add(newValues);

					} catch (NumberFormatException e) {
					}

				}

		return data;

	}

	private void confirm(String path) {

		try {

			/*
			 * Change the focus to save the modification of the last edited cell
			 * (if it has not been validated by pressing enter or changing the
			 * focus)
			 */
			for (PSTTable table : lPsttable) {
				table.requestFocus();
				table.editCellAt(0, 0);

				result = new PositiveScoreTable(getCorrespondingALAL());
				result.setPath(path);
			}

		} catch (InvalidListToCreatePositiveScoreTable e) {
			parent.abstractException(e);
		}

		setVisible(false);

	}

	public void activateOKButton() {
		jb_ok.setEnabled(true);
	}

}
