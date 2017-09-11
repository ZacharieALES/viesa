package exception;

public class InvalidInputFileException extends AbstractException{
	
	private static final long serialVersionUID = -1395818139524560482L;
	
	private String fileName;
	
	public InvalidInputFileException(String fileName){
		this.fileName = fileName;
	}

	@Override
	public String defaultMessage() {
		return "The first line of file "+ fileName + " returns null (may be caused by character \")";
	}

}
