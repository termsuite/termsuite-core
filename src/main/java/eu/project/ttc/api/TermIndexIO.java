package eu.project.ttc.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;

import com.google.common.base.Charsets;

import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.JSONTermIndexIO;

public class TermIndexIO {
	
	public static void toJson(TermIndex termIndex, Writer writer) throws IOException {
		toJson(termIndex, writer, new JSONOptions());
	}
	
	public static void toJson(TermIndex termIndex, Writer writer, JSONOptions options) throws IOException {
		JSONTermIndexIO.save(writer, termIndex, options);
	}

	public static void toTbx(TermIndex termIndex, Writer writer) {
		
	}

	public static void toTsv(TermIndex termIndex, Writer writer) {
		toTsv(termIndex, writer, new TSVOptions());
	}

	public static void toTsv(TermIndex termIndex, Writer writer, TSVOptions options) {
		
	}

	public static TermIndex fromJson(URL termIndexUrl) {
		return fromJson(termIndexUrl, new JSONOptions());
	}

	public static TermIndex fromJson(URL termIndexUrl, JSONOptions options) {
		try {
			return JSONTermIndexIO.load(
				new InputStreamReader(termIndexUrl.openStream(), Charsets.UTF_8),
				options
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
