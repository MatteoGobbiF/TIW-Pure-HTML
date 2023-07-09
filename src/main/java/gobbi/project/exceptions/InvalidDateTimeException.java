package gobbi.project.exceptions;

public class InvalidDateTimeException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public InvalidDateTimeException(String message) {
		super(message);
	}
}
