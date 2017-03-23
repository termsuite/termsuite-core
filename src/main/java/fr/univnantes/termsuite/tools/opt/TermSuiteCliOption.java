package fr.univnantes.termsuite.tools.opt;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.univnantes.termsuite.engines.contextualizer.AssociationRate;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.model.TermProperty;

public enum TermSuiteCliOption implements CliOption {
	/*
	 * GENERAL PURPOSE OPTIONS
	 */
	HELP("help", "h", OptType.T_NONE, "Print a help message."),
	LOG_INFO("info", null, OptType.T_NONE, "Activate logging (by default to console)"),
	LOG_TO_FILE("log-file", null, OptType.T_FILE, "Write log messages to given file"),
	LOG_DEBUG("debug", null, OptType.T_NONE, "Activate verbose logging for debugging purpose"),

	
	/*
	 * PREPROCESSOR OPTIONS
	 */
	LANGUAGE(		"language", 	"l", 		OptType.T_LANG, "Language of the input corpus"),
	ENCODING(		"encoding", 	"e", 		OptType.T_ENC, 	"Encoding of the input corpus"),
	TAGGER(			"tagger", 		null, 		OptType.T_STRING, "Which POS tagger to use. Allowed are: " + Tagger.stream().map(Tagger::getShortName).collect(joining(", "))),
	TAGGER_PATH(	"tagger-home",	"t", 		OptType.T_FILE, 	"Path to POS tagger's home"),
	FROM_TXT_CORPUS_PATH("from-text-corpus", "c", OptType.T_DIR, "Directory to corpus (containing a list of .txt documents)"),
	WATCH(			"watch", 		null, 		OptType.T_TERM_LIST, 	"List of terms (grouping keys or lemmas) to log to output"),
	RESOURCE_DIR(	"resource-dir", null, 		OptType.T_DIR, 	"Custom resource directory"),
	RESOURCE_JAR(	"resource-jar", null, 		OptType.T_FILE, 	"Custom resource jar"),
	RESOURCE_URL_PREFIX("resource-url-prefix", null, OptType.T_STRING, "Custom resource url prefix"),
	PREPARED_TERMINO_JSON("json", 	null, 		OptType.T_FILE, 	"Path to JSON indexed corpus file where all occurrences are imported to"),
	CAS_TSV(		"tsv-anno", 	null, 		OptType.T_DIR, 	"Path to TSV export directory of all spotted term annotations"),
	CAS_JSON(		"json-anno", 	null, 		OptType.T_DIR, 	"Path to JSON export directory of all spotted term annotations"),
	CAS_XMI(		"xmi-anno", 	null, 		OptType.T_DIR, 	"Path to XMI export directory of all spotted term annotations"),
	
	
	/*
	 * EXTRACTOR OPTIONS
	 */
	
	// CORPUS_LOADING_OPTIONS
	FROM_PREPARED_CORPUS_PATH("from-prepared-corpus", null, OptType.T_DIR, "A file or directory path. Starts the terminology extraction pipeline from an XMI corpus or an imported terminology json file instead of a txt corpus."),
	
	
	// PRE_FILTER
	PRE_FILTER_PROPERTY("pre-filter-property", null, OptType.T_STRING, "Enables pre-gathering filtering based on given property. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	PRE_FILTER_KEEP_VARIANTS("pre-filter-keep-variants", null, OptType.T_NONE, "Keep variants during pre-gathering filtering even if they are to be filtered"),
	PRE_FILTER_MAX_VARIANTS_NUM("pre-filter-max-variants", null, OptType.T_INT, "The maximum number of variants to keep during pre-gathering filtering"),
	PRE_FILTER_THRESHOLD("pre-filter-th", null, OptType.T_INT_OR_FLOAT, "Threshold value of pre-gathering filter"),
	PRE_FILTER_TOP_N("pre-filter-top-n", null, OptType.T_INT, "N value for pre-gathering filtering over top N terms"),
	
	
	// CONTEXTUALIZER
	CONTEXTUALIZER_ENABLED(	"contextualize", 		null, OptType.T_NONE, 	"Activates the contextualizer"),
	CONTEXTUALIZER_SCOPE(	"context-scope", 		null, OptType.T_INT, 	"Radius of single-word term window used during contextualization"),
	CONTEXTUALIZER_MIN_COOC_TH("context-coocc-th", 	null, OptType.T_INT_OR_FLOAT, 	"Sets a minimum frequency threshold for co-terms to appear in context vectors"),
	CONTEXTUALIZER_ASSOC_RATE("context-assoc-rate", 	null, OptType.T_INT_OR_FLOAT, 	"Association rate measure used to normalize context vectors. Allowed values are: " + Arrays.stream(AssociationRate.values()).map(Class::getSimpleName).collect(joining(","))),

	
	// MORPHOLOGY ANALYZER
	MORPHOLOGY_DISABLED("disable-morphology", 				null, OptType.T_NONE, "Disable morphology analysis (native, prefix, derivation splitting)"),
	MORPHOLOGY_PREFIX_DISABLED("disable-prefix-splitting", 	null, OptType.T_NONE, "Disable morphological prefix splitting"),
	MORPHOLOGY_DERIVATIVE_DISABLED("disable-derivative-splitting", null, OptType.T_NONE, "Disable morphological derivative splitting"),
	MORPHOLOGY_NATIVE_DISABLED("disable-native-splitting", 	null, OptType.T_NONE, "Disable morphological native splitting"),

	
	// GATHERER 
	GATHERER_ENABLE_SEMANTIC("enable-semantic-gathering", 		null, 	OptType.T_NONE, 	"Enable semantic term gathering (monolingual alignment)"),
	GATHERER_DISABLE_MERGER("disable-merging", 					null, 	OptType.T_NONE, 	"Disable graphical term merging"),
	GATHERER_SEMANTIC_SIMILAIRTY_TH("semantic-similarity-th", 	null, 	OptType.T_INT_OR_FLOAT, 	"Minimum semantic similarity threshold for semantic gathering (monolingual alignment)"),
	GATHERER_GRAPH_SIMILARITY_THRESHOLD("graphical-similarity-th", 	null, OptType.T_INT_OR_FLOAT, 	"Graphical similarity threshold"),
	GATHERER_SEMANTIC_NB_CANDIDATES("nb-semantic-candidates", 	null, 	OptType.T_INT, 	"Max number of semantic variants for each terms"),
	GATHERER_SEMANTIC_DISTANCE("semantic-distance", 			null, 	OptType.T_INT_OR_FLOAT, 	"Similarity measure used for semantic alignment. Allowed values: " + Arrays.stream(SimilarityDistance.values()).map(Class::getSimpleName).collect(joining(", "))),


	// POST-PROCESSOR OPTIONS
	POSTPROC_DISABLED("disable-post-processing", 				null, OptType.T_NONE, "Disable post-gathering scoring and filtering processings"),
	POSTPROC_INDEPENDANCE_TH("postproc-independance-th", 		null, OptType.T_INT_OR_FLOAT, "Term independance score threshold. Terms under threshold are filtered out."),
	POSTPROC_VARIATION_SCORE_TH("postproc-variation-score-th", 	null, OptType.T_INT_OR_FLOAT, "Filters out variations with scores under given threshold"),
	
	
	// POST_FILTER
	POST_FILTER_PROPERTY("post-filter-property", 			null, OptType.T_STRING, "Enables post-gathering filtering based on given property. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	POST_FILTER_KEEP_VARIANTS("post-filter-keep-variants", 	null, OptType.T_NONE, "Keep variants during post-gathering filtering even if they are to be filtered"),
	POST_FILTER_MAX_VARIANTS_NUM("post-filter-max-variants", null, OptType.T_INT, "The maximum number of variants to keep during post-gathering filtering"),
	POST_FILTER_THRESHOLD("post-filter-th", 				null, OptType.T_INT_OR_FLOAT, "Threshold value of post-gathering filter"),
	POST_FILTER_TOP_N("post-filter-top-n", 					null, OptType.T_INT, "N value for post-gathering filtering over top N terms"),

	
	// RANKING
	RANKING_DESC_PROPERTY("ranking-desc", null, 	OptType.T_STRING, "Sets the output ranking property in DESCENDING order. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	RANKING_ASC_PROPERTY("ranking-asc", 	null, 	OptType.T_STRING, "Sets the output ranking property in ASCENDING order. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	
	// OUTPUT
	TSV(	"tsv", 		null, 	OptType.T_FILE, 	"Outputs terminology to TSV file"),
	JSON(	"json", 	null, 	OptType.T_FILE, 	"Outputs terminology to JSON file"),
	TBX(	"tbx", 		null, 	OptType.T_FILE, 	"Outputs terminology to XMI file"),
	
	
	/*
	 * TSV OPTIONS
	 */
	TSV_PROPERTIES(	"tsv-properties", 		null, 	OptType.T_STRING, 	"The comma-separated list columns of the tsv file. Allowed values are: " + properties().stream().collect(joining(","))),
	TSV_HIDE_HEADER("tsv-hide-headers", 	null, 	OptType.T_NONE, 	"Hide column headers"),
	TSV_HIDE_VARIANTS("tsv-hide-variants",	null, 	OptType.T_NONE, 	"Does no show the variants for each term"),
	
	
	
	/*
	 * ALIGNER
	 */
	SOURCE_TERMINO("source-termino", 	null, OptType.T_FILE, "The source terminology (indexed corpus)"),
	TARGET_TERMINO("target-termino", 	null, OptType.T_FILE, "The source terminology (indexed corpus)"),
	TERM(			"term", 			null, OptType.T_TERM_LIST, "The source term (lemma or grouping key) to translate"),
	TERM_LIST(		"term-list", 		null, OptType.T_FILE, "The path to a list of source terms (lemmas or grouping keys) to translate"),
	DICTIONARY(		"dictionary", 		null, OptType.T_FILE, "The path to the bilingual dictionary to use for bilingual alignment"),
	N(				"n", 				"n", OptType.T_INT, "The number of translation candidates to show in the output"),
	MIN_CANDIDATE_FREQUENCY("min-candidate-frequency", null, OptType.T_INT, "The minimum frequency of target translation candidates"),
	DISTANCE(		"distance", 		null, OptType.T_INT_OR_FLOAT, "Similarity measure used for context vector alignment. Allowed values: " + Arrays.stream(SimilarityDistance.values()).map(Class::getSimpleName).collect(joining(", "))),
	EXPLAIN(		"explain", 			null, OptType.T_NONE, "Shows for each aligned term the most influencial co-terms"), 
	ALIGNER_TSV(	"tsv", 				null, OptType.T_FILE, "A file path to write output of the bilingual aligner")
	
	
	;
	
	private String optName;
	private String optShortName;
	private OptType argType;
	private String description;
	
	private TermSuiteCliOption(String optName, String optShortName, OptType argType, String description) {
		this.optName = optName;
		this.optShortName = optShortName;
		this.argType = argType;
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.tools.opt.CliOption#getOptName()
	 */
	@Override
	public String getOptName() {
		return optName;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.tools.opt.CliOption#getArgType()
	 */
	@Override
	public OptType getArgType() {
		return argType;
	}
	
	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.tools.opt.CliOption#getOptShortName()
	 */
	@Override
	public String getOptShortName() {
		return optShortName;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.tools.opt.CliOption#hasArg()
	 */
	@Override
	public boolean hasArg() {
		return argType != OptType.T_NONE;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.tools.opt.CliOption#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	private static final List<String> properties() {
		List<String> properties = new ArrayList<>();
		
		for(TermProperty p:TermProperty.values())
			properties.add(p.getShortName());
		for(RelationProperty p:RelationProperty.values())
			properties.add(p.getShortName());
		
		return properties;
	}

}
