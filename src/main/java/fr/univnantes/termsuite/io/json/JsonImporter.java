package fr.univnantes.termsuite.io.json;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import fr.univnantes.termsuite.io.IndexedCorpusImporter;
import fr.univnantes.termsuite.model.IndexedCorpus;

public class JsonImporter implements IndexedCorpusImporter {

	private JsonOptions options;
	
	public JsonImporter(JsonOptions options) {
		super();
		this.options = options;
	}

	@Override
	public IndexedCorpus importt(Reader reader) throws IOException {
		return JsonTerminologyIO.load(reader, options);
	}

	@Override
	public IndexedCorpus importFromString(String string) throws IOException {
		return importt(new StringReader(string));
	}

	@Override
	public IndexedCorpus importt(Path path) throws IOException {
		try(FileReader reader = new FileReader(path.toFile())) {
			return importt(reader);
		}
	}

}
