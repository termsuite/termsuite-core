package fr.univnantes.termsuite.api;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExtractorConfigIO {

	public static ExtractorOptions fromJson(InputStream stream) throws IOException {
		return new ObjectMapper().readValue(stream, ExtractorOptions.class);
	}
	
	public static ExtractorOptions fromJson(Path path) throws IOException {
		return new ObjectMapper().readValue(path.toFile(), ExtractorOptions.class);
	}

	public static void toJson(ExtractorOptions options, Path path) throws IOException {
		try (FileWriter writer = new FileWriter(path.toFile())) {
			JsonGenerator jg = new JsonFactory().createGenerator(writer);
			jg.useDefaultPrettyPrinter();
			new ObjectMapper().writeValue(jg, options);
		}
	}

	public static String toJson(ExtractorOptions options) {
		try {
			StringWriter writer = new StringWriter();
			JsonGenerator jg = new JsonFactory().createGenerator(writer);
			jg.useDefaultPrettyPrinter();
			new ObjectMapper().writeValue(jg, options);
			return writer.getBuffer().toString();
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
