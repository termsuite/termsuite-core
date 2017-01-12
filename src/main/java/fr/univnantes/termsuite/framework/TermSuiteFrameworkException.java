package fr.univnantes.termsuite.framework;

public class TermSuiteFrameworkException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TermSuiteFrameworkException() {
		super();
	}

	public TermSuiteFrameworkException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TermSuiteFrameworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public TermSuiteFrameworkException(String message) {
		super(message);
	}

	public TermSuiteFrameworkException(Throwable cause) {
		super(cause);
	}
}
