package fr.univnantes.termsuite.api;

import java.nio.file.Path;

import fr.univnantes.termsuite.model.Lang;

public class TextCorpus extends Corpus {
	
	public static final String TXT_PATTERN = "**/*.txt";
	public static final String TXT_EXTENSION = "txt";

	public TextCorpus(Lang lang, Path rootDirectory) {
		super(lang, rootDirectory, TXT_PATTERN, TXT_EXTENSION);
	}
}
