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

import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class JIconButton extends JButton{

//	static final Logger log = Logger.getLogger("");
	
	public JIconButton(String s_iconPath){
			
		this.setImage(s_iconPath);
		this.setPreferredSize(new Dimension(17, 17));
		this.setMaximumSize(new Dimension(17, 17));
		this.setBorderPainted(false);
		this.setContentAreaFilled(false);
		this.setFocusPainted(false);
		
	}
	
	public JIconButton(String s_iconPath, int size_x, int size_y){
		
		this(s_iconPath);
		setSize(size_x, size_y);
		
	}
		
	public void setSize(int i, int j){
		this.setPreferredSize(new Dimension(i,j));
		this.setMaximumSize(new Dimension(i, j));
	}
	
	public void setImage(String sPath){
		if(ClassLoader.getSystemResource("img/exclamation.png") == null)
			System.err.println("Unreachable image");
		
		this.setIcon((new ImageIcon(ClassLoader.getSystemResource(sPath))));	
	}
}
