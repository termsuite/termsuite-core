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

public enum CliOption {
	/*
	 * GENERAL PURPOSE OPTIONS
	 */
	HELP("help", "h", null, "Print a help message."),
	
	
	/*
	 * PREPROCESSOR OPTIONS
	 */
	LANGUAGE(		"language", 	"l", 		"LANG", "Language of the input corpus"),
	ENCODING(		"encoding", 	"e", 		"ENC", 	"Encoding of the input corpus"),
	TAGGER(			"tagger", 		null, 		"STRING", "Which POS tagger to use. Allowed are: " + Tagger.stream().map(Tagger::getShortName).collect(joining(", "))),
	TAGGER_PATH(	"tagger-home",	"t", 		"FILE", 	"Path to POS tagger's home"),
	FROM_TXT_CORPUS_PATH("from-text-corpus", "c", "DIR", "Directory to corpus (containing a list of .txt documents"),
	WATCH(			"watch", 		null, 		"STRING", 	"List of terms (grouping keys or lemmas) to log to output"),
	RESOURCE_DIR(	"resource-dir", null, 		"DIR", 	"Custom resource directory"),
	RESOURCE_JAR(	"resource-jar", null, 		"FILE", 	"Custom resource jar"),
	RESOURCE_URL_PREFIX("resource-url-prefix", null, "STRING", "Custom resource url prefix"),
	PREPARED_TERMINO_JSON("json", 	null, 		"FILE", 	"Path to JSON indexed corpus file where all occurrences are imported to"),
	CAS_TSV(		"tsv-anno", 	null, 		"DIR", 	"Path to TSV export directory of all spotted term annotations"),
	CAS_JSON(		"json-anno", 	null, 		"DIR", 	"Path to JSON export directory of all spotted term annotations"),
	CAS_XMI(		"xmi-anno", 	null, 		"DIR", 	"Path to XMI export directory of all spotted term annotations"),
	
	
	/*
	 * EXTRACTOR OPTIONS
	 */
	
	// CORPUS_LOADING_OPTIONS
	FROM_PREPARED_CORPUS_PATH("from-prepared-corpus", null, "DIR", "A file or directory path. Starts the terminology extraction pipeline from an XMI corpus or an imported terminology json file instead of a txt corpus."),
	
	
	// PRE_FILTER
	PRE_FILTER_PROPERTY("pre-filter-property", null, "STRING", "Enables pre-gathering filtering based on given property. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	PRE_FILTER_KEEP_VARIANTS("pre-filter-keep-variants", null, null, "Keep variants during pre-gathering filtering even if they are to be filtered"),
	PRE_FILTER_MAX_VARIANTS_NUM("pre-filter-max-variants", null, "INT", "The maximum number of variants to keep during pre-gathering filtering"),
	PRE_FILTER_THRESHOLD("pre-filter-th", null, "INT|FLOAT", "Threshold value of pre-gathering filter"),
	PRE_FILTER_TOP_N("pre-filter-top-n", null, "INT", "N value for pre-gathering filtering over top N terms"),
	
	
	// CONTEXTUALIZER
	CONTEXTUALIZER_ENABLED(	"contextualize", 		null, null, 	"Activates the contextualizer"),
	CONTEXTUALIZER_SCOPE(	"context-scope", 		null, "INT", 	"Radius of single-word term window used during contextualization"),
	CONTEXTUALIZER_MIN_COOC_TH("context-coocc-th", 	null, "INT|FLOAT", 	"Sets a minimum frequency threshold for co-terms to appear in context vectors"),
	CONTEXTUALIZER_ASSOC_RATE("context-assoc-rate", 	null, "FLOAT", 	"Association rate measure used to normalize context vectors. Allowed values are: " + Arrays.stream(AssociationRate.values()).map(Class::getSimpleName).collect(joining(","))),

	
	// MORPHOLOGY ANALYZER
	MORPHOLOGY_DISABLED("disable-morphology", null, null, "Disable morphology analysis (native, prefix, derivation splitting)"),
	MORPHOLOGY_PREFIX_DISABLED("disable-prefix-splitting", null, null, "Disable morphological prefix splitting"),
	MORPHOLOGY_DERIVATIVE_DISABLED("disable-derivative-splitting", null, null, "Disable morphological derivative splitting"),
	MORPHOLOGY_NATIVE_DISABLED("disable-native-splitting", null, null, "Disable morphological native splitting"),

	
	// GATHERER 
	GATHERER_ENABLE_SEMANTIC("enable-semantic-gathering", 		null, 	null, 	"Enable semantic term gathering (monolingual alignment)"),
	GATHERER_DISABLE_MERGER("disable-merging", 					null, 	null, 	"Disable graphical term merging"),
	GATHERER_SEMANTIC_SIMILAIRTY_TH("semantic-similarity-th", 	null, 	"FLOAT", 	"Minimum semantic similarity threshold for semantic gathering (monolingual alignment)"),
	GATHERER_GRAPH_SIMILARITY_THRESHOLD("graphical-similarity-th", 	null, "FLOAT", 	"Graphical similarity threshold"),
	GATHERER_SEMANTIC_NB_CANDIDATES("nb-semantic-candidates", 	null, 	"INT", 	"Max number of semantic variants for each terms"),
	GATHERER_SEMANTIC_DISTANCE("semantic-distance", 			null, 	"FLOAT", 	"Similarity measure used for semantic alignment. Allowed values: " + Arrays.stream(SimilarityDistance.values()).map(Class::getSimpleName).collect(joining(", "))),


	// POST-PROCESSOR OPTIONS
	POSTPROC_DISABLED("disable-post-processing", null, null, "Disable post-gathering scoring and filtering processings"),
	POSTPROC_INDEPENDANCE_TH("postproc-independance-th", null, "FLOAT", "Term independance score threshold. Terms under threshold are filtered out."),
	POSTPROC_VARIATION_SCORE_TH("postproc-variation-score-th", null, "FLOAT", "Filters out variations with scores under given threshold"),
	
	
	// POST_FILTER
	POST_FILTER_PROPERTY("post-filter-property", null, "STRING", "Enables post-gathering filtering based on given property. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	POST_FILTER_KEEP_VARIANTS("post-filter-keep-variants", null, null, "Keep variants during post-gathering filtering even if they are to be filtered"),
	POST_FILTER_MAX_VARIANTS_NUM("post-filter-max-variants", null, "INT", "The maximum number of variants to keep during post-gathering filtering"),
	POST_FILTER_THRESHOLD("post-filter-th", null, "INT|FLOAT", "Threshold value of post-gathering filter"),
	POST_FILTER_TOP_N("post-filter-top-n", null, "INT", "N value for post-gathering filtering over top N terms"),

	
	// RANKING
	RANKING_DESC_PROPERTY("ranking-desc", null, "STRING", "Sets the output ranking property in DESCENDING order. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	RANKING_ASC_PROPERTY("ranking-asc", null, "STRING", "Sets the output ranking property in ASCENDING order. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	
	// OUTPUT
	TSV(	"tsv", 	null, 		"FILE", 	"Outputs terminology to TSV file"),
	JSON(	"json", 	null, 	"FILE", 	"Outputs terminology to JSON file"),
	TBX(	"tbx", 	null, 		"FILE", 	"Outputs terminology to XMI file"),
	
	
	/*
	 * TSV OPTIONS
	 */
	TSV_PROPERTIES(	"tsv-properties", 	null, 		"STRING", 	"The comma-separated list columns of the tsv file. Allowed values are: " + properties().stream().collect(joining(","))),
	TSV_HIDE_HEADER("tsv-hide-headers", null, 		null, 	"Hide column headers"),
	TSV_HIDE_VARIANTS("tsv-hide-variants",null, 	null, 	"Does no show the variants for each term"),
	
	
	
	/*
	 * ALIGNER
	 */
	SOURCE_TERMINO("source-termino", 	null, "FILE", "The source terminology (indexed corpus)"),
	TARGET_TERMINO("target-termino", 	null, "FILE", "The source terminology (indexed corpus)"),
	TERM(			"term", 			null, "STRING", "The source term (lemma or grouping key) to translate"),
	TERM_LIST(		"term-list", 		null, "FILE", "The path to a list of source terms (lemmas or grouping keys) to translate"),
	DICTIONARY(		"dictionary", 		null, "FILE", "The path to the bilingual dictionary to use for bilingual alignment"),
	N(				"n", 				"n", "INT", "The number of translation candidates to show in the output"),
	MIN_CANDIDATE_FREQUENCY("min-candidate-frequency", null, "INT", "The minimum frequency of target translation candidates"),
	DISTANCE(		"distance", 		null, "FLOAT", "Similarity measure used for context vector alignment. Allowed values: " + Arrays.stream(SimilarityDistance.values()).map(Class::getSimpleName).collect(joining(", "))),
	EXPLAIN(		"explain", 			null, null, "Shows for each aligned term the most influencial co-terms"), 
	ALIGNER_TSV(	"tsv", 				null, "FILE", "A file path to write output of the bilingual aligner")
	
	
	;
	
	private String optName;
	private String optShortName;
	private String argType;
	private String description;
	
	private CliOption(String optName, String optShortName, String argType, String description) {
		this.optName = optName;
		this.optShortName = optShortName;
		this.argType = argType;
		this.description = description;
	}

	public String getOptName() {
		return optName;
	}

	public String getArgType() {
		return argType;
	}
	
	public String getOptShortName() {
		return optShortName;
	}

	public boolean hasArg() {
		return argType != null;
	}

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
