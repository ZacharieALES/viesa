package exception;

/**
 * Exception thrown while creating an AAColumnFormat when the number of columns in argument does not correspond to the number of index of this column in the input files 
 * (to each column must correspond exactly one index of column in the input files)
 * or when the number of columns is equal to 0
 * @author zach
 *
 */
public class InvalidArgumentsToCreateAnAAColumnFormat extends Exception{

	private static final long serialVersionUID = 4565676801468657309L;
	
	public InvalidArgumentsToCreateAnAAColumnFormat(String message){
		super(message);
	}

}
