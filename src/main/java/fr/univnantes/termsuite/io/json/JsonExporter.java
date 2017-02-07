package fr.univnantes.termsuite.io.json;

import java.io.IOException;
import java.io.Writer;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.model.IndexedCorpus;

public class JsonExporter {

	private JsonOptions options;
	
	public JsonExporter(JsonOptions options) {
		super();
		this.options = options;
	}

	@Export
	public void export(IndexedCorpus indexedCorpus, Writer writer) {
		try {
			JsonTerminologyIO.save(writer, indexedCorpus, options);
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
