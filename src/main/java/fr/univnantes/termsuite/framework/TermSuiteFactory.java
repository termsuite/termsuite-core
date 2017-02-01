package fr.univnantes.termsuite.framework;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.modules.ExtractorModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.framework.pipeline.AggregateEngineRunner;
import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.framework.pipeline.SimpleEngineRunner;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.occurrences.EmptyOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.XodusOccurrenceStore;
import fr.univnantes.termsuite.utils.TermHistory;

public class TermSuiteFactory {

	public static Terminology createTerminology(Lang lang, String name) {
		return new Terminology(name, lang);
	}

	public static OccurrenceStore createEmptyOccurrenceStore(Lang lang) {
		return new EmptyOccurrenceStore(lang);
	}

	public static OccurrenceStore createMemoryOccurrenceStore(Lang lang) {
		return new MemoryOccurrenceStore(lang);
	}

	public static OccurrenceStore createPersitentOccurrenceStore(String storeUrl, Lang lang) {
		return new XodusOccurrenceStore(lang, storeUrl);
	}
	
	public static EngineRunner createEngineRunner(
			Class<? extends Engine> engineClass, 
			IndexedCorpus terminology, 
			ResourceConfig config, 
			TermHistory history, 
			Object... parameters) {
		EngineDescription description = new EngineDescription(engineClass.getSimpleName(), engineClass, parameters);
		Injector injector = createExtractorInjector(terminology, config, history);
		return createEngineRunner(description, injector, null);
	}

	public static Injector createExtractorInjector(
			IndexedCorpus terminology, 
			ResourceConfig config,
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

	public static Pipeline createPipeline(Class<? extends Engine> engineClass, IndexedCorpus terminology,
			ResourceConfig resourceConfig, TermHistory history, Object... parameters) {
		EngineDescription description = new EngineDescription(engineClass.getSimpleName(), engineClass, parameters);
		Injector injector = createExtractorInjector(terminology, resourceConfig, history);
		EngineRunner runner = createEngineRunner(description, injector, null);
		Pipeline pipeline = injector.getInstance(Pipeline.class);
		pipeline.setRunner(runner);
		return pipeline;
	}

	public static IndexedCorpus createIndexedCorpus(Terminology termino, OccurrenceStore store) {
		return new IndexedCorpus(termino, store);
	}

	public static IndexedCorpus createIndexedCorpus(Lang lang, String name) {
		return createIndexedCorpus(createTerminology(lang, name), createMemoryOccurrenceStore(lang));
	}

	public static TermRelation createVariation(VariationType variationType, Term from, Term to) {
		TermRelation relation = new TermRelation(RelationType.VARIATION, from, to);
		for(VariationType vType:VariationType.values())
			relation.setProperty(vType.getRelationProperty(), false);
		relation.setProperty(RelationProperty.VARIATION_TYPE, variationType);
		relation.setProperty(variationType.getRelationProperty(), true);
		return relation;
	}
}
