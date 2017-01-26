package fr.univnantes.termsuite.framework;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.framework.modules.ExtractorModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.framework.pipeline.AggregateEngineRunner;
import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.framework.pipeline.SimpleEngineRunner;
import fr.univnantes.termsuite.index.MemoryTerminology;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.occurrences.EmptyOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.XodusOccurrenceStore;
import fr.univnantes.termsuite.utils.TermHistory;

public class TermSuiteFactory {

	public static Terminology createTerminology(Lang lang, String name, boolean withOccurrences) {
		OccurrenceStore store = withOccurrences ? 
				new MemoryOccurrenceStore(lang)
				: new EmptyOccurrenceStore(lang);
				return new MemoryTerminology(name, lang, store);
	}

	public static Terminology createPersitentTerminology(String storeUrl, Lang lang, String name) {
		OccurrenceStore store = new XodusOccurrenceStore(lang, storeUrl);
		return new MemoryTerminology(name, lang, store);
	}
	
	public static EngineRunner createEngineRunner(
			Class<? extends Engine> engineClass, 
			Terminology terminology, 
			ResourceConfig config, 
			TermHistory history, 
			Object... parameters) {
		EngineDescription description = new EngineDescription(engineClass.getSimpleName(), engineClass, parameters);
		Injector injector = createExtractorInjector(terminology, config, history);
		return createEngineRunner(description, injector, null);
	}

	public static Injector createExtractorInjector(Terminology terminology, ResourceConfig config,
			TermHistory history) {
		Injector injector = Guice.createInjector(
				new ResourceModule(config),
				new ExtractorModule(terminology, history)
			);
		return injector;
	}

	public static EngineRunner createEngineRunner(EngineDescription description,
			Injector injector, AggregateEngineRunner parent) {
		if(description.isAggregated()) 
			return new AggregateEngineRunner(description, injector, parent);
		else
			return new SimpleEngineRunner(description, injector, parent);
	}

	public static Pipeline createPipeline(Class<? extends Engine> engineClass, Terminology terminology,
			ResourceConfig resourceConfig, TermHistory history, Object... parameters) {
		EngineDescription description = new EngineDescription(engineClass.getSimpleName(), engineClass, parameters);
		Injector injector = createExtractorInjector(terminology, resourceConfig, history);
		EngineRunner runner = createEngineRunner(description, injector, null);
		Pipeline pipeline = injector.getInstance(Pipeline.class);
		pipeline.setRunner(runner);
		return pipeline;
	}
}
