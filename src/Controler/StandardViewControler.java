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
import java.util.regex.Matcher;

import clustering.AbstractClusteringMethod;
import exception.UndefinedColumnFormatException;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;
import model.AnnotatedArray;
import model.Corpus;
import model.NumericalColumn;


public class StandardViewControler{
	

//	static final Logger log = Logger.getLogger("");
		
	public void addAA(String sPath, boolean h_header) {
		addAA(new File(sPath), h_header);
	}
	
	public void addAA(File f, boolean h_header){
		
		if(f.exists()){
			if(f.isDirectory()){	
				addAA(f.listFiles(), h_header);
			}
			else{
				
				/* Regexp to test that the end of the file end by "csv" */
				java.util.regex.Pattern csvRegexp = java.util.regex.Pattern.compile(".*csv");
	
				Matcher fileName = csvRegexp.matcher(f.getName());
				
				if(fileName.matches()){
					try {
						Corpus.getCorpus().add(f.getAbsolutePath(), h_header);
					} catch (UndefinedColumnFormatException e) {e.printStackTrace();}		
				}
				
			}
		}

	}
	
	public void addAA(File[] f, boolean h_header){
		
		for(int i = 0 ; i < f.length ; i++)
				addAA(f[i], h_header);
		
	}

	public void removeAA(AnnotatedArray aa){
		Corpus.getCorpus().remove(aa);
	}
	
	public void executeExtraction(){
		
		/* Extract patterns */
		Corpus.getCorpus().extractPatterns();
		
		System.out.println("SV: extract patterns");
		
	}
	
	public void executeClustering(ArrayList<AbstractClusteringMethod> clusteringToPerform){
		
		Corpus.getCorpus().clusterPatterns(clusteringToPerform);
		
	}
	
	public void setAnnotationSimilarities(File f){
		Corpus.getCorpus().setAnnotationSimilarities(new PositiveScoreTable(f.getAbsolutePath()));
	}
	
	public void setAnnotationSimilarities(PositiveScoreTable pst){
		Corpus.getCorpus().setAnnotationSimilarities(pst);
	}
	
	public void setGapCost(double gapcost){
		SABREParameter param = SABRE.getInstance().getParam();
		param.gap_cost = gapcost;
		SABRE.getInstance().notifyObserversParameters();
	}

	public void setDesynchCost(double desynchcost){
		SABREParameter param = SABRE.getInstance().getParam();
		param.desynch_cost = desynchcost;
		SABRE.getInstance().notifyObserversParameters();
	}

	public void setMaxSim(double d) {
		NumericalColumn.maxSim = d;
	}
	
	public void setDesiredNumberOfAlignments(int value){
		Corpus.getCorpus().setDesiredNumberOfAlignments(value);
	}
	
}
