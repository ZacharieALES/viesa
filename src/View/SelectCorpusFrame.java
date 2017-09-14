package View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import extraction.SABRE;
import extraction.SABREParameter;
import main.MainCogniSismef;
import main.MainTutorial;
import main.MainVito;
import main.MainTutorial.StepWrapper;
import model.Corpus;
import net.miginfocom.swing.MigLayout;

public class SelectCorpusFrame extends JFrame{

	private static final long serialVersionUID = -4070320145031804723L;

	private final boolean useCogniCISMEF = false;
	private final boolean useVito = true;


	public SelectCorpusFrame(){


		super("VIESA");

		try {

			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

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

		this.setLayout(new MigLayout("fill", "[]", "[]20[]10[]10[]25[]10[]25[]10[]"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

		JLabel jlTitle= new JLabel("<html><body><b>Select the desired version</b></body></html>",SwingConstants.CENTER);
		JLabel jlTuto = new JLabel("1 - Guided introduction of VIESA features"); 
		JLabel jlPrefilled = new JLabel("2 - Pre-filled versions of known data corpus");
		JLabel jlDefault = new JLabel("3 - Default version without any pre-filled information"); 


		JButton jbTuto = new JButton("Start the tutorial from the beginning");
		JButton jbTutoSkipSteps = new JButton("Start the tutorial from a given step");
		JButton jbDefault = new JButton("Start the default version");

		jbTuto.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				MainTutorial.initialize();
				MainTutorial.run(0);

				SelectCorpusFrame.this.dispose();
			}
		});

		jbTutoSkipSteps.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				List<StepWrapper> hm = MainTutorial.initialize();
				JPanel panel = new JPanel(new MigLayout("fill", "[]", "[][]"));
				panel.add(new JLabel("Select in the box the tutorial step at which you want to start:"), "wrap");

				JComboBox<StepWrapper> jcb = new JComboBox<>();

				for(StepWrapper entry: hm)
					jcb.addItem(entry);

				panel.add(jcb, "center");
				JOptionPane.showMessageDialog(null,panel,"Step selection",JOptionPane.INFORMATION_MESSAGE);

				MainTutorial.run(hm.get(jcb.getSelectedIndex()).stepNb);

				SelectCorpusFrame.this.dispose();
			}
		});

		jbDefault.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				int desiredNumberOfAlignments = 10;
				double gap = 10;

				SABRE.getInstance().setParam(new SABREParameter(gap, gap/2, desiredNumberOfAlignments)); 

				new StandardView();

				SelectCorpusFrame.this.dispose();
			}
		});

		getContentPane().add(jlTitle, "center, wrap");
		getContentPane().add(jlTuto, "wrap");
		getContentPane().add(jbTuto, "center, wrap");
		getContentPane().add(jbTutoSkipSteps, "center, wrap");
		getContentPane().add(jlPrefilled, "wrap");

		if(useCogniCISMEF){

			JButton jbCogniCISMEF = new JButton("Open Cogni-CISMEF corpus");
			jbCogniCISMEF.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					SelectCorpusFrame.this.dispose();
					MainCogniSismef.run();
				}
			});
			
			getContentPane().add(jbCogniCISMEF, "center, wrap");
		}

		if(useVito){

			JButton jbVito = new JButton("Open Silent corpus");
			jbVito.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					SelectCorpusFrame.this.dispose();
					new MainVito();
				}
			});
			
			getContentPane().add(jbVito, "center, wrap");
		}
		
		getContentPane().add(jlDefault, "wrap");
		getContentPane().add(jbDefault, "center, wrap"); 

		//Display the window. 
		setLocationRelativeTo(null); 
		pack();
		setVisible(true); 
	}

} 

