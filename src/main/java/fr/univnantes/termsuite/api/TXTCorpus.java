package fr.univnantes.termsuite.api;

import java.nio.file.Path;
import java.util.stream.Stream;

import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.FileSystemCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.uima.readers.StringPreparator;

public class TXTCorpus extends FileSystemCorpus implements TextualCorpus {
	
	public static final String TXT_PATTERN = "**/*.txt";
	public static final String TXT_EXTENSION = "txt";

	public TXTCorpus(Lang lang, Path rootDirectory) {
		super(lang, rootDirectory, TXT_PATTERN, TXT_EXTENSION);
	}

	@Override
	public Stream<Document> documents() {
		return pathWalker(
				getRootDirectory(), 
				getPattern(), 
				path -> new Document(getLang(),  path.toString()));
	}

	@Override
	public String readDocumentText(Document doc) {
		return cleanRawText(readFileContent(doc));
	}
	
	public String cleanRawText(String rawText) {
		StringPreparator stringPreparator = new StringPreparator();
		return stringPreparator.prepare(rawText);
	}
}
