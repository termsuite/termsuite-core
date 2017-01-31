package fr.univnantes.termsuite.export.json;

import java.io.IOException;
import java.io.Writer;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.index.JsonTerminologyIO;
import fr.univnantes.termsuite.model.IndexedCorpus;

public class JsonExporter {

	public static void export(IndexedCorpus indexedCorpus, Writer writer) {
		export(indexedCorpus, writer, new JsonOptions());
	}
	
	public static void export(IndexedCorpus indexedCorpus, Writer writer, JsonOptions options) {
		try {
			JsonTerminologyIO.save(writer, indexedCorpus, options);
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
