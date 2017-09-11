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

package util.springbox;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import exception.CSVSeparatorNotFoundException;

import au.com.bytecode.opencsv.CSVReader;

public class AAReader {
	CSVReader reader;
	private String s_path;
	private int width;
	ArrayList<Character> separators;
	Character separator;

	public AAReader(String path)  throws IOException, CSVSeparatorNotFoundException {
		this.s_path = path;
		separators = new ArrayList<Character>();
		separators.add(';');
		separators.add(',');
		separators.add(' ');
		separators.add('\t');
		if(!"".equals(s_path)){
			this.separate();
			
		}
		else {
			throw new IOException("Path is not defined");
		}
		
	}
	
	private void separate() throws IOException, CSVSeparatorNotFoundException{
		boolean separatorFound = false;
		int iSeparator = 0;
		
		while(!separatorFound && iSeparator < separators.size()){

			reader = new CSVReader(new FileReader(s_path), separators.get(iSeparator));
					
			String[] firstLine = reader.readNext();
			
			if(firstLine.length <= 1){
				
				iSeparator++;
//				reader = new CSVReader(new FileReader(s_path), separators.get(iSeparator));

			}
			else{
				separatorFound = true;
				this.width = firstLine.length;
			}
			
		}
		
		if(iSeparator < separators.size())
			this.separator = separators.get(iSeparator);
		else
			throw new CSVSeparatorNotFoundException(s_path, separators);
	}
	
	public List<String[]> read() throws IOException { 
		
		List<String[]> myEntries = null;
		reader = new CSVReader(new FileReader(s_path), separator);
		myEntries = reader.readAll();
		return myEntries;
	}
	
	public int getWidth(){
		return width;
	}
}
