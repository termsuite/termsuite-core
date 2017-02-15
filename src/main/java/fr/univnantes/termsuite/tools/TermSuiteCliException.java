package fr.univnantes.termsuite.tools;

public class TermSuiteCliException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TermSuiteCliException() {
		super();
	}

	public TermSuiteCliException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TermSuiteCliException(String message, Throwable cause) {
		super(message, cause);
	}

	public TermSuiteCliException(String message) {
		super(message);
	}

	public TermSuiteCliException(Throwable cause) {
		super(cause);
	}
}
