package fr.univnantes.termsuite.io;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;

public interface IndexedCorpusExporter {

	public void export(IndexedCorpus corpus, Writer writer);
	public String exportToString(IndexedCorpus corpus);
	public void export(IndexedCorpus corpus, Path path) throws IOException;

	public void export(Terminology termino, Writer writer);
	public String exportToString(Terminology termino);
	public void export(Terminology termino, Path path) throws IOException;

}
