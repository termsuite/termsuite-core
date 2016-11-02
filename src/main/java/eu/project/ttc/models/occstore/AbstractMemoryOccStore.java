package eu.project.ttc.models.occstore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceStore;

public abstract class AbstractMemoryOccStore  implements OccurrenceStore {

	private Map<String, Document> documents = new HashMap<>();

	protected Document protectedGetDocument(String documentUrl) {
		if(!documents.containsKey(documentUrl))
			documents.put(documentUrl, new Document(documentUrl));
		return documents.get(documentUrl);
	}


	private static final String ERR_DOC_DOES_NOT_EXIST = "Document %s does not exists";
	
	@Override
	public Document getDocument(String url) {
		Preconditions.checkArgument(documents.containsKey(url), ERR_DOC_DOES_NOT_EXIST, url);
		return documents.get(url);
	}

	@Override
	public Collection<Document> getDocuments() {
		return documents.values();
	}
}
