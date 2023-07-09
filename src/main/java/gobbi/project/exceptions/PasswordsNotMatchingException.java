package gobbi.project.exceptions;

public class PasswordsNotMatchingException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public PasswordsNotMatchingException(String message) {
		super(message);
	}
}

