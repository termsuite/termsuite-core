package fr.univnantes.termsuite.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Charsets;

import fr.univnantes.termsuite.index.JsonTerminologyIO;
import fr.univnantes.termsuite.model.Terminology;

public class TerminologyIO {
	
	public static void toJson(Terminology termino, Writer writer) throws IOException {
		toJson(termino, writer, new JsonOptions());
	}
	
	public static void toJson(Terminology termino, Writer writer, JsonOptions options) throws IOException {
		JsonTerminologyIO.save(writer, termino, options);
	}
	
	public static Terminology fromJson(String filePath, JsonOptions options) {
		return fromJson(Paths.get(filePath), options);
	}

	public static Terminology fromJson(String filePath) {
		return fromJson(filePath, new JsonOptions());
	}

	public static Terminology fromJson(Path path) {
		return fromJson(path, new JsonOptions());
	}
	
	public static Terminology fromJson(Path path, JsonOptions options) {
		try {
			return fromJson(path.toUri().toURL(), options);
		} catch (MalformedURLException e) {
			throw new TermSuiteException(e);
		}
	}

	public static Terminology fromJson(URL terminoUrl) {
		return fromJson(terminoUrl, new JsonOptions());
	}

	public static Terminology fromJson(URL terminoUrl, JsonOptions options) {
		try {
			return JsonTerminologyIO.load(
				new InputStreamReader(terminoUrl.openStream(), Charsets.UTF_8),
				options
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
