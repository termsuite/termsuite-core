package fr.univnantes.termsuite.api;

import java.util.stream.Stream;

import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;

public interface TextualCorpus {
	
	/**
	 * The total number of documents
	 * 
	 * @return
	 * 		the total number of documents if known, <code>-1</code> otherwise
	 */
	int getNbDocuments();

	/**
	 * The total byte size of the corpus
	 * 
	 * @return
	 * 		the total byte size of the corpus if known, <code>-1</code> otherwise
	 */
	long getTotalSize();
	Stream<Document> documents();
	String readDocumentText(Document doc);
	Lang getLang();
}
