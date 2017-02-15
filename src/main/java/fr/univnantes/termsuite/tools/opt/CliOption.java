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
	LANGUAGE(		"language", 	"l", 		true, 	"Language of the input corpus"),
	ENCODING(		"encoding", 	"e", 		true, 	"Encoding of the input corpus"),
	TAGGER(			"tagger", 		null, 		true, 	"Which POS tagger to use. Allowed are: " + Tagger.stream().map(Tagger::getShortName).collect(joining(", "))),
	TAGGER_PATH(	"tagger-home",	"t", 		true, 	"Path to POS tagger's home"),
	FROM_TXT_CORPUS_PATH("from-text-corpus", "c", true, "Directory to corpus (containing a list of .txt documents"),
	WATCH(			"watch", 		null, 		true, 	"List of terms (grouping keys or lemmas) to log to output"),
	RESOURCE_DIR(	"resource-dir", null, 		true, 	"Custom resource directory"),
	RESOURCE_JAR(	"resource-jar", null, 		true, 	"Custom resource jar"),
	RESOURCE_URL_PREFIX("resource-url-prefix", null, true, "Custom resource url prefix"),
	PREPARED_TERMINO_JSON("json", 	null, 		true, 	"Path to JSON indexed corpus file where all occurrences are imported to"),
	CAS_TSV(		"tsv-anno", 	null, 		true, 	"Path to TSV export directory of all spotted term annotations"),
	CAS_JSON(		"json-anno", 	null, 		true, 	"Path to JSON export directory of all spotted term annotations"),
	CAS_XMI(		"xmi-anno", 	null, 		true, 	"Path to XMI export directory of all spotted term annotations"),
	
	
	/*
	 * EXTRACTOR OPTIONS
	 */
	
	// CORPUS_LOADING_OPTIONS
	FROM_PREPARED_CORPUS_PATH("from-prepared-corpus", null, true, "A file or directory path. Starts the terminology extraction pipeline from an XMI corpus or an imported terminology json file instead of a txt corpus."),
	
	
	// PRE_FILTER
	PRE_FILTER_PROPERTY("pre-filter-property", null, true, "Enables pre-gathering filtering based on given property. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	PRE_FILTER_KEEP_VARIANTS("pre-filter-keep-variants", null, false, "Keep variants during pre-gathering filtering even if they are to be filtered"),
	PRE_FILTER_MAX_VARIANTS_NUM("pre-filter-max-variants", null, true, "The maximum number of variants to keep during pre-gathering filtering"),
	PRE_FILTER_THRESHOLD("pre-filter-th", null, true, "Threshold value of pre-gathering filter"),
	PRE_FILTER_TOP_N("pre-filter-top-n", null, true, "N value for pre-gathering filtering over top N terms"),
	
	
	// CONTEXTUALIZER
	CONTEXTUALIZER_ENABLED(	"contextualize", 		null, false, 	"Activates the contextualizer"),
	CONTEXTUALIZER_SCOPE(	"context-scope", 		null, true, 	"Radius of single-word term window used during contextualization"),
	CONTEXTUALIZER_MIN_COOC_TH("context-coocc-th", 	null, true, 	"Sets a minimum frequency threshold for co-terms to appear in context vectors"),
	CONTEXTUALIZER_ASSOC_RATE("context-assoc-rate", 	null, true, 	"Association rate measure used to normalize context vectors. Allowed values are: " + Arrays.stream(AssociationRate.values()).map(Class::getSimpleName).collect(joining(","))),

	
	// MORPHOLOGY ANALYZER
	MORPHOLOGY_DISABLED("disable-morphology", null, false, "Disable morphology analysis (native, prefix, derivation splitting)"),
	MORPHOLOGY_PREFIX_DISABLED("disable-prefix-splitting", null, false, "Disable morphological prefix splitting"),
	MORPHOLOGY_DERIVATIVE_DISABLED("disable-derivative-splitting", null, false, "Disable morphological derivative splitting"),
	MORPHOLOGY_NATIVE_DISABLED("disable-native-splitting", null, false, "Disable morphological native splitting"),

	
	// GATHERER 
	GATHERER_ENABLE_SEMANTIC("enable-semantic-gathering", 		null, 	false, 	"Enable semantic term gathering (monolingual alignment)"),
	GATHERER_DISABLE_MERGER("disable-merging", 					null, 	false, 	"Disable graphical term merging"),
	GATHERER_SEMANTIC_SIMILAIRTY_TH("semantic-similarity-th", 	null, 	true, 	"Minimum semantic similarity threshold for semantic gathering (monolingual alignment)"),
	GATHERER_GRAPH_SIMILARITY_THRESHOLD("graphical-similarity-th", 	null, 	true, 	"Graphical similarity threshold"),
	GATHERER_SEMANTIC_NB_CANDIDATES("nb-semantic-candidates", 	null, 	true, 	"Max number of semantic variants for each terms"),
	GATHERER_SEMANTIC_DISTANCE("semantic-distance", 			null, 	true, 	"Similarity measure used for semantic alignment. Allowed values: " + Arrays.stream(SimilarityDistance.values()).map(Class::getSimpleName).collect(joining(", "))),


	// POST-PROCESSOR OPTIONS
	POSTPROC_DISABLED("disable-post-processing", null, false, "Disable post-gathering scoring and filtering processings"),
	POSTPROC_INDEPENDANCE_TH("postproc-independance-th", null, true, "Term independance score threshold. Terms under threshold are filtered out."),
	POSTPROC_VARIATION_SCORE_TH("postproc-variation-score-th", null, true, "Filters out variations with scores under given threshold"),
	
	
	// POST_FILTER
	POST_FILTER_PROPERTY("post-filter-property", null, true, "Enables post-gathering filtering based on given property. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	POST_FILTER_KEEP_VARIANTS("post-filter-keep-variants", null, false, "Keep variants during post-gathering filtering even if they are to be filtered"),
	POST_FILTER_MAX_VARIANTS_NUM("post-filter-max-variants", null, true, "The maximum number of variants to keep during post-gathering filtering"),
	POST_FILTER_THRESHOLD("post-filter-th", null, true, "Threshold value of post-gathering filter"),
	POST_FILTER_TOP_N("post-filter-top-n", null, true, "N value for post-gathering filtering over top N terms"),

	
	// RANKING
	RANKING_DESC_PROPERTY("ranking-desc", null, true, "Sets the output ranking property in DESCENDING order. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	RANKING_ASC_PROPERTY("ranking-asc", null, true, "Sets the output ranking property in ASCENDING order. Allowed values are: " + TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(joining(", "))),
	
	// OUTPUT
	TSV(	"tsv", 	null, 		true, 	"Outputs terminology to TSV file"),
	JSON(	"json", 	null, 	true, 	"Outputs terminology to JSON file"),
	TBX(	"tbx", 	null, 		true, 	"Outputs terminology to XMI file"),
	
	
	/*
	 * TSV OPTIONS
	 */
	TSV_PROPERTIES(	"tsv-properties", 	null, 		true, 	"The comma-separated list columns of the tsv file. Allowed values are: " + properties().stream().collect(joining(","))),
	TSV_HIDE_HEADER("tsv-hide-headers", null, 		false, 	"Hide column headers"),
	TSV_HIDE_VARIANTS("tsv-hide-variants",null, 	false, 	"Does no show the variants for each term"),
	;
	
	private String optName;
	private String optShortName;
	private boolean hasArg;
	private String description;
	
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
	
	private static final List<String> properties() {
		List<String> properties = new ArrayList<>();
		
		for(TermProperty p:TermProperty.values())
			properties.add(p.getShortName());
		for(RelationProperty p:RelationProperty.values())
			properties.add(p.getShortName());
		
		return properties;
	}
}
