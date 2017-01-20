package fr.univnantes.termsuite.api;

import java.util.Optional;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.engines.TerminologyExtractorEngine;
import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.EngineFactory;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.modules.ExtractorModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.model.Terminology;
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
	
	public void execute(Terminology terminology) {
		if(options == null)
			options = TermSuite.getDefaultExtractorConfig(terminology.getLang());
		Injector injector = Guice.createInjector(
				new ResourceModule(resourceConfig.orElse(null)),
				new ExtractorModule(terminology, history.orElse(null))
				);
		EngineDescription engineDescription = TermSuite.createEngineDescription(TerminologyExtractorEngine.class, options);
		EngineFactory lifeCycle = new EngineFactory(injector);
		TerminologyEngine engine = lifeCycle.create(engineDescription);
		engine.execute();
	}
}
