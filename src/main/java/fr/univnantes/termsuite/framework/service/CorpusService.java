package fr.univnantes.termsuite.framework.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.stream.Stream;

import fr.univnantes.termsuite.api.Corpus;
import fr.univnantes.termsuite.api.CorpusMetadata;
import fr.univnantes.termsuite.api.TextCorpus;
import fr.univnantes.termsuite.model.Document;

public interface CorpusService {

	Path getTargetDocumentPath(Corpus targetCorpus, Corpus sourceCorpus, Path sourceDocumentPath);

	String readDocumentText(Document document, Charset encoding) throws IOException;

	String cleanRawText(String rawText);

	CorpusMetadata computeMetadata(TextCorpus corpus) throws IOException;

	Stream<Document> documents(TextCorpus corpus);

}