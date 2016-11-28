package fr.univnantes.termsuite.eval.exceptions;

public class DictionaryNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;

	public DictionaryNotFoundException(String path) {
		super("Could not find dictionary: " + path);
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
