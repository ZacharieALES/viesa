package exception;

public class InvalidAnnotationIndex extends AbstractException{

	int index;
	
	public InvalidAnnotationIndex(int i){
		index = i;
	}
	
	@Override
	public String defaultMessage() {
		
		return "Invalid index: " + index;
	}

}
