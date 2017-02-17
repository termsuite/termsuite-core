package fr.univnantes.termsuite.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.engines.gatherer.GathererOptions;
import fr.univnantes.termsuite.engines.postproc.TermRankingOptions;
import fr.univnantes.termsuite.engines.splitter.MorphologicalOptions;
import fr.univnantes.termsuite.resources.PostProcessorOptions;
import fr.univnantes.termsuite.utils.JsonConfigObject;

public class ExtractorOptions extends JsonConfigObject {

	@JsonProperty("pre-filter")
	private TerminoFilterOptions preFilterConfig = new TerminoFilterOptions();
	
	@JsonProperty("contextualizer")
	private ContextualizerOptions contextualizerOptions = new ContextualizerOptions();
	
	@JsonProperty("morphology")
	private MorphologicalOptions morphologicalConfig = new MorphologicalOptions();

	@JsonProperty("gatherer")
	private GathererOptions gathererConfig = new GathererOptions();

	@JsonProperty("post-processor")
	private PostProcessorOptions postProcessorConfig = new PostProcessorOptions();

	@JsonProperty("post-filter")
	private TerminoFilterOptions postFilterConfig = new TerminoFilterOptions();
	
	@JsonProperty("ranking")
	private TermRankingOptions rankingConfig = new TermRankingOptions();


	/*
	 * Ensure it is only instanciated through default JSON config in classpath
	 */
	public ExtractorOptions() {
		super();
	}

	public TerminoFilterOptions getPreFilterConfig() {
		return preFilterConfig;
	}

	public ExtractorOptions setPreFilterConfig(TerminoFilterOptions preFilterConfig) {
		this.preFilterConfig = preFilterConfig;
		return this;
	}

	public TerminoFilterOptions getPostFilterConfig() {
		return postFilterConfig;
	}

	public ExtractorOptions setPostFilterConfig(TerminoFilterOptions postFilterConfig) {
		this.postFilterConfig = postFilterConfig;
		return this;
	}

	public PostProcessorOptions getPostProcessorConfig() {
		return postProcessorConfig;
	}

	public ExtractorOptions setPostProcessorConfig(PostProcessorOptions postProcessorConfig) {
		this.postProcessorConfig = postProcessorConfig;
		return this;
	}

	public GathererOptions getGathererConfig() {
		return gathererConfig;
	}

	public ExtractorOptions setGathererConfig(GathererOptions gathererConfig) {
		this.gathererConfig = gathererConfig;
		return this;
	}

	public TermRankingOptions getRankingConfig() {
		return rankingConfig;
	}

	public ExtractorOptions setRankingConfig(TermRankingOptions rankingConfig) {
		this.rankingConfig = rankingConfig;
		return this;
	}

	public MorphologicalOptions getMorphologicalConfig() {
		return morphologicalConfig;
	}

	public ExtractorOptions setMorphologicalConfig(MorphologicalOptions morphologicalConfig) {
		this.morphologicalConfig = morphologicalConfig;
		return this;
	}
	
	public ContextualizerOptions getContextualizerOptions() {
		return contextualizerOptions;
	}
	
	public ExtractorOptions setContextualizerOptions(ContextualizerOptions contextualizerOptions) {
		this.contextualizerOptions = contextualizerOptions;
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ExtractorOptions) {
			ExtractorOptions o = (ExtractorOptions)obj;
			return 
					Objects.equals(preFilterConfig, o.preFilterConfig)
					&& Objects.equals(postFilterConfig, o.postFilterConfig)
					&& Objects.equals(morphologicalConfig, o.morphologicalConfig)
					&& Objects.equals(gathererConfig, o.gathererConfig)
					&& Objects.equals(rankingConfig, o.rankingConfig)
					&& Objects.equals(contextualizerOptions, o.contextualizerOptions)
					&& Objects.equals(postProcessorConfig, o.postProcessorConfig);
		} else 
			return false;
	}
	
}
