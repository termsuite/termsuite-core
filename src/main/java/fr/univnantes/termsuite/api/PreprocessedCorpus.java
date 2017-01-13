package fr.univnantes.termsuite.api;

import java.nio.file.Path;

import fr.univnantes.termsuite.model.Lang;

public class PreprocessedCorpus extends Corpus {
	
	public static final String XMI_PATTERN = "**/*.xmi";
	public static final String XMI_EXTENSION = "xmi";
	public static final String JSON_PATTERN = "**/*.json";
	public static final String JSON_EXTENSION = "json";

	public PreprocessedCorpus(Lang lang, Path rootDirectory, String pattern, String extension) {
		super(lang, rootDirectory, pattern, extension);
	}
}
