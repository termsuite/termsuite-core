package fr.univnantes.termsuite.tools.opt;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.univnantes.termsuite.engines.contextualizer.AssociationRate;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.model.TermProperty;

@SuppressWarnings("unchecked")
public enum TermSuiteCliOption implements CliOption {
	/*
	 * GENERAL PURPOSE OPTIONS
	 */
	HELP("help", "h", OptType.T_NONE, "Print a help message.", Collections.EMPTY_LIST),
	LOG_INFO("info", null, OptType.T_NONE, "Activate logging (by default to console)", Collections.EMPTY_LIST),
	LOG_TO_FILE("log-file", null, OptType.T_FILE, "Write log messages to given file", Collections.EMPTY_LIST),
	LOG_DEBUG("debug", null, OptType.T_NONE, "Activate verbose logging for debugging purpose", Collections.EMPTY_LIST),

	
	/*
	 * PREPROCESSOR OPTIONS
	 */
	LANGUAGE(		"language", 	"l", 		OptType.T_LANG, "Language of the input corpus", Collections.EMPTY_LIST),
	ENCODING(		"encoding", 	"e", 		OptType.T_ENC, 	"Encoding of the input corpus", Collections.EMPTY_LIST),
	TAGGER(			"tagger", 		null, 		OptType.T_STRING, "Which POS tagger to use.", Tagger.stream().map(Tagger::getShortName).collect(toList())),
	TAGGER_PATH(	"tagger-home",	"t", 		OptType.T_FILE, 	"Path to POS tagger's home", Collections.EMPTY_LIST),
	FROM_TXT_CORPUS_PATH("from-text-corpus", "c", OptType.T_DIR, "Directory to corpus (containing a list of .txt documents)", Collections.EMPTY_LIST),
	WATCH(			"watch", 		null, 		OptType.T_TERM_LIST, 	"List of terms (grouping keys or lemmas) to log to output", Collections.EMPTY_LIST),
	RESOURCE_DIR(	"resource-dir", null, 		OptType.T_DIR, 	"Custom resource directory", Collections.EMPTY_LIST),
	RESOURCE_JAR(	"resource-jar", null, 		OptType.T_FILE, 	"Custom resource jar", Collections.EMPTY_LIST),
	RESOURCE_URL_PREFIX("resource-url-prefix", null, OptType.T_STRING, "Custom resource url prefix", Collections.EMPTY_LIST),
	PREPARED_TERMINO_JSON("json", 	null, 		OptType.T_FILE, 	"Path to JSON indexed corpus file where all occurrences are imported to", Collections.EMPTY_LIST),
	CAS_TSV(		"tsv-anno", 	null, 		OptType.T_DIR, 	"Path to TSV export directory of all spotted term annotations", Collections.EMPTY_LIST),
	CAS_JSON(		"json-anno", 	null, 		OptType.T_DIR, 	"Path to JSON export directory of all spotted term annotations", Collections.EMPTY_LIST),
	CAS_XMI(		"xmi-anno", 	null, 		OptType.T_DIR, 	"Path to XMI export directory of all spotted term annotations", Collections.EMPTY_LIST),
	
	
	/*
	 * EXTRACTOR OPTIONS
	 */
	
	// CORPUS_LOADING_OPTIONS
	FROM_PREPARED_CORPUS_PATH("from-prepared-corpus", null, OptType.T_DIR, "A file or directory path. Starts the terminology extraction pipeline from an XMI corpus or an imported terminology json file instead of a txt corpus.", Collections.EMPTY_LIST),
	
	
	// PRE_FILTER
	PRE_FILTER_PROPERTY("pre-filter-property", null, OptType.T_STRING, "Enables pre-gathering filtering based on given property.", TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(toList())),
	PRE_FILTER_MAX_VARIANTS_NUM("pre-filter-max-variants", null, OptType.T_INT, "The maximum number of variants to keep during pre-gathering filtering", Collections.EMPTY_LIST),
	PRE_FILTER_THRESHOLD("pre-filter-th", null, OptType.T_INT_OR_FLOAT, "Threshold value of pre-gathering filter", Collections.EMPTY_LIST),
	PRE_FILTER_TOP_N("pre-filter-top-n", null, OptType.T_INT, "N value for pre-gathering filtering over top N terms", Collections.EMPTY_LIST),
	
	
	// CONTEXTUALIZER
	CONTEXTUALIZER_ENABLED(	"contextualize", 		null, OptType.T_NONE, 	"Activates the contextualizer", Collections.EMPTY_LIST),
	CONTEXTUALIZER_SCOPE(	"context-scope", 		null, OptType.T_INT, 	"Radius of single-word term window used during contextualization", Collections.EMPTY_LIST),
	CONTEXTUALIZER_MIN_COOC_TH("context-coocc-th", 	null, OptType.T_INT_OR_FLOAT, 	"Sets a minimum frequency threshold for co-terms to appear in context vectors", Collections.EMPTY_LIST),
	CONTEXTUALIZER_ASSOC_RATE("context-assoc-rate", 	null, OptType.T_INT_OR_FLOAT, 	"Association rate measure used to normalize context vectors.", Arrays.stream(AssociationRate.values()).map(Class::getSimpleName).collect(toList())),

	
	// MORPHOLOGY ANALYZER
	MORPHOLOGY_DISABLED("disable-morphology", 				null, OptType.T_NONE, "Disable morphology analysis (native, prefix, derivation splitting)", Collections.EMPTY_LIST),
	MORPHOLOGY_PREFIX_DISABLED("disable-prefix-splitting", 	null, OptType.T_NONE, "Disable morphological prefix splitting", Collections.EMPTY_LIST),
	MORPHOLOGY_DERIVATIVE_DISABLED("disable-derivative-splitting", null, OptType.T_NONE, "Disable morphological derivative splitting", Collections.EMPTY_LIST),
	MORPHOLOGY_NATIVE_DISABLED("disable-native-splitting", 	null, OptType.T_NONE, "Disable morphological native splitting", Collections.EMPTY_LIST),

	
	// GATHERER 
	GATHERER_DISABLE_GATHERING("disable-gathering", 		null, 	OptType.T_NONE, 	"Disable variant term gathering", Collections.EMPTY_LIST),
	GATHERER_ENABLE_SEMANTIC("enable-semantic-gathering", 		null, 	OptType.T_NONE, 	"Enable semantic term gathering (monolingual alignment)", Collections.EMPTY_LIST),
	GATHERER_DISABLE_MERGER("disable-merging", 					null, 	OptType.T_NONE, 	"Disable graphical term merging", Collections.EMPTY_LIST),
	GATHERER_SEMANTIC_SIMILAIRTY_TH("semantic-similarity-th", 	null, 	OptType.T_INT_OR_FLOAT, 	"Minimum semantic similarity threshold for semantic gathering (monolingual alignment)", Collections.EMPTY_LIST),
	GATHERER_GRAPH_SIMILARITY_THRESHOLD("graphical-similarity-th", 	null, OptType.T_INT_OR_FLOAT, 	"Graphical similarity threshold", Collections.EMPTY_LIST),
	GATHERER_SEMANTIC_NB_CANDIDATES("nb-semantic-candidates", 	null, 	OptType.T_INT, 	"Max number of semantic variants for each terms", Collections.EMPTY_LIST),
	GATHERER_SEMANTIC_DISTANCE("semantic-distance", 			null, 	OptType.T_INT_OR_FLOAT, 	"Similarity measure used for semantic alignment.", Arrays.stream(SimilarityDistance.values()).map(Class::getSimpleName).collect(toList())),


	// POST-PROCESSOR OPTIONS
	POSTPROC_DISABLED("disable-post-processing", 				null, OptType.T_NONE, "Disable post-gathering scoring and filtering processings", Collections.EMPTY_LIST),
	POSTPROC_INDEPENDANCE_TH("postproc-independance-th", 		null, OptType.T_INT_OR_FLOAT, "Term independance score threshold. Terms under threshold are filtered out.", Collections.EMPTY_LIST),
	POSTPROC_VARIATION_SCORE_TH("postproc-variation-score-th", 	null, OptType.T_INT_OR_FLOAT, "Filters out variations with scores under given threshold", Collections.EMPTY_LIST),
	
	
	// POST_FILTER
	POST_FILTER_PROPERTY("post-filter-property", 			null, OptType.T_STRING, "Enables post-gathering filtering based on given property. ", TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(toList())),
	POST_FILTER_KEEP_VARIANTS("post-filter-keep-variants", 	null, OptType.T_NONE, "Keep variants during post-gathering filtering even if they are to be filtered", Collections.EMPTY_LIST),
	POST_FILTER_MAX_VARIANTS_NUM("post-filter-max-variants", null, OptType.T_INT, "The maximum number of variants to keep during post-gathering filtering", Collections.EMPTY_LIST),
	POST_FILTER_THRESHOLD("post-filter-th", 				null, OptType.T_INT_OR_FLOAT, "Threshold value of post-gathering filter", Collections.EMPTY_LIST),
	POST_FILTER_TOP_N("post-filter-top-n", 					null, OptType.T_INT, "N value for post-gathering filtering over top N terms", Collections.EMPTY_LIST),

	
	// RANKING
	RANKING_DESC_PROPERTY("ranking-desc", null, 	OptType.T_STRING, "Sets the output ranking property in DESCENDING order. ", TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(toList())),
	RANKING_ASC_PROPERTY("ranking-asc", 	null, 	OptType.T_STRING, "Sets the output ranking property in ASCENDING order. ", TermProperty.numberValues().sorted().map(TermProperty::getShortName).collect(toList())),
	
	// OUTPUT
	TSV(	"tsv", 		null, 	OptType.T_FILE, 	"Outputs terminology to TSV file", Collections.EMPTY_LIST),
	JSON(	"json", 	null, 	OptType.T_FILE, 	"Outputs terminology to JSON file", Collections.EMPTY_LIST),
	TBX(	"tbx", 		null, 	OptType.T_FILE, 	"Outputs terminology to TBX file", Collections.EMPTY_LIST),
	
	
	// OTHER OPTIONS
	NO_OCCURRENCE("no-occurrence", 	null, 	OptType.T_NONE, "Do not store occurrence offsets in memory while spotting. Allows to process bigger volumes of input text.", Collections.EMPTY_LIST),
	CAPPED_SIZE("capped-size", 	null, 	OptType.T_INT, "The maximum number of terms to keep in memory while spotting. Allows to process bigger volumes of input text.", Collections.EMPTY_LIST),
	
	
	
	/*
	 * TSV OPTIONS
	 */
	TSV_PROPERTIES(	"tsv-properties", 		null, 	OptType.T_STRING, 	"The comma-separated list columns of the tsv file.", properties().stream().collect(toList())),
	TSV_HIDE_HEADER("tsv-hide-headers", 	null, 	OptType.T_NONE, 	"Hide column headers", Collections.EMPTY_LIST),
	TSV_HIDE_VARIANTS("tsv-hide-variants",	null, 	OptType.T_NONE, 	"Does no show the variants for each term", Collections.EMPTY_LIST),
	
	/*
	 * ALIGNER
	 */
	SOURCE_TERMINO("source-termino", 	null, OptType.T_FILE, "The source terminology (indexed corpus)", Collections.EMPTY_LIST),
	TARGET_TERMINO("target-termino", 	null, OptType.T_FILE, "The source terminology (indexed corpus)", Collections.EMPTY_LIST),
	TERM(			"term", 			null, OptType.T_TERM_LIST, "The source term (lemma or grouping key) to translate", Collections.EMPTY_LIST),
	TERM_LIST(		"term-list", 		null, OptType.T_FILE, "The path to a list of source terms (lemmas or grouping keys) to translate", Collections.EMPTY_LIST),
	DICTIONARY(		"dictionary", 		null, OptType.T_FILE, "The path to the bilingual dictionary to use for bilingual alignment", Collections.EMPTY_LIST),
	N(				"n", 				"n", OptType.T_INT, "The number of translation candidates to show in the output", Collections.EMPTY_LIST),
	MIN_CANDIDATE_FREQUENCY("min-candidate-frequency", null, OptType.T_INT, "The minimum frequency of target translation candidates", Collections.EMPTY_LIST),
	DISTANCE(		"distance", 		null, OptType.T_INT_OR_FLOAT, "Similarity measure used for context vector alignment.", Arrays.stream(SimilarityDistance.values()).map(Class::getSimpleName).collect(toList())),
	EXPLAIN(		"explain", 			null, OptType.T_NONE, "Shows for each aligned term the most influencial co-terms", Collections.EMPTY_LIST), 
	ALIGNER_TSV(	"tsv", 				null, OptType.T_FILE, "A file path to write output of the bilingual aligner", Collections.EMPTY_LIST)
	
	
	;
	
	private List<Object> allowedValues;
	private String optName;
	private String optShortName;
	private OptType argType;
	private String description;
	
	private TermSuiteCliOption(String optName, String optShortName, OptType argType, String description, List<Object> allowedValues) {
		this.optName = optName;
		this.optShortName = optShortName;
		this.argType = argType;
		this.description = description;
		this.allowedValues = allowedValues;
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
	
	@Override
	public List<Object> getAllowedValues() {
		return allowedValues;
	}
}
