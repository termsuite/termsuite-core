package eu.project.ttc.tools.builders;

public class CorpusException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CorpusException() {
		super();
	}

	public CorpusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CorpusException(String message, Throwable cause) {
		super(message, cause);
	}

	public CorpusException(String message) {
		super(message);
	}

	public CorpusException(Throwable cause) {
		super(cause);
	}
}
