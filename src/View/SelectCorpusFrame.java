package View;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;
import main.Main;
import main.MainCogniSismef;
import main.MainTutorial;
import model.Corpus;
import net.miginfocom.swing.MigLayout;

public class SelectCorpusFrame extends JFrame{

	public SelectCorpusFrame(){


		super("Welcome to VIESA");

		try {

			System.out.print(".");
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			System.out.println(".");

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}

		this.setLayout(new MigLayout("fill", "[]", "[]20[]10[]25[]10[]25[]10[]"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

		JLabel jlTitle= new JLabel("<html><body><b>Select the desired software version</b></body></html>",SwingConstants.CENTER);
		JLabel jlTuto = new JLabel("1 - Guided introduction of VIESA features"); 
		JLabel jlPrefilled = new JLabel("2 - Pre-filled versions of known data corpus");
		JLabel jlDefault = new JLabel("3 - Default version without any pre-filled information"); 


		JButton jbTuto = new JButton("Start the tutorial");
		JButton jbDefault = new JButton("Start the default version");
		JButton jbCogniCISMEF = new JButton("Open Cogni-CISMEF corpus");

		jbTuto.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainTutorial.run();
				
				SelectCorpusFrame.this.dispose();
			}
		});

		jbDefault.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

					int desiredNumberOfAlignments = 10;
					double gap = 10;

					SABRE.getInstance().setParam(new SABREParameter(gap, gap/2)); 
					Corpus.getCorpus().setDesiredNumberOfAlignments(desiredNumberOfAlignments);

					StandardView sv = StandardView.getInstance();
					Corpus.getCorpus().addObserver(sv);
					SABRE.getInstance().addObserver(sv);

					SelectCorpusFrame.this.dispose();
			}
		});
		
		jbCogniCISMEF.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				SelectCorpusFrame.this.dispose();
				MainCogniSismef.run();
			}
		});

		getContentPane().add(jlTitle, "center,wrap");
		getContentPane().add(jlTuto, "wrap");
		getContentPane().add(jbTuto, "center, wrap");
		getContentPane().add(jlPrefilled, "wrap");
		getContentPane().add(jbCogniCISMEF, "center");
//		getContentPane().add(jbCogniCISMEF, "center, wrap");
//		getContentPane().add(jlDefault, "wrap");
//		getContentPane().add(jbDefault, "center, wrap"); 

		//Display the window. 
		setLocationRelativeTo(null); 
		pack();
		setVisible(true); 
	}

} 

