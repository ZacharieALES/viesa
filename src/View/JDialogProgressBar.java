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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class JDialogProgressBar extends JDialog implements PropertyChangeListener{

	private JProgressBar jpb;
	private JButton jb_cancel = new JButton("Cancel");
	
	/* Swing Worker which corresponds to the progress bar */
	private SwingWorker<?, ?> sw;
	
	public JDialogProgressBar(StandardView parent){
		super(parent,"");
			
		this.setSize(300, 110);
		this.setLocationRelativeTo(null);
		jpb = new JProgressBar(0, 100);
		
		jpb.setValue(0);
		jpb.setStringPainted(true);
		

		JPanel jp = new JPanel(new MigLayout("fill", "[center]", "[]10[]"));
		
		
		jb_cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				cancel();
				setVisible(false);
			}			
		});
		
		jp.add(jpb, "growx,wrap");
		jp.add(jb_cancel);
		
		this.setContentPane(jp);
	}
	
	public void cancel(){
		sw.cancel(false);
	}
	
	public void showThis(){
		this.setVisible(true);
	}

	public void setSwingWorker(SwingWorker<?, ?> sw) {
		this.sw = sw;
		sw.addPropertyChangeListener(this);
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if("progress".equals(evt.getPropertyName())){
			jpb.setValue((Integer)evt.getNewValue());
		}
		else if("description".equals(evt.getPropertyName())){
			this.setTitle(((String)evt.getNewValue()));
		}
	}
	
}
