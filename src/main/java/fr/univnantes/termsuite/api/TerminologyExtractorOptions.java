package fr.univnantes.termsuite.api;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.engines.gatherer.GathererOptions;
import fr.univnantes.termsuite.engines.postproc.TermRankingOptions;
import fr.univnantes.termsuite.engines.splitter.MorphologicalOptions;
import fr.univnantes.termsuite.framework.ConfigurationObject;
import fr.univnantes.termsuite.resources.PostProcessorOptions;

@ConfigurationObject
public class TerminologyExtractorOptions {

	private boolean contextualizerEnabled = false;
	private ContextualizerOptions contextualizerOptions = new ContextualizerOptions();

	private boolean preFilterEnabled = false;
	private TerminoFilterOptions preFilterConfig = new TerminoFilterOptions();
	
	private boolean postFilterEnabled = false;
	private TerminoFilterOptions postFilterConfig = new TerminoFilterOptions();

	private boolean postProcessorEnabled = true;
	private PostProcessorOptions postProcessorConfig = new PostProcessorOptions();
	
	private boolean gathererEnabled = true;
	private GathererOptions gathererConfig = new GathererOptions();

	private boolean morphologicalAnalysisEnabled = true;
	private MorphologicalOptions morphologicalConfig = new MorphologicalOptions();
	
	private TermRankingOptions rankingConfig = new TermRankingOptions();

	public boolean isPreFilterEnabled() {
		return preFilterEnabled;
	}

	public TerminologyExtractorOptions setPreFilterEnabled(boolean preFilterEnabled) {
		this.preFilterEnabled = preFilterEnabled;
		return this;
	}

	public TerminoFilterOptions getPreFilterConfig() {
		return preFilterConfig;
	}

	public TerminologyExtractorOptions setPreFilterConfig(TerminoFilterOptions preFilterConfig) {
		this.preFilterConfig = preFilterConfig;
		return this;
	}

	public boolean isPostFilterEnabled() {
		return postFilterEnabled;
	}

	public TerminologyExtractorOptions setPostFilterEnabled(boolean postFilterEnabled) {
		this.postFilterEnabled = postFilterEnabled;
		return this;
	}

	public TerminoFilterOptions getPostFilterConfig() {
		return postFilterConfig;
	}

	public TerminologyExtractorOptions setPostFilterConfig(TerminoFilterOptions postFilterConfig) {
		this.postFilterConfig = postFilterConfig;
		return this;
	}

	public boolean isPostProcessorEnabled() {
		return postProcessorEnabled;
	}

	public TerminologyExtractorOptions setPostProcessorEnabled(boolean postProcessorEnabled) {
		this.postProcessorEnabled = postProcessorEnabled;
		return this;
	}

	public PostProcessorOptions getPostProcessorConfig() {
		return postProcessorConfig;
	}

	public TerminologyExtractorOptions setPostProcessorConfig(PostProcessorOptions postProcessorConfig) {
		this.postProcessorConfig = postProcessorConfig;
		return this;
	}

	public boolean isGathererEnabled() {
		return gathererEnabled;
	}

	public TerminologyExtractorOptions setGathererEnabled(boolean gathererEnabled) {
		this.gathererEnabled = gathererEnabled;
		return this;
	}

	public GathererOptions getGathererConfig() {
		return gathererConfig;
	}

	public TerminologyExtractorOptions setGathererConfig(GathererOptions gathererConfig) {
		this.gathererConfig = gathererConfig;
		return this;
	}

	public TermRankingOptions getRankingConfig() {
		return rankingConfig;
	}

	public TerminologyExtractorOptions setRankingConfig(TermRankingOptions rankingConfig) {
		this.rankingConfig = rankingConfig;
		return this;
	}

	public boolean isMorphologicalAnalysisEnabled() {
		return morphologicalAnalysisEnabled;
	}

	public void setMorphologicalAnalysisEnabled(boolean morphologicalAnalysisEnabled) {
		this.morphologicalAnalysisEnabled = morphologicalAnalysisEnabled;
	}

	public MorphologicalOptions getMorphologicalConfig() {
		return morphologicalConfig;
	}

	public void setMorphologicalConfig(MorphologicalOptions morphologicalConfig) {
		this.morphologicalConfig = morphologicalConfig;
	}
}
