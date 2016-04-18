package eu.project.ttc.readers;

/**
 * An interface of input documents sent to collection readers
 * 
 * @author Damien Cram
 * 
 * @see StreamingCollectionReader
 *
 */
public interface CollectionDocument {

	/**
	 * The document sentinelle  sent to the reader to stipulate that the stream is ended
	 * 
	 */
	public static final CollectionDocument LAST_DOCUMENT = new CollectionDocument() {
		@Override
		public String getUri() {
			return "http://termsuite.github.io/documents/last";
		}
		@Override
		public String getText() {
			return null;
		}
	};

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
