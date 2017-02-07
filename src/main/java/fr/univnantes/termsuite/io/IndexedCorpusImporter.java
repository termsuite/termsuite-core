package fr.univnantes.termsuite.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import fr.univnantes.termsuite.model.IndexedCorpus;

public interface IndexedCorpusImporter {

	public IndexedCorpus importt(Reader reader) throws IOException;
	public IndexedCorpus importFromString(String string) throws IOException;
	public IndexedCorpus importt(Path path) throws IOException;
}
