package gobbi.project.exceptions;

public class InvalidEmailSyntaxException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public InvalidEmailSyntaxException(String message) {
		super(message);
	}
}
