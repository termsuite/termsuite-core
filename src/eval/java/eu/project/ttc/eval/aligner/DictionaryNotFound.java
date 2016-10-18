package eu.project.ttc.eval.aligner;

public class DictionaryNotFound extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;

	public DictionaryNotFound(String path) {
		super("Could not find dictionary: " + path);
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
