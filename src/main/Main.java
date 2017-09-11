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

package main;

import java.io.File;

import View.SelectCorpusFrame;

//import View.ColumnView;


public class Main {

//	static final Logger log = Logger.getLogger("");
	
	//TODO ajouter un bouton cluster utilisable apres avoir extrait
	//TODO fermer barre chargement si 0 patterns apres clustering
	
	public static boolean hasHeader;
	public static String scoreFolder = "";
	public static final String SCORES_DEFAULT = "initial_scores.csv";
	public static final String SCORES_CUSTOM_PREFIX = "custom_scores";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new SelectCorpusFrame();

//		MainDogma.run();
//		MainCogniSismef.run();
//		MainChanoni.run();
		
//		new MainMohamed();

//		new MainChanoniTestLPCA4D().run();
		
	}
	

	public static String getLatestScoreTablePath(){
		
		String result = scoreFolder + "/" + Main.SCORES_DEFAULT;
		
		int i = getLatestCustomTableNumber();
		
		if(i > 0)
			result = getCustomTableName(i);
		
		return result;
		
	}
	
	/** Return the number of custom tables created by the user (a custom table is created each time the user validate some changes in the scores) */
	public static int getLatestCustomTableNumber(){

		int i = 1;
		File f= new File(getCustomTableName(i));
		
		while(f.exists()){
			i++;
			f= new File(getCustomTableName(i));
		}
		
		return i-1;
		
	}
	
	public static String getCustomTableName(int i){
		return scoreFolder + "/" + Main.SCORES_CUSTOM_PREFIX + "_" + i + ".csv";
	}

	public static void getNextCustomTableName() {
//		int i = getLatestCustomTableNumber()+1;
		
	}
	
	
}
