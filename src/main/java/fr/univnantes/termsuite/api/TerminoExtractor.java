package fr.univnantes.termsuite.api;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.engines.TerminologyExtractorEngine;
import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.ExtractorLifeCycle;
import fr.univnantes.termsuite.framework.ExtractorModule;
import fr.univnantes.termsuite.framework.TerminologyEngine;
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
	
	private TermHistory history = null;
	private ExtractorOptions options;
	private ResourceConfig resourceConfig = new ResourceConfig();
	
	public TerminoExtractor setResourceConfig(ResourceConfig resourceConfig) {
		this.resourceConfig = resourceConfig;
		return this;
	}
	
	public TerminoExtractor watch(String termKey) {
		if(this.history == null) {
			this.history = new TermHistory();
		}
		this.history.addWatchedGroupingKeys(termKey);
		return this;
	}

	public TerminoExtractor setHistory(TermHistory history) {
		this.history = history;
		return this;
	}

	public TerminoExtractor setOptions(ExtractorOptions options) {
		this.options = options;
		return this;
	}
	
	public void execute(Terminology terminology) {
		if(options == null)
			options = TermSuite.getDefaultExtractorConfig(terminology.getLang());
		ExtractorModule extractorModule = new ExtractorModule(terminology, resourceConfig, history);
		Injector injector = Guice.createInjector(
				extractorModule);
		EngineDescription engineDescription = TermSuite.createEngineDescription(TerminologyExtractorEngine.class, options);
		ExtractorLifeCycle lifeCycle = new ExtractorLifeCycle(injector);
		TerminologyEngine engine = lifeCycle.create(engineDescription);
		engine.execute();
	}
}
