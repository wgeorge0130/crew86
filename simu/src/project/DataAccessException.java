package project;

public class DataAccessException extends RuntimeException {
	private static final long serialVersionUID = 223654549329206850L;
	public DataAccessException() {
		super();
	}
	public DataAccessException(String message) {
		super(message);

	}
}