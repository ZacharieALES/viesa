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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import clustering.ClusteringSolution;

/**
 * Combobox which represent a set of cluster set. The cluster sets can be hard (ie : one solution) or hierarchical (a set of solutions)
 * @author zach
 *
 */
public class CSComboBox extends JComboBox<ClusteringSolution>{

	private static final long serialVersionUID = 1L;

	private DefaultComboBoxModel<ClusteringSolution> cscb = new DefaultComboBoxModel<ClusteringSolution>();
	
	public CSComboBox(){
		
		this.setModel(cscb);
	}
	
	public void addCS(ClusteringSolution cs){
		cscb.addElement(cs);
		cscb.setSelectedItem(cs);
	}
	
//	public void setCS(List<ClusteringSolution> al_cs){		
//
//		cscb.removeAllElements();
//		
//		if(al_cs != null && al_cs.size() > 0){
//
//			cscb.addElement(al_cs.get(0));
//			cscb.setSelectedItem(al_cs.get(0));
//			
//			
//			for(int i = 1 ; i < al_cs.size() ; i++)
//				cscb.addElement(al_cs.get(i));
//			
//		}
//		
//	}
	
	public void reinitialize(){
		cscb.removeAllElements();
	}
	
}
