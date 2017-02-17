package fr.univnantes.termsuite.io.json;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import fr.univnantes.termsuite.io.IndexedCorpusImporter;
import fr.univnantes.termsuite.model.IndexedCorpus;

public class JsonLoader implements IndexedCorpusImporter {

	private JsonOptions options;
	
	public JsonLoader(JsonOptions options) {
		super();
		this.options = options;
	}

	@Override
	public IndexedCorpus load(Reader reader) throws IOException {
		return JsonTerminologyIO.load(reader, options);
	}

	@Override
	public IndexedCorpus loadFromString(String string) throws IOException {
		return load(new StringReader(string));
	}

	@Override
	public IndexedCorpus load(Path path) throws IOException {
		try(FileReader reader = new FileReader(path.toFile())) {
			return load(reader);
		}
	}

}
