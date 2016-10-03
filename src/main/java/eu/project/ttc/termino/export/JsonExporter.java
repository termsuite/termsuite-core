package eu.project.ttc.termino.export;

import java.io.IOException;
import java.io.Writer;

import eu.project.ttc.api.JsonOptions;
import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.JsonTermIndexIO;

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
