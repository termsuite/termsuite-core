package eu.project.ttc.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	
	public static TermIndex fromJson(String filePath, JSONOptions options) {
		return fromJson(Paths.get(filePath), options);
	}

	public static TermIndex fromJson(String filePath) {
		return fromJson(filePath, new JSONOptions());
	}

	public static TermIndex fromJson(Path path) {
		return fromJson(path, new JSONOptions());
	}
	
	public static TermIndex fromJson(Path path, JSONOptions options) {
		try {
			return fromJson(path.toUri().toURL(), options);
		} catch (MalformedURLException e) {
			throw new TermSuiteException(e);
		}
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
