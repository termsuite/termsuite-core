package eu.project.ttc.api;

public class TermSuiteException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TermSuiteException() {
		super();
	}

	public TermSuiteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TermSuiteException(String message, Throwable cause) {
		super(message, cause);
	}

	public TermSuiteException(String message) {
		super(message);
	}

	public TermSuiteException(Throwable cause) {
		super(cause);
	}
}
