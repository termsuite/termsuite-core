package fr.univnantes.termsuite.api;

import java.util.stream.Stream;

import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;

public interface TextualCorpus {
	Stream<Document> documents();
	String readDocumentText(Document doc);
	Lang getLang();
}
