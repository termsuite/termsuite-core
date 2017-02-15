package fr.univnantes.termsuite.tools.opt;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.contextualizer.AssociationRate;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.engines.gatherer.GathererOptions;
import fr.univnantes.termsuite.engines.postproc.TermRankingOptions;
import fr.univnantes.termsuite.engines.splitter.MorphologicalOptions;
import fr.univnantes.termsuite.io.tsv.TsvOptions;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TextCorpus;
import fr.univnantes.termsuite.resources.PostProcessorOptions;
import fr.univnantes.termsuite.tools.CliUtil;
import fr.univnantes.termsuite.tools.CommandLineClient;
import fr.univnantes.termsuite.utils.TermHistory;

public class ClientHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHelper.class);
	private CommandLineClient client;
	
	public ClientHelper(CommandLineClient client) {
		super();
		this.client = client;
	}

	public ResourceConfig getResourceConfig() {
		ResourceConfig resourceConfig = new ResourceConfig();
		if(client.isSet(CliOption.RESOURCE_DIR))
			resourceConfig.addDirectory(client.asDir(CliOption.RESOURCE_DIR));
		if(client.isSet(CliOption.RESOURCE_JAR))
			resourceConfig.addJar(client.asPath(CliOption.RESOURCE_JAR));
		if(client.isSet(CliOption.RESOURCE_URL_PREFIX))
			try {
				resourceConfig.addResourcePrefix(new URL(client.asString(CliOption.RESOURCE_URL_PREFIX)));
			} catch (MalformedURLException e) {
				LOGGER.error("Invalid url prefix: {}", client.asString(CliOption.RESOURCE_URL_PREFIX));
				throw new TermSuiteException(e);
			}
		return resourceConfig;
	}
	
	public void declareResourceOpts() {
		client.declareFacultative(CliOption.RESOURCE_DIR);
		client.declareFacultative(CliOption.RESOURCE_JAR);
		client.declareFacultative(CliOption.RESOURCE_URL_PREFIX);
	}

	public void declareHistory() {
		client.declareFacultative(CliOption.WATCH);
	}
	
	public Optional<TermHistory> getHistory() {
		if(client.isSet(CliOption.WATCH)) {
			TermHistory value = new TermHistory();
			value.addWatchedLemmas(client.asArray(CliOption.WATCH));
			return Optional.of(value);
		} else
			return Optional.empty();
	}

	public void declareExtractorOptions() {
		client.declareFacultative(CliOption.PRE_FILTER_PROPERTY);
		client.declareConditional(CliOption.PRE_FILTER_PROPERTY,
				CliOption.PRE_FILTER_KEEP_VARIANTS,
				CliOption.PRE_FILTER_MAX_VARIANTS_NUM,
				CliOption.PRE_FILTER_THRESHOLD,
				CliOption.PRE_FILTER_TOP_N);
		client.declareAtMostOneOf(CliOption.PRE_FILTER_TOP_N, CliOption.PRE_FILTER_THRESHOLD);
		
		
		client.declareFacultative(CliOption.CONTEXTUALIZER_ENABLED);
		client.declareConditional(CliOption.CONTEXTUALIZER_ENABLED,
				CliOption.CONTEXTUALIZER_ASSOC_RATE,
				CliOption.CONTEXTUALIZER_MIN_COOC_TH,
				CliOption.CONTEXTUALIZER_SCOPE);
		
		client.declareFacultative(CliOption.MORPHOLOGY_DISABLED);
		client.declareFacultative(CliOption.MORPHOLOGY_DERIVATIVE_DISABLED);
		client.declareFacultative(CliOption.MORPHOLOGY_NATIVE_DISABLED);
		client.declareFacultative(CliOption.MORPHOLOGY_PREFIX_DISABLED);
		

		client.declareFacultative(CliOption.GATHERER_DISABLE_MERGER);
		client.declareFacultative(CliOption.GATHERER_GRAPH_SIMILARITY_THRESHOLD);
		client.declareFacultative(CliOption.GATHERER_ENABLE_SEMANTIC);
		client.declareConditional(CliOption.GATHERER_ENABLE_SEMANTIC,
				CliOption.GATHERER_SEMANTIC_DISTANCE,
				CliOption.GATHERER_SEMANTIC_NB_CANDIDATES,
				CliOption.GATHERER_SEMANTIC_SIMILAIRTY_TH,
				CliOption.GATHERER_SEMANTIC_DISTANCE);
		
		
		client.declareFacultative(CliOption.POSTPROC_DISABLED);
		client.declareFacultative(CliOption.POSTPROC_INDEPENDANCE_TH);
		client.declareFacultative(CliOption.POSTPROC_VARIATION_SCORE_TH);
		client.declareCannotAppearWhenCondition(CliOption.POSTPROC_DISABLED,
				CliOption.POSTPROC_INDEPENDANCE_TH,
				CliOption.POSTPROC_VARIATION_SCORE_TH);
		
		
		client.declareFacultative(CliOption.POST_FILTER_PROPERTY);
		client.declareConditional(CliOption.POST_FILTER_PROPERTY, 
				CliOption.POST_FILTER_KEEP_VARIANTS,
				CliOption.POST_FILTER_MAX_VARIANTS_NUM,
				CliOption.POST_FILTER_THRESHOLD,
				CliOption.POST_FILTER_TOP_N);
		client.declareAtMostOneOf(CliOption.POST_FILTER_TOP_N, CliOption.POST_FILTER_THRESHOLD);

		client.declareFacultative(CliOption.RANKING_DESC_PROPERTY);
		client.declareFacultative(CliOption.RANKING_ASC_PROPERTY);
		client.declareAtMostOneOf(CliOption.RANKING_DESC_PROPERTY, CliOption.RANKING_ASC_PROPERTY);

	}
	
	public ExtractorOptions getExtractorOptions(Lang lang) {
		ExtractorOptions options = TermSuite.getDefaultExtractorConfig(lang);
		
		configureFilter(options.getPreFilterConfig(),
			CliOption.PRE_FILTER_PROPERTY,
			CliOption.PRE_FILTER_KEEP_VARIANTS,
			CliOption.PRE_FILTER_MAX_VARIANTS_NUM,
			CliOption.PRE_FILTER_THRESHOLD,
			CliOption.PRE_FILTER_TOP_N
		);

		configureContextualizer(options);
		configureMorphology(options);
		configureGatherer(options);
		configurePostProc(options);
		configureRanking(options);

		configureFilter(options.getPostFilterConfig(),
			CliOption.POST_FILTER_PROPERTY,
			CliOption.POST_FILTER_KEEP_VARIANTS,
			CliOption.POST_FILTER_MAX_VARIANTS_NUM,
			CliOption.POST_FILTER_THRESHOLD,
			CliOption.POST_FILTER_TOP_N
		);

		return options;
	}

	private void configureRanking(ExtractorOptions options) {
		TermRankingOptions config = options.getRankingConfig();
		TermProperty termProperty = null;
		if(client.isSet(CliOption.RANKING_ASC_PROPERTY)) {
			termProperty = client.asTermProperty(CliOption.RANKING_ASC_PROPERTY);
			config.setRankingProperty(termProperty);
			config.setDesc(false);
			ensureNumeric(CliOption.RANKING_ASC_PROPERTY, termProperty);
		} else 	if(client.isSet(CliOption.RANKING_DESC_PROPERTY)) {
			termProperty = client.asTermProperty(CliOption.RANKING_DESC_PROPERTY);
			config.setRankingProperty(termProperty);
			config.setDesc(true);
			ensureNumeric(CliOption.RANKING_DESC_PROPERTY, termProperty);
		}
	}

	private void ensureNumeric(CliOption opt, Property<?> property) {
		if(property != null && !property.isNumeric()) {
			CliUtil.throwException("\"%s\" is an invalid property for option --%s. Expected a number property.", 
					property.getShortName(),
					opt.getOptName()
				);
		}
	}
	
	private void configurePostProc(ExtractorOptions options) {
		PostProcessorOptions config = options.getPostProcessorConfig();
		config.setEnabled(!client.isSet(CliOption.POSTPROC_DISABLED));
		if(config.isEnabled()) {
			if(client.isSet(CliOption.POSTPROC_INDEPENDANCE_TH))
				config.setTermIndependanceTh(client.asDouble(CliOption.POSTPROC_INDEPENDANCE_TH));
			if(client.isSet(CliOption.POSTPROC_VARIATION_SCORE_TH))
				config.setVariationScoreTh(client.asDouble(CliOption.POSTPROC_VARIATION_SCORE_TH));
		}
	}

	private void configureGatherer(ExtractorOptions options) {
		GathererOptions config = options.getGathererConfig();
		config.setMergerEnabled(!client.isSet(CliOption.GATHERER_DISABLE_MERGER));
		config.setSemanticEnabled(client.isSet(CliOption.GATHERER_ENABLE_SEMANTIC));
		if(client.isSet(CliOption.GATHERER_GRAPH_SIMILARITY_THRESHOLD))
			config.setGraphicalSimilarityThreshold(client.asDouble(CliOption.GATHERER_GRAPH_SIMILARITY_THRESHOLD));
		if(config.isSemanticEnabled()) {
			if(client.isSet(CliOption.GATHERER_SEMANTIC_DISTANCE)) {
				Class<? extends SimilarityDistance> cls = SimilarityDistance.forName(client.asString(CliOption.GATHERER_SEMANTIC_DISTANCE));
				config.setSemanticSimilarityDistance(cls);
			}
			if(client.isSet(CliOption.GATHERER_SEMANTIC_NB_CANDIDATES))
				config.setSemanticNbCandidates(client.asInt(CliOption.GATHERER_SEMANTIC_NB_CANDIDATES));
			if(client.isSet(CliOption.GATHERER_SEMANTIC_SIMILAIRTY_TH))
				config.setSemanticSimilarityThreshold(client.asDouble(CliOption.GATHERER_SEMANTIC_SIMILAIRTY_TH));
		}
		
	}

	private void configureMorphology(ExtractorOptions options) {
		MorphologicalOptions config = options.getMorphologicalConfig();
		config.setEnabled(!client.isSet(CliOption.MORPHOLOGY_DISABLED));
		if(config.isEnabled()) {
			config.setDerivativesDetecterEnabled(!client.isSet(CliOption.MORPHOLOGY_DERIVATIVE_DISABLED));
			config.setNativeSplittingEnabled(!client.isSet(CliOption.MORPHOLOGY_NATIVE_DISABLED));
			config.setPrefixSplitterEnabled(!client.isSet(CliOption.MORPHOLOGY_PREFIX_DISABLED));
		}
	}

	private void configureContextualizer(ExtractorOptions options) {
		ContextualizerOptions config = options.getContextualizerOptions();
		config.setEnabled(client.isSet(CliOption.CONTEXTUALIZER_ENABLED));
		if(config.isEnabled()) {
			if(client.isSet(CliOption.CONTEXTUALIZER_SCOPE))
				config.setScope(client.asInt(CliOption.CONTEXTUALIZER_SCOPE));
			if(client.isSet(CliOption.CONTEXTUALIZER_MIN_COOC_TH))
				config
					.setMinimumCooccFrequencyThreshold(
						client.asInt(CliOption.CONTEXTUALIZER_MIN_COOC_TH));
			if(client.isSet(CliOption.CONTEXTUALIZER_ASSOC_RATE))
				config.setAssociationRate(AssociationRate.forName(
						client.asString(CliOption.CONTEXTUALIZER_ASSOC_RATE)));
		}
	}

	private void configureFilter(TerminoFilterOptions filterConfig, CliOption filterProperty,
			CliOption keepVariants, CliOption maxVariantsNum, CliOption threshold,
			CliOption topN) {
		filterConfig.setEnabled(client.isSet(filterProperty));
		if(filterConfig.isEnabled()) {
			TermProperty property = client.asTermProperty(filterProperty);
			filterConfig.setProperty(property);
			filterConfig.setKeepVariants(client.asBoolean(keepVariants));
			
			if(client.isSet(maxVariantsNum))
				filterConfig.setMaxVariantNum(client.asInt(maxVariantsNum));

			if(client.isSet(threshold))
				filterConfig.keepOverTh(client.asDouble(threshold));
			else if(client.isSet(topN))
				filterConfig.keepTopN(client.asInt(topN));
			else
				CliUtil.throwAtLeast(topN, threshold);
			
			filterConfig.setKeepVariants(client.asBoolean(keepVariants));
		}
	}

	public void declareTsvOptions() {
		client.declareConditional(CliOption.TSV, 
				CliOption.TSV_PROPERTIES,
				CliOption.TSV_HIDE_VARIANTS,
				CliOption.TSV_HIDE_HEADER
				);
	}
	
	public TsvOptions getTsvOptions() {
		TsvOptions options = new TsvOptions();
		
		options.setShowVariants(!client.isSet(CliOption.TSV_HIDE_VARIANTS));

		if(client.isSet(CliOption.TSV_PROPERTIES)) {
			Collection<Property<?>> properties = new ArrayList<>();
			for(String str:client.asList(CliOption.TSV_PROPERTIES)) 
				properties.add(client.asProperty(str));
			options.properties(properties);
		}
		
		options.showHeaders(!client.asBoolean(CliOption.TSV_HIDE_HEADER));
		
		return options;
	}
	
	
	public TextCorpus getTxtCorpus() {
		Path ascorpusPath = client.asDir(CliOption.FROM_TXT_CORPUS_PATH);
		return new TextCorpus(client.getLang(), ascorpusPath);
	}

}
