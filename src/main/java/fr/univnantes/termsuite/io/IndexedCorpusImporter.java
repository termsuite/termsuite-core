package fr.univnantes.termsuite.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import fr.univnantes.termsuite.model.IndexedCorpus;

public interface IndexedCorpusImporter {

	public IndexedCorpus load(Reader reader) throws IOException;
	public IndexedCorpus loadFromString(String string) throws IOException;
	public IndexedCorpus load(Path path) throws IOException;
}
