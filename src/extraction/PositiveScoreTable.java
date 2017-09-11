package extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import exception.InvalidListToCreatePositiveScoreTable;
import model.Coordinate;
import model.Corpus;

/**
 * A positive table score is a table which contains the score between all pairs of annotations.
 * All the score are equal to zero except the ones specified in the input file.
 * @author zach
 *
 */
public class PositiveScoreTable{
	
	private String sPath;
	
//	/** Minimal positive value in the table */
//	private double minValue;
	
//	/** Minimal value in the table which is strictly positive */
//	private double minPositiveScore = -1;
	
	protected double[][] values;
	
	/**
	 * When creating a score table from a file, the score of some couples of annotations may be equal to 0.
	 * To facilitate an edition of the table, these values must be stored (otherwise the user would need to manually wrote the annotations again)
	 */
	public ArrayList<Coordinate<Short>> couplesWithZeroSimilarityInInputFile;

	
	public double get(Short i1, Short i2){
		if(i1 < values.length && i2 < values.length)
			return values[i1][i2];
		else
			return 0;
	}
	
	public void setPath(String s){
		sPath = s;
	}
	
	/**
	 * Return a reader for a csv file.
	 * Several cell separators are checked (',' ';' ...).
	 * To check a separator, we check if the first line of the file contains several columns when using this separtor.
	 * 
	 * @param filePath Path of the file
	 * @return A reader to the file or null if no proper separator is found
	 */
	CSVReader getReader(String filePath){

		ArrayList<Character> separator = new ArrayList<Character>();
		separator.add(',');
		separator.add(';');
		separator.add(' ');
		separator.add('\t');
		
		CSVReader reader = null;
		
		try {

			boolean fileInJar = true;
				
			InputStream is = getClass().getResourceAsStream(filePath);
			InputStreamReader isr = null;
			BufferedReader buf = null;
			
			if(is == null)
				fileInJar = false;
			else
				isr = new InputStreamReader(is);
			
			boolean separatorFound = false;
			int iSeparator = 0;

			/* While the separator have not been found, try the next one */
			while( !separatorFound && iSeparator < separator.size()){

				/* If the file is in the jar */
				if(fileInJar){
					buf = new BufferedReader(isr);
					reader = new CSVReader(buf, separator.get(iSeparator));
				}
				else
					reader = new CSVReader(new FileReader(sPath), separator.get(iSeparator));
							
				
				String [] headers = reader.readNext();

				/* If the separator is not valid */
				if(headers.length <= 1)
					iSeparator++;
				
				/* If the separator is valid */
				else
					separatorFound = true;	 
			}

			if(!separatorFound)
		    	reader = null;
	    }catch (IOException e) {e.printStackTrace();} 
		
		return reader;
	}
	

	/**
	 * Create a positive score table from a list of String[]
	 * @param list Each element in the list must contains exactly 3 values in the following order:
	 * 		- name of the first annotation;
	 * 		- name of the second annotation;
	 * 		- similarity between the two annotations
	 * 		The headers must not be in the list
	 * @throws InvalidListToCreatePositiveScoreTable 
	 */
	public PositiveScoreTable(List<ArrayList<String>> list) throws InvalidListToCreatePositiveScoreTable{
		
		try{

			addAllAnnotationsInCorpus(list);
			couplesWithZeroSimilarityInInputFile  = new ArrayList<Coordinate<Short>>();
			
			Corpus c = Corpus.getCorpus();

			/* Initialize all values to zero (only the value specified in the file will be positive */
			values = new double[c.getAnnotations().size()][c.getAnnotations().size()];

			for(int i = 0 ; i < c.getAnnotations().size() ; ++i) {
				for(int j = 0 ; j < c.getAnnotations().size() ; ++j)
					values[i][j]=0.0;
			}
				
			for(int i = 0 ; i < list.size() ; ++i){
				List<String> row = list.get(i);
				
				if(row.size() != 3){
					throw new InvalidListToCreatePositiveScoreTable(list, i);
				}
				else{
					
					double d;
					
					try{
						d = Double.parseDouble(row.get(2));
					}catch(NumberFormatException e){throw new InvalidListToCreatePositiveScoreTable(list, i);}

  					Short id1 = Corpus.getCorpus().getAnnotationIndex(row.get(0).trim());
  					Short id2 = Corpus.getCorpus().getAnnotationIndex(row.get(1).trim());

  					values[id1][id2] = d;
  					values[id2][id1] = d;
					
  					if(d <= 0.0)
  						couplesWithZeroSimilarityInInputFile.add(new Coordinate<Short>(id1, id2));
  					
				}
				
			}
		}
		catch(InvalidListToCreatePositiveScoreTable e){throw e;}
		
	}
	
	
	/**
	 * Create a score table from a file
	 * @param s_path Path of the file
	 */
	public PositiveScoreTable(String s_path){
		Corpus c = Corpus.getCorpus();
		sPath = s_path;
		
	    CSVReader reader = getReader(s_path);

	    if(reader != null){
	    	
	    	String [] currentLine = null;
	  	    this.addAllAnnotationsInCorpus(s_path);
	  	    couplesWithZeroSimilarityInInputFile = new ArrayList<Coordinate<Short>>();
	  	    
	  	    try {

  				/* Initialize all values to zero (only the value specified in the file will be positive */
  				values = new double[c.getAnnotations().size()][c.getAnnotations().size()];
  	
  				for(int i = 0 ; i < c.getAnnotations().size() ; ++i) {
  					for(int j = 0 ; j < c.getAnnotations().size() ; ++j)
  						values[i][j]=0;
  				}
  				
  				while((currentLine = reader.readNext()) != null) {

  					Short id1 = c.getAnnotationIndex(currentLine[0]);
  					Short id2 = c.getAnnotationIndex(currentLine[1]);
  					
//  					System.out.println(id1 + " - " + currentLine[0]);
//  					System.out.println(id2 + " - " + currentLine[1]);
  					double v= Double.parseDouble(currentLine[2]);

  					values[id1][id2] = v;
  					values[id2][id1] = v;
  					
  					if(v <= 0.0)
  						couplesWithZeroSimilarityInInputFile.add(new Coordinate<Short>(id1, id2));
//  System.out.println("(id1,id2,a1,a2,v): (" + id1 + "," + id2 + "," + currentLine[0] + ","  + currentLine[1] + "," + v + ")");					
  				}
	  			
	  		} catch (IOException e) {
	  		e.printStackTrace();
	  		}  	
	    }
	    else
	    	System.err.println("Error in PositiveScoreTable: no proper separator found for the file: " + s_path);
	   
	}

	
	public String getPath(){
		return sPath;
	}

	private void addAllAnnotationsInCorpus(List<ArrayList<String>> list)throws InvalidListToCreatePositiveScoreTable{
	
		try{
	    	for(int i = 0 ; i < list.size() ; ++i){
	    		
	    		if(list.get(i).size() != 3){
	    			throw new InvalidListToCreatePositiveScoreTable(list, i);
	    		}
	    		
	    		List<String> currentLine = list.get(i);
				Corpus.getCorpus().getAnnotationIndex(currentLine.get(0));
				Corpus.getCorpus().getAnnotationIndex(currentLine.get(1));
			}
		}catch(InvalidListToCreatePositiveScoreTable e){throw e;}
	}
	
	private void addAllAnnotationsInCorpus(String s_path){

	    CSVReader reader = getReader(s_path);

	    if(reader != null){
	    	
	    	try{
		    	/* First get the headers */
		    	String[] currentLine;

				while((currentLine = reader.readNext()) != null) {
	
					Corpus.getCorpus().getAnnotationIndex(currentLine[0]);
					Corpus.getCorpus().getAnnotationIndex(currentLine[1]);
				}
	    	}catch(Exception e){e.printStackTrace();}
	    }
			
		/* If less than one cell is found for the first line */
		else{
			System.err.println("Error: invalid number of cell in the first line of the file");
			System.exit(0);
		}
		
	}
	
	public int size(){
		return values.length;
	}
	

	public String toString(){
		
		String result = "";
		
		
		for(int i = 0 ; i < values.length ; ++i)
			result += "\t" + Corpus.getCorpus().getAnnotation(i);
		result += "\n";
		
		for(int i = 0 ; i < values.length ; ++i){
			
			result += Corpus.getCorpus().getAnnotation(i);
			
			for(int j = 0 ; j < values[i].length ; ++j){
//				System.out.println("taille: " + values[i].length + " (i,j) = (" + i + "," + j + ")");
				if(values[i][j] > 0.0)
					result += "\t" + values[i][j];
				else
					result += "\t-";
			}
			
			result += "\n";
			
		}
		
		return result;
				
	}

	public static void saveInFile(File selectedFile, List<ArrayList<String>> rowdata) throws InvalidListToCreatePositiveScoreTable{
		
		/* Check the input row data */
		boolean isValid = true;
		int row = -1;
		
		if(rowdata.size() == 0)
			isValid = false;
		
		int i = 0;
		while(isValid && i < rowdata.size()){
			
			if(rowdata.get(i).size() != 3){
				row = i;
				isValid = false;
			}
			
			++i;
			
		}
		
		if(!isValid)
			throw new InvalidListToCreatePositiveScoreTable(rowdata, row);
		
		 try{
		     FileWriter fw = new FileWriter(selectedFile, true);
		     BufferedWriter output = new BufferedWriter(fw);

		     output.write("Annotation1;Annotation2;Similarity\n");
		     
		     for(ArrayList<String> line : rowdata)
		    	 output.write(line.get(0) + ";" + line.get(1) + ";" + line.get(2) + "\n");
		     
		     output.flush();
		     output.close();
		 }
		 catch(IOException ioe){
		     ioe.printStackTrace();
		 }

	}
	
	public void setSimilarity(int i, int j, double d){
		
		int max = Math.max(i,  j);
		
		/* If one of the annotation index exceeds the size of the table
		 * This is possible if an annotation does not appear in the initial similarity score file used to initiation the positive score table.
		 */
		if(max >= values.length){
			double[][] newValues = new double[max+1][max+1];
			
			for(int k = 0 ; k < values.length ; k++)
				for(int l = 0 ; l < values.length ; l++)
					newValues[k][l] = values[k][l];
			
			values = newValues;
			
		}
		
		values[i][j] = d;
		values[j][i] = d;
	}
}
