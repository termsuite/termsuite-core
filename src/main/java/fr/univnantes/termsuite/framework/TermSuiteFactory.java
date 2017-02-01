package fr.univnantes.termsuite.framework;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.export.BaseExporter;
import fr.univnantes.termsuite.export.TerminologyExporter;
import fr.univnantes.termsuite.export.json.JsonExporter;
import fr.univnantes.termsuite.export.json.JsonOptions;
import fr.univnantes.termsuite.export.other.CompoundExporter;
import fr.univnantes.termsuite.export.other.TermDistributionExporter;
import fr.univnantes.termsuite.export.other.VariantDistributionExporter;
import fr.univnantes.termsuite.export.other.VariantEvalExporter;
import fr.univnantes.termsuite.export.other.VariantEvalExporterOptions;
import fr.univnantes.termsuite.export.other.VariationExporter;
import fr.univnantes.termsuite.export.other.VariationRuleExamplesExporter;
import fr.univnantes.termsuite.export.tsv.TsvExporter;
import fr.univnantes.termsuite.export.tsv.TsvOptions;
import fr.univnantes.termsuite.framework.modules.IndexedCorpusModule;
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
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Relation;
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
				new IndexedCorpusModule(terminology, history)
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

	public static Relation createVariation(VariationType variationType, Term from, Term to) {
		Relation relation = new Relation(RelationType.VARIATION, from, to);
		for(VariationType vType:VariationType.values())
			relation.setProperty(vType.getRelationProperty(), false);
		relation.setProperty(RelationProperty.VARIATION_TYPE, variationType);
		relation.setProperty(variationType.getRelationProperty(), true);
		return relation;
	}

	public static TerminologyExporter createTsvExporter() {
		return createTsvExporter(new TsvOptions());
	}

	public static TerminologyExporter createTsvExporter(TsvOptions options) {
		return new BaseExporter(new TsvExporter(options));
	}
	
	public static TerminologyExporter createJsonExporter(JsonOptions options) {
		return new BaseExporter(new JsonExporter(options));
	}
	
	public static TerminologyExporter createJsonExporter() {
		return new BaseExporter(new JsonExporter(new JsonOptions()));
	}

	public static TerminologyExporter createCompoundExporter() {
		return new BaseExporter(new CompoundExporter());
	}

	public static TerminologyExporter createVariantEvalExporter(VariantEvalExporterOptions options) {
		return new BaseExporter(new VariantEvalExporter(options));
	}

	public static TerminologyExporter createTermDistributionExporter(List<TermProperty> termProperties, Predicate<Term> selector) {
		return new BaseExporter(new TermDistributionExporter(termProperties, selector));
	}

	public static TerminologyExporter createVariantDistributionExporter(List<RelationProperty> relationProperties, Predicate<Relation> selector) {
		return new BaseExporter(new VariantDistributionExporter(relationProperties, selector));
	}

	public static TerminologyExporter createVariationExporter(VariationType... variationTypes) {
		return new BaseExporter(new VariationExporter(Lists.newArrayList(variationTypes)));
	}

	public static TerminologyExporter createVariationRuleExamplesExporter() {
		return new BaseExporter(new VariationRuleExamplesExporter());
	}

	

}
