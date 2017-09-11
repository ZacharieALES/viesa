package exception;

public class InvalidAnnotationIndex extends AbstractException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1193792788420708696L;
	int index;
	
	public InvalidAnnotationIndex(int i){
		index = i;
	}
	
	@Override
	public String defaultMessage() {
		
		return "Invalid index: " + index;
	}

}
