package eu.project.ttc.termino.export;

import java.io.IOException;
import java.io.Writer;

import eu.project.ttc.api.JSONOptions;
import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.JSONTermIndexIO;

public class JsonExporter {

	public static void export(TermIndex termIndex, Writer writer) {
		export(termIndex, writer, new JSONOptions());
	}
	
	public static void export(TermIndex termIndex, Writer writer, JSONOptions options) {
		try {
			JSONTermIndexIO.save(writer, termIndex, options);
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
