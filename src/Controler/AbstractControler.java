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

package Controler;

import java.io.File;
import java.util.ArrayList;

import model.AnnotatedArray;
import model.Corpus;



public abstract class AbstractControler {
	
	protected Corpus corpus;
	
	public AbstractControler(Corpus c){
		this.corpus = c;
	}
	
	abstract void addAA(String sPath, boolean h_header, ArrayList<Integer> commentColumns, ArrayList<Integer> annotationColumns) ;
	abstract void addAA(File f, boolean h_header, ArrayList<Integer> commentColumns, ArrayList<Integer> annotationColumns) ;
	abstract void addAA(File[] f, boolean h_header, ArrayList<Integer> commentColumns, ArrayList<Integer> annotationColumns);
	abstract void removeAA(AnnotatedArray aa);
	abstract void process();
	abstract void setMinScore(int i);
}
