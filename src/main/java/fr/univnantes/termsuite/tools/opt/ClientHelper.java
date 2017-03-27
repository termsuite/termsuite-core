package fr.univnantes.termsuite.tools.opt;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.api.TXTCorpus;
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
		if(client.isSet(TermSuiteCliOption.RESOURCE_DIR))
			resourceConfig.addDirectory(client.asDir(TermSuiteCliOption.RESOURCE_DIR));
		if(client.isSet(TermSuiteCliOption.RESOURCE_JAR))
			resourceConfig.addJar(client.asPath(TermSuiteCliOption.RESOURCE_JAR));
		if(client.isSet(TermSuiteCliOption.RESOURCE_URL_PREFIX))
			try {
				resourceConfig.addResourcePrefix(new URL(client.asString(TermSuiteCliOption.RESOURCE_URL_PREFIX)));
			} catch (MalformedURLException e) {
				LOGGER.error("Invalid url prefix: {}", client.asString(TermSuiteCliOption.RESOURCE_URL_PREFIX));
				throw new TermSuiteException(e);
			}
		return resourceConfig;
	}
	
	public void declareResourceOpts() {
		client.declareFacultative(TermSuiteCliOption.RESOURCE_DIR);
		client.declareFacultative(TermSuiteCliOption.RESOURCE_JAR);
		client.declareFacultative(TermSuiteCliOption.RESOURCE_URL_PREFIX);
	}

	public void declareHistory() {
		client.declareFacultative(TermSuiteCliOption.WATCH);
	}
	
	public Optional<TermHistory> getHistory() {
		if(client.isSet(TermSuiteCliOption.WATCH)) {
			TermHistory value = new TermHistory();
			List<String> asTermString = client.asTermString(TermSuiteCliOption.WATCH);
			value.addWatchedTermString(asTermString);
			return Optional.of(value);
		} else
			return Optional.empty();
	}

	public void declareExtractorOptions() {
		client.declareFacultative(TermSuiteCliOption.PRE_FILTER_PROPERTY);
		client.declareConditional(TermSuiteCliOption.PRE_FILTER_PROPERTY,
				TermSuiteCliOption.PRE_FILTER_MAX_VARIANTS_NUM,
				TermSuiteCliOption.PRE_FILTER_THRESHOLD,
				TermSuiteCliOption.PRE_FILTER_TOP_N);
		client.declareAtMostOneOf(TermSuiteCliOption.PRE_FILTER_TOP_N, TermSuiteCliOption.PRE_FILTER_THRESHOLD);
		
		
		client.declareFacultative(TermSuiteCliOption.CONTEXTUALIZER_ENABLED);
		client.declareConditional(TermSuiteCliOption.CONTEXTUALIZER_ENABLED,
				TermSuiteCliOption.CONTEXTUALIZER_ASSOC_RATE,
				TermSuiteCliOption.CONTEXTUALIZER_MIN_COOC_TH,
				TermSuiteCliOption.CONTEXTUALIZER_SCOPE);
		
		client.declareFacultative(TermSuiteCliOption.MORPHOLOGY_DISABLED);
		client.declareFacultative(TermSuiteCliOption.MORPHOLOGY_DERIVATIVE_DISABLED);
		client.declareFacultative(TermSuiteCliOption.MORPHOLOGY_NATIVE_DISABLED);
		client.declareFacultative(TermSuiteCliOption.MORPHOLOGY_PREFIX_DISABLED);
		

		client.declareFacultative(TermSuiteCliOption.GATHERER_DISABLE_MERGER);
		client.declareFacultative(TermSuiteCliOption.GATHERER_DISABLE_GATHERING);
		client.declareFacultative(TermSuiteCliOption.GATHERER_GRAPH_SIMILARITY_THRESHOLD);
		client.declareFacultative(TermSuiteCliOption.GATHERER_ENABLE_SEMANTIC);
		client.declareConditional(TermSuiteCliOption.GATHERER_ENABLE_SEMANTIC,
				TermSuiteCliOption.GATHERER_SEMANTIC_DISTANCE,
				TermSuiteCliOption.GATHERER_SEMANTIC_NB_CANDIDATES,
				TermSuiteCliOption.GATHERER_SEMANTIC_SIMILAIRTY_TH,
				TermSuiteCliOption.GATHERER_SEMANTIC_DISTANCE);
		
		
		client.declareFacultative(TermSuiteCliOption.POSTPROC_DISABLED);
		client.declareFacultative(TermSuiteCliOption.POSTPROC_INDEPENDANCE_TH);
		client.declareFacultative(TermSuiteCliOption.POSTPROC_VARIATION_SCORE_TH);
		client.declareCannotAppearWhenCondition(TermSuiteCliOption.POSTPROC_DISABLED,
				TermSuiteCliOption.POSTPROC_INDEPENDANCE_TH,
				TermSuiteCliOption.POSTPROC_VARIATION_SCORE_TH);
		
		
		client.declareFacultative(TermSuiteCliOption.POST_FILTER_PROPERTY);
		client.declareConditional(TermSuiteCliOption.POST_FILTER_PROPERTY, 
				TermSuiteCliOption.POST_FILTER_KEEP_VARIANTS,
				TermSuiteCliOption.POST_FILTER_MAX_VARIANTS_NUM,
				TermSuiteCliOption.POST_FILTER_THRESHOLD,
				TermSuiteCliOption.POST_FILTER_TOP_N);
		client.declareAtMostOneOf(TermSuiteCliOption.POST_FILTER_TOP_N, TermSuiteCliOption.POST_FILTER_THRESHOLD);

		client.declareFacultative(TermSuiteCliOption.RANKING_DESC_PROPERTY);
		client.declareFacultative(TermSuiteCliOption.RANKING_ASC_PROPERTY);
		client.declareAtMostOneOf(TermSuiteCliOption.RANKING_DESC_PROPERTY, TermSuiteCliOption.RANKING_ASC_PROPERTY);

	}
	
	public ExtractorOptions getExtractorOptions(Lang lang) {
		ExtractorOptions options = TermSuite.getDefaultExtractorConfig(lang);
		
		configureFilter(options.getPreFilterConfig(),
			TermSuiteCliOption.PRE_FILTER_PROPERTY,
			TermSuiteCliOption.PRE_FILTER_MAX_VARIANTS_NUM,
			TermSuiteCliOption.PRE_FILTER_THRESHOLD,
			TermSuiteCliOption.PRE_FILTER_TOP_N
		);
		options.getPreFilterConfig().setKeepVariants(false);

		configureContextualizer(options);
		configureMorphology(options);
		configureGatherer(options);
		configurePostProc(options);
		configureRanking(options);

		configureFilter(options.getPostFilterConfig(),
			TermSuiteCliOption.POST_FILTER_PROPERTY,
			TermSuiteCliOption.POST_FILTER_MAX_VARIANTS_NUM,
			TermSuiteCliOption.POST_FILTER_THRESHOLD,
			TermSuiteCliOption.POST_FILTER_TOP_N
		);
		if(options.getPostFilterConfig().isEnabled())
			options.getPostFilterConfig().setKeepVariants(client.asBoolean(TermSuiteCliOption.POST_FILTER_KEEP_VARIANTS));


		return options;
	}

	private void configureRanking(ExtractorOptions options) {
		TermRankingOptions config = options.getRankingConfig();
		TermProperty termProperty = null;
		if(client.isSet(TermSuiteCliOption.RANKING_ASC_PROPERTY)) {
			termProperty = client.asTermProperty(TermSuiteCliOption.RANKING_ASC_PROPERTY);
			config.setRankingProperty(termProperty);
			config.setDesc(false);
			ensureNumeric(TermSuiteCliOption.RANKING_ASC_PROPERTY, termProperty);
		} else 	if(client.isSet(TermSuiteCliOption.RANKING_DESC_PROPERTY)) {
			termProperty = client.asTermProperty(TermSuiteCliOption.RANKING_DESC_PROPERTY);
			config.setRankingProperty(termProperty);
			config.setDesc(true);
			ensureNumeric(TermSuiteCliOption.RANKING_DESC_PROPERTY, termProperty);
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
		config.setEnabled(!client.isSet(TermSuiteCliOption.POSTPROC_DISABLED));
		if(config.isEnabled()) {
			if(client.isSet(TermSuiteCliOption.POSTPROC_INDEPENDANCE_TH))
				config.setTermIndependanceTh(client.asDouble(TermSuiteCliOption.POSTPROC_INDEPENDANCE_TH));
			if(client.isSet(TermSuiteCliOption.POSTPROC_VARIATION_SCORE_TH))
				config.setVariationScoreTh(client.asDouble(TermSuiteCliOption.POSTPROC_VARIATION_SCORE_TH));
		}
	}

	private void configureGatherer(ExtractorOptions options) {
		GathererOptions config = options.getGathererConfig();
		config.setMergerEnabled(!client.isSet(TermSuiteCliOption.GATHERER_DISABLE_MERGER));
		config.setSemanticEnabled(client.isSet(TermSuiteCliOption.GATHERER_ENABLE_SEMANTIC));
		
		if(client.isSet(TermSuiteCliOption.GATHERER_DISABLE_GATHERING))
			config.setEnabled(false);
			
		
			
		if(client.isSet(TermSuiteCliOption.GATHERER_GRAPH_SIMILARITY_THRESHOLD))
			config.setGraphicalSimilarityThreshold(client.asDouble(TermSuiteCliOption.GATHERER_GRAPH_SIMILARITY_THRESHOLD));
		if(config.isSemanticEnabled()) {
			if(client.isSet(TermSuiteCliOption.GATHERER_SEMANTIC_DISTANCE)) {
				Class<? extends SimilarityDistance> cls = SimilarityDistance.forName(client.asString(TermSuiteCliOption.GATHERER_SEMANTIC_DISTANCE));
				config.setSemanticSimilarityDistance(cls);
			}
			if(client.isSet(TermSuiteCliOption.GATHERER_SEMANTIC_NB_CANDIDATES))
				config.setSemanticNbCandidates(client.asInt(TermSuiteCliOption.GATHERER_SEMANTIC_NB_CANDIDATES));
			if(client.isSet(TermSuiteCliOption.GATHERER_SEMANTIC_SIMILAIRTY_TH))
				config.setSemanticSimilarityThreshold(client.asDouble(TermSuiteCliOption.GATHERER_SEMANTIC_SIMILAIRTY_TH));
		}
		
	}

	private void configureMorphology(ExtractorOptions options) {
		MorphologicalOptions config = options.getMorphologicalConfig();
		config.setEnabled(!client.isSet(TermSuiteCliOption.MORPHOLOGY_DISABLED));
		if(config.isEnabled()) {
			config.setDerivativesDetecterEnabled(!client.isSet(TermSuiteCliOption.MORPHOLOGY_DERIVATIVE_DISABLED));
			config.setNativeSplittingEnabled(!client.isSet(TermSuiteCliOption.MORPHOLOGY_NATIVE_DISABLED));
			config.setPrefixSplitterEnabled(!client.isSet(TermSuiteCliOption.MORPHOLOGY_PREFIX_DISABLED));
		}
	}

	private void configureContextualizer(ExtractorOptions options) {
		ContextualizerOptions config = options.getContextualizerOptions();
		config.setEnabled(client.isSet(TermSuiteCliOption.CONTEXTUALIZER_ENABLED));
		if(config.isEnabled()) {
			if(client.isSet(TermSuiteCliOption.CONTEXTUALIZER_SCOPE))
				config.setScope(client.asInt(TermSuiteCliOption.CONTEXTUALIZER_SCOPE));
			if(client.isSet(TermSuiteCliOption.CONTEXTUALIZER_MIN_COOC_TH))
				config
					.setMinimumCooccFrequencyThreshold(
						client.asInt(TermSuiteCliOption.CONTEXTUALIZER_MIN_COOC_TH));
			if(client.isSet(TermSuiteCliOption.CONTEXTUALIZER_ASSOC_RATE))
				config.setAssociationRate(AssociationRate.forName(
						client.asString(TermSuiteCliOption.CONTEXTUALIZER_ASSOC_RATE)));
		}
	}

	private void configureFilter(TerminoFilterOptions filterConfig, CliOption filterProperty,
			CliOption maxVariantsNum, CliOption threshold,
			TermSuiteCliOption topN) {
		filterConfig.setEnabled(client.isSet(filterProperty));
		if(filterConfig.isEnabled()) {
			TermProperty property = client.asTermProperty(filterProperty);
			filterConfig.setProperty(property);
			
			if(client.isSet(maxVariantsNum))
				filterConfig.setMaxVariantNum(client.asInt(maxVariantsNum));

			if(client.isSet(threshold))
				filterConfig.keepOverTh(client.asDouble(threshold));
			else if(client.isSet(topN))
				filterConfig.keepTopN(client.asInt(topN));
			else
				CliUtil.throwAtLeast(topN, threshold);
			
		}
	}

	public void declareTsvOptions() {
		client.declareConditional(TermSuiteCliOption.TSV, 
				TermSuiteCliOption.TSV_PROPERTIES,
				TermSuiteCliOption.TSV_HIDE_VARIANTS,
				TermSuiteCliOption.TSV_HIDE_HEADER
				);
	}
	
	public TsvOptions getTsvOptions() {
		TsvOptions options = new TsvOptions();
		
		options.setShowVariants(!client.isSet(TermSuiteCliOption.TSV_HIDE_VARIANTS));

		if(client.isSet(TermSuiteCliOption.TSV_PROPERTIES)) {
			Collection<Property<?>> properties = new ArrayList<>();
			for(String str:client.asList(TermSuiteCliOption.TSV_PROPERTIES)) 
				properties.add(client.asProperty(str));
			options.properties(properties);
		}
		
		options.showHeaders(!client.isSet(TermSuiteCliOption.TSV_HIDE_HEADER));
		
		return options;
	}
	
	
	public TXTCorpus getTxtCorpus() {
		Path ascorpusPath = client.asDir(TermSuiteCliOption.FROM_TXT_CORPUS_PATH);
		return new TXTCorpus(client.getLang(), ascorpusPath);
	}

}
