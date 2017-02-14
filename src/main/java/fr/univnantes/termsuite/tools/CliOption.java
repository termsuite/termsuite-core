package fr.univnantes.termsuite.tools;

import static java.util.stream.Collectors.joining;

import fr.univnantes.termsuite.model.Tagger;

public enum CliOption {
	LANGUAGE(		"language", 	"l", 		true, 	"Language of the input corpus"),
	ENCODING(		"encoding", 	"e", 		true, 	"Encoding of the input corpus"),
	TAGGER(			"tagger", 		null, 		true, 	"Which POS tagger to use. Allowed are: " + Tagger.stream().map(Tagger::getShortName).collect(joining(", "))),
	TAGGER_PATH(	"tagger-home",	"t", 		true, 	"Path to POS tagger's home"),
	CORPUS_PATH(	"corpus", 		"c", 		true, 	"Directory to corpus (containing a list of .txt documents"),
	WATCH(			"watch", 		null, 		true, 	"List of terms (grouping keys or lemmas) to log to output"),
	RESOURCE_DIR(	"resource-dir", null, 		true, 	"Custom resource directory"),
	RESOURCE_JAR(	"resource-jar", null, 		true, 	"Custom resource jar"),
	RESOURCE_URL_PREFIX("resource-url-prefix", null, true, "Custom resource url prefix"),
	PREPARED_TERMINO_JSON("json", 	null, 		true, 	"Path to JSON indexed corpus file where all occurrences are imported to"),
	CAS_TSV(		"tsv-anno", 	null, 		true, 	"Path to TSV export directory of all spotted term annotations"),
	CAS_JSON(		"json-anno", 	null, 		true, 	"Path to JSON export directory of all spotted term annotations"),
	CAS_XMI(		"xmi-anno", 	null, 		true, 	"Path to XMI export directory of all spotted term annotations")
	;
	
	public String optName;
	public String optShortName;
	public boolean hasArg;
	public String description;
	
	private CliOption(String optName, String optShortName, boolean hasArg, String description) {
		this.optName = optName;
		this.optShortName = optShortName;
		this.hasArg = hasArg;
		this.description = description;
	}

	public String getOptName() {
		return optName;
	}

	public String getOptShortName() {
		return optShortName;
	}

	public boolean hasArg() {
		return hasArg;
	}

	public String getDescription() {
		return description;
	}
}
