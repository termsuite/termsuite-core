package fr.univnantes.termsuite.api;

import com.google.inject.Guice;

import fr.univnantes.termsuite.engines.TerminologyExtractorEngine;
import fr.univnantes.termsuite.framework.ExtractorModule;
import fr.univnantes.termsuite.model.Terminology;

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
	
	private TerminologyExtractorOptions options = new TerminologyExtractorOptions();
	private ResourceConfig resourceConfig = new ResourceConfig();
	
	public TerminoExtractor setResourceConfig(ResourceConfig resourceConfig) {
		this.resourceConfig = resourceConfig;
		return this;
	}
	
	public TerminoExtractor setOptions(TerminologyExtractorOptions options) {
		this.options = options;
		return this;
	}
	
	public void execute(Terminology terminology) {
		ExtractorModule extractorModule = new ExtractorModule(terminology, options, resourceConfig);
		TerminologyExtractorEngine engine = Guice.createInjector(extractorModule)
			.getInstance(TerminologyExtractorEngine.class);
		
		engine.execute();
	}
}
