package project;

public class DivideByZeroException extends RuntimeException{
	private static final long serialVersionUID = 2663580265549951229L;
	public DivideByZeroException() {
		super();
	}
	public DivideByZeroException(String message) {
		super(message);

	}
}