package fr.univnantes.termsuite.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Charsets;

import fr.univnantes.termsuite.io.json.JsonOptions;
import fr.univnantes.termsuite.io.json.JsonTerminologyIO;
import fr.univnantes.termsuite.model.IndexedCorpus;

public class IndexedCorpusIO {
	
	public static void toJson(IndexedCorpus termino, Path path) throws IOException {
		toJson(termino, path, new JsonOptions());
	}
	
	public static void toJson(IndexedCorpus termino, Path path, JsonOptions options) throws IOException {
		JsonTerminologyIO.save(new OutputStreamWriter(
			    new FileOutputStream(path.toFile()), Charsets.UTF_8), termino, options);
	}
	
	public static void toJson(IndexedCorpus termino, String path) throws IOException {
		toJson(termino, Paths.get(path), new JsonOptions());
	}
	
	public static void toJson(IndexedCorpus termino, String path, JsonOptions options) throws IOException {
		toJson(termino, Paths.get(path), options);
	}

	public static IndexedCorpus fromJson(String filePath, JsonOptions options) {
		return fromJson(Paths.get(filePath), options);
	}

	public static IndexedCorpus fromJson(String filePath) {
		return fromJson(filePath, new JsonOptions());
	}

	public static IndexedCorpus fromJson(Path path) {
		return fromJson(path, new JsonOptions());
	}
	
	public static IndexedCorpus fromJson(Path path, JsonOptions options) {
		try {
			return fromJson(path.toUri().toURL(), options);
		} catch (MalformedURLException e) {
			throw new TermSuiteException(e);
		}
	}

	public static IndexedCorpus fromJson(URL terminoUrl) {
		return fromJson(terminoUrl, new JsonOptions());
	}

	public static IndexedCorpus fromJson(URL terminoUrl, JsonOptions options) {
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
