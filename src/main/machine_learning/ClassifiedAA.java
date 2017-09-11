package main.machine_learning;

import java.io.Serializable;

import exception.CSVSeparatorNotFoundException;
import exception.InvalidCSVFileNumberOfColumnsException;
import exception.InvalidInputFileException;
import exception.InvalidNumberOfColumnsInInputFilesException;
import model.AAColumnFormat;
import model.AnnotatedArray;

public class ClassifiedAA extends AnnotatedArray implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6334551472046741475L;
	public String aa_class;
    
    public ClassifiedAA(){}

    public ClassifiedAA(String s_path, boolean h_header, AAColumnFormat aacf, String aa_class)
	throws CSVSeparatorNotFoundException, InvalidNumberOfColumnsInInputFilesException,
	       InvalidCSVFileNumberOfColumnsException, InvalidInputFileException {
	super(s_path, h_header, aacf);

	this.aa_class = aa_class;

    }



}