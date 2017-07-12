package fr.univnantes.termsuite.api;

import java.nio.file.Path;

import fr.univnantes.termsuite.model.FileSystemCorpus;
import fr.univnantes.termsuite.model.Lang;

public class TXTCorpus extends FileSystemCorpus implements TextualCorpus {
	
	public static final String TXT_PATTERN = "**/*.txt";
	public static final String TXT_EXTENSION = "txt";
	
	public TXTCorpus(Lang lang, Path rootDirectory) {
		super(lang, rootDirectory, TXT_PATTERN, TXT_EXTENSION);
	}

}
