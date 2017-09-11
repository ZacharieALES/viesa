package exception;

public class UndefinedColumnFormatException extends Exception{

	private static final long serialVersionUID = -5282321880915033623L;

	public UndefinedColumnFormatException(){
		super("The column format must be defined prior to add annotated elements in the corpus.");
	}
}
