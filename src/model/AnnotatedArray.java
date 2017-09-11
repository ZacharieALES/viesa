package model;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import au.com.bytecode.opencsv.CSVReader;
import exception.CSVSeparatorNotFoundException;
import exception.InvalidAnnotatedArrayNumberOfColumnsException;
import exception.InvalidCSVFileNumberOfColumnsException;
import exception.InvalidInputFileException;
import exception.InvalidNumberOfColumnsInInputFilesException;


public class AnnotatedArray implements Serializable{

	public AnnotatedArray(){}

	public ArrayList<NumericalColumn> getNumerical_annotations() {
		return numerical_annotations;
	}

	@XmlTransient
	public void setNumerical_annotations(ArrayList<NumericalColumn> numerical_annotations) {
		this.numerical_annotations = numerical_annotations;
	}

	public ArrayList<AbstractColumn<?>> getCommentAndAnnotations() {
		return commentAndAnnotations;
	}

	@XmlTransient
	public void setCommentAndAnnotations(ArrayList<AbstractColumn<?>> commentAndAnnotations) {
		this.commentAndAnnotations = commentAndAnnotations;
	}

	@XmlTransient
	public void setAnnotations(ArrayList<AnnotationColumn> annotations) {
		this.annotations = annotations;
	}

	@XmlAttribute
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@XmlAttribute
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	private static final long serialVersionUID = 4052620824719751929L;

	/*
	 * One ArrayList for each column of annotation 
	 */
	protected ArrayList<AnnotationColumn> annotations;
	protected ArrayList<NumericalColumn> numerical_annotations;

	private ArrayList<AbstractColumn<?>> commentAndAnnotations;

	private String fileName = "unknown_file";
	private String fullPath;

	public AnnotatedArray(AnnotatedArray aa) {
		this.annotations = new ArrayList<AnnotationColumn>(aa.annotations);
		this.fileName = aa.fileName;
		this.fullPath = aa.fullPath;
		this.numerical_annotations = new ArrayList<NumericalColumn>(aa.numerical_annotations);
		this.commentAndAnnotations = new ArrayList<AbstractColumn<?>>(aa.commentAndAnnotations);
	}

	/**
	 * Create an AnnotatedArray from a csv file. The 1st line of the csv file
	 * may contain headers. The cells separator can be ',', ';', ' ' or ' '.
	 * Only the columns contains in <commentColumns> and <annotationsColumns>
	 * will be considered
	 * 
	 * @param s_path
	 *            Path of the csv file
	 * @param h_header
	 *            True if the csv file contain horizontal headers on the first
	 *            line
	 * @param commentColumns
	 *            Indice of the columns which contain the comments (the 1st
	 *            column of the file correspond to 0)
	 * @param annotationColumns
	 *            Columns which contains annotations (the 1st column of the file
	 *            correspond to 0)
	 * @param aacf 
	 * 		      Format of the annotation columns
	 * @throws CSVSeparatorNotFoundException
	 * @throws InvalidNumberOfColumnsInInputFilesException
	 * @throws InvalidCSVFileNumberOfColumnsException
	 * @throws InvalidInputFileException 
	 * @throws UndefinedAnnotationInSMException 
	 */
	public AnnotatedArray(String s_path, boolean h_header,
			AAColumnFormat aacf)
					throws CSVSeparatorNotFoundException,
					InvalidNumberOfColumnsInInputFilesException,
					InvalidCSVFileNumberOfColumnsException, InvalidInputFileException{

		ArrayList<Character> separator = new ArrayList<Character>();
		separator.add(';');
		separator.add(',');
		//		separator.add(' ');
		separator.add('\t');

		//TODO Gérer les fichiers vides

		/* Get the file name */
		fullPath = s_path;
		String[] temp = s_path.split("/");
		String [] temp2 = temp[temp.length - 1].split("\\\\");
		fileName = temp2[temp2.length - 1];

		CSVReader reader = null;
		String[] currentLine = null;

		try {

			boolean separatorFound = false;
			int iSeparator = 0;

			/* While the separator have not been found */
			while (!separatorFound && iSeparator < separator.size()) {

				/* Try the next one */
				//				reader = new CSVReader(new FileReader(s_path),
				//						separator.get(iSeparator));
				reader = new CSVReader(new InputStreamReader(new FileInputStream(s_path), StandardCharsets.UTF_8),
						separator.get(iSeparator));
				currentLine = reader.readNext();

				if(currentLine == null){
					reader.close();
					throw new InvalidInputFileException(s_path);
				}

				if (currentLine.length <= 1) {
					iSeparator++;
					//					reader = new CSVReader(new FileReader(s_path),
					//							separator.get(iSeparator));
					//					reader = new CSVReader(new InputStreamReader(new FileInputStream(s_path), StandardCharsets.UTF_8),
					//							separator.get(iSeparator));
				} else
					separatorFound = true;
			}

			if (separatorFound) {

				/* Create all the columns */
				commentAndAnnotations = aacf.createAllEmptyColumns();

				int lineSize = currentLine.length;

				int maxColumnIndexInCsvFile = -1;

				for(int i = 0 ; i < aacf.getTotalNumberOfColumns() ; ++i)
					if(aacf.getPositionOfColumnIInInputFile(i) > maxColumnIndexInCsvFile)
						maxColumnIndexInCsvFile = aacf.getPositionOfColumnIInInputFile(i);

				/* If the maximal index of a column in the csv file (for the column format) is lower than the number of columns in the current csv file */
				if(maxColumnIndexInCsvFile < currentLine.length){

					if(h_header){

						if(!Corpus.getCorpus().isColumnHeaderDefined()){

							ArrayList<String> columnHeaders = new ArrayList<String>();

							for(int i = 0 ; i < commentAndAnnotations.size() ; ++i)
								columnHeaders.add(currentLine[aacf.getPositionOfColumnIInInputFile(i)].trim());
							//							for(String s : currentLine)
							//								columnHeaders.add(s);

							Corpus.getCorpus().setColumnHeader(columnHeaders);
						}

						currentLine = reader.readNext();
					}

					/* For each line */
					do {

						if (currentLine.length != lineSize){
							throw new InvalidCSVFileNumberOfColumnsException(fileName, (commentAndAnnotations.size() + 1), currentLine.length, lineSize);
						}

						/* Fill each column */
						for(int i = 0 ; i < commentAndAnnotations.size() ; ++i)
							commentAndAnnotations.get(i).addElement(currentLine[aacf.getPositionOfColumnIInInputFile(i)].trim());

					} while ((currentLine = reader.readNext()) != null);

					fillAnnotationsColumnsFromAbstractColumns(commentAndAnnotations);
				}
				else
					throw  new InvalidNumberOfColumnsInInputFilesException(fileName, (maxColumnIndexInCsvFile + 1), (currentLine.length));

			} else {
				System.err.println("No proper separator has been found for the file (file name : "
						+ s_path + ")\n Tested separators : " + separator);
				throw new CSVSeparatorNotFoundException(fileName, separator);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void fillAnnotationsColumnsFromAbstractColumns(ArrayList<AbstractColumn<?>> al){

		annotations = new ArrayList<AnnotationColumn>();
		numerical_annotations = new ArrayList<NumericalColumn>();

		for(AbstractColumn<?> col : al){

			if(col instanceof AnnotationColumn)
				annotations.add((AnnotationColumn)col);
			else if(col instanceof NumericalColumn)
				numerical_annotations.add((NumericalColumn)col);

		}
	}

	public int getNumberOfAnnotationColumns(){
		return annotations.size() + numerical_annotations.size();
	}

	/**
	 * Create an AnnotatedArray object from an ArrayList<AbstractColumn<?>>
	 * @param track Contain the annotations and the comment
	 * @throws InvalidAnnotatedArrayNumberOfColumnsException 
	 */
	public AnnotatedArray(ArrayList<AbstractColumn<?>> track){

		fillAnnotationsColumnsFromAbstractColumns(track);

	}

	/**
	 * Display the annotated element
	 */
	public String display(){

		StringBuffer buff = new StringBuffer();

		if(commentAndAnnotations != null){

			/* For each line */
			for(int i = 0 ; i < commentAndAnnotations.get(0).values.size() ; ++i){

				/* For each annotation column*/
				for(int j = 0 ; j < commentAndAnnotations.size() ; j++)
					buff.append(commentAndAnnotations.get(j).toString(i));
				//					buff.append(commentAndAnnotations.get(i).toString(j));
				buff.append("\n");
			}
		}
		else
			buff.append("nullAA");

		return buff.toString();
	}


	/**
	 * Display the annotated element
	 */
	public String displayNumericalColumns(){

		StringBuffer buff = new StringBuffer();

		if(numerical_annotations != null && numerical_annotations.size() > 0){

			/* For each line */
			for(int i = 0 ; i < numerical_annotations.get(0).values.size() ; ++i){

				/* For each annotation column*/
				for(int j = 0 ; j < numerical_annotations.size() ; j++)
					buff.append(numerical_annotations.get(j).toString(i) + " ");
				buff.append("\n");
			}
		}
		else
			buff.append("AA without any numerical column");

		return buff.toString();

	}

	public String toString(){
		return this.fileName;
	}

	public AbstractColumn<?> getAnnotationColumn(int id){

		if(id < annotations.size())
			return annotations.get(id);
		else
			return numerical_annotations.get(id - annotations.size());

	}	

	public ArrayList<AnnotationColumn> getAnnotations(){
		return annotations;
	}

	public int getNumberOfLines(){

		int numberOfLines = 0;
		if(annotations.size() > 0)
			numberOfLines = annotations.get(0).getValues().size();
		else if(numerical_annotations.size() > 0)
			numberOfLines = numerical_annotations.get(0).getValues().size();
		return numberOfLines;
	}

	public ArrayList<NumericalColumn> getNumericalAnnotations(){
		return numerical_annotations;
	}

	public ArrayList<AnnotationColumn> getAnnotationColumn(){
		return annotations;
	}

	public String getAnnotation(int row, int col){

		return commentAndAnnotations.get(col).toString(row);

	}

	public boolean equals(Object obj){

		boolean result = false;

		if(obj==this)
			result = true;
		else{
			if(obj instanceof AnnotatedArray){

				AnnotatedArray other = (AnnotatedArray) obj;

				if(getNumberOfAnnotationColumns() == other.getNumberOfAnnotationColumns()){

					/* If the reference are not the same */
					if(this.annotations != other.annotations){
						if(annotations != null && this.annotations.equals(other.annotations)){
							result = true;
						}
					}
					else
						result = true;
				}
			}
		}

		return result;

	}

	public int hashCode(){

		int result = 11;
		final int multiplier = 19;

		result = multiplier*result+getNumberOfAnnotationColumns();
		result = multiplier*result + (annotations==null ? 0 : annotations.hashCode());

		return result;

	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * Test if the AnnotatedArray is compatible with a given column format
	 * @param aacf Column Format considered
	 * @return True if the annotated array is compatible with the format <aacf> ; False otherwise
	 */
	public boolean isCompatibleWith(AAColumnFormat aacf) {
		boolean isCompatible = true;		int i = 0;

		while(isCompatible && i < commentAndAnnotations.size()){

			/* If the class of column i of <this> is not the same than the class of column i of the column format */
			if(!aacf.getColumn(i).getClass().equals(commentAndAnnotations.get(i).getClass()))
				isCompatible = false;
			++i;
		}
		return isCompatible;
	}

	public int getNumberOfCommentColumns() {
		return this.commentAndAnnotations.size() - getNumberOfAnnotationColumns();
	}

	public String getFullPath(){
		return fullPath;
	}

}
