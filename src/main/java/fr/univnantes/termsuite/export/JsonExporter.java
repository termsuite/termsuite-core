package fr.univnantes.termsuite.export;

import java.io.IOException;
import java.io.Writer;

import fr.univnantes.termsuite.api.JsonOptions;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.termino.JsonTermIndexIO;

public class JsonExporter {

	public static void export(TermIndex termIndex, Writer writer) {
		export(termIndex, writer, new JsonOptions());
	}
	
	public static void export(TermIndex termIndex, Writer writer, JsonOptions options) {
		try {
			JsonTermIndexIO.save(writer, termIndex, options);
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
