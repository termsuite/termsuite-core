package fr.univnantes.termsuite.api;

import java.util.Optional;

import fr.univnantes.termsuite.engines.TerminologyExtractorEngine;
import fr.univnantes.termsuite.framework.Pipeline;
import fr.univnantes.termsuite.framework.PipelineStats;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.utils.TermHistory;

/**
 * 
 * A builder and launcher class for execute a terminology extraction
 * pipeline from a PreprocessedCorpus or a Terminology.
 * 
 * @author Damien Cram
 * 
 * @see Preprocessor
 *
 */
public class TerminoExtractor {
	
	private Optional<TermHistory> history = Optional.empty();
	private ExtractorOptions options;
	private Optional<ResourceConfig> resourceConfig = Optional.empty();
	
	public TerminoExtractor setResourceConfig(ResourceConfig resourceConfig) {
		this.resourceConfig = Optional.of(resourceConfig);
		return this;
	}
	
	public TerminoExtractor watch(String termKey) {
		if(!this.history.isPresent()) {
			this.history = Optional.of(new TermHistory());
		}
		this.history.get().addWatchedGroupingKeys(termKey);
		return this;
	}

	public TerminoExtractor setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}

	public TerminoExtractor setOptions(ExtractorOptions options) {
		this.options = options;
		return this;
	}
	
	public PipelineStats execute(IndexedCorpus corpus) {
		if(options == null)
			options = TermSuite.getDefaultExtractorConfig(corpus.getTerminology().getLang());
		Pipeline pipeline = TermSuiteFactory.createPipeline(
				TerminologyExtractorEngine.class, 
				corpus,
				resourceConfig.orElse(null),
				history.orElse(null),
				options
				);
		return pipeline.run();
	}
}
