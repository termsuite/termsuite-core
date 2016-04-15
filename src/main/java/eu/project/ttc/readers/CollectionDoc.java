package eu.project.ttc.readers;

/**
 * An interface of input documents sent to collection readers
 * 
 * @author Damien Cram
 * 
 * @see StreamingCollectionReader
 *
 */
public interface CollectionDoc {

	/**
	 * The unique identifier of the document.
	 * 
	 * @return the uri
	 */
	public String getUri();
	
	/**
	 * The context of the text.
	 * 
	 * @return
	 */
	public String getText();

}
