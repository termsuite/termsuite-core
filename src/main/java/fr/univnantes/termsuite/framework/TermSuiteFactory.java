package fr.univnantes.termsuite.framework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.PreprocessedCorpus;
import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.modules.IndexedCorpusModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.framework.pipeline.AggregateEngineRunner;
import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.framework.pipeline.SimpleEngineRunner;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.io.BaseIndexedCorpusExporter;
import fr.univnantes.termsuite.io.IndexedCorpusExporter;
import fr.univnantes.termsuite.io.IndexedCorpusImporter;
import fr.univnantes.termsuite.io.json.JsonExporter;
import fr.univnantes.termsuite.io.json.JsonLoader;
import fr.univnantes.termsuite.io.json.JsonOptions;
import fr.univnantes.termsuite.io.other.CompoundExporter;
import fr.univnantes.termsuite.io.other.TermDistributionExporter;
import fr.univnantes.termsuite.io.other.VariantDistributionExporter;
import fr.univnantes.termsuite.io.other.VariantEvalExporter;
import fr.univnantes.termsuite.io.other.VariantEvalExporterOptions;
import fr.univnantes.termsuite.io.other.VariationExporter;
import fr.univnantes.termsuite.io.other.VariationRuleExamplesExporter;
import fr.univnantes.termsuite.io.tbx.TbxExporter;
import fr.univnantes.termsuite.io.tsv.TsvExporter;
import fr.univnantes.termsuite.io.tsv.TsvOptions;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
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
			IndexedCorpus terminology) {
		Injector injector = Guice.createInjector(
				new ResourceModule(null),
				new IndexedCorpusModule(terminology, null)
			);
		return injector;
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
			Object... parameters) {
		return createPipeline(engineClass, terminology, null, null, parameters);
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
		relation.setProperty(variationType.getRelationProperty(), true);
		return relation;
	}

	public static IndexedCorpusExporter createTsvExporter() {
		return createTsvExporter(new TsvOptions());
	}



	public static IndexedCorpusExporter createTsvExporter(TsvOptions options) {
		return new BaseIndexedCorpusExporter(new TsvExporter(options));
	}
	
	public static IndexedCorpusExporter createTbxExporter() {
		return new BaseIndexedCorpusExporter(new TbxExporter());
	}

	public static IndexedCorpusExporter createJsonExporter(JsonOptions options) {
		return new BaseIndexedCorpusExporter(new JsonExporter(options));
	}
	
	public static IndexedCorpusExporter createJsonExporter() {
		return new BaseIndexedCorpusExporter(new JsonExporter(new JsonOptions()));
	}

	public static IndexedCorpusExporter createCompoundExporter() {
		return new BaseIndexedCorpusExporter(new CompoundExporter());
	}

	public static IndexedCorpusExporter createVariantEvalExporter(VariantEvalExporterOptions options) {
		return new BaseIndexedCorpusExporter(new VariantEvalExporter(options));
	}

	public static IndexedCorpusExporter createTermDistributionExporter(List<TermProperty> termProperties, Predicate<Term> selector) {
		return new BaseIndexedCorpusExporter(new TermDistributionExporter(termProperties, selector));
	}

	public static IndexedCorpusExporter createVariantDistributionExporter(List<RelationProperty> relationProperties, Predicate<RelationService> selector) {
		return new BaseIndexedCorpusExporter(new VariantDistributionExporter(relationProperties, selector));
	}

	public static IndexedCorpusExporter createVariationExporter(VariationType... variationTypes) {
		return new BaseIndexedCorpusExporter(new VariationExporter(Lists.newArrayList(variationTypes)));
	}

	public static IndexedCorpusExporter createVariationRuleExamplesExporter() {
		return new BaseIndexedCorpusExporter(new VariationRuleExamplesExporter());
	}

	public static IndexedCorpusImporter createJsonLoader(JsonOptions jsonOptions) {
		return new JsonLoader(jsonOptions);
	}

	public static IndexedCorpusImporter createJsonLoader() {
		return createJsonLoader(new JsonOptions());
	}

	/**
	 * 
	 * Creates a {@link PreprocessedCorpus}. Detects if it is XMI or JSON.
	 * 
	 * @param lang
	 * 			the language of the input corpus
	 * @param rootDirectory
	 * 			the root directory of the corpus
	 * @return
	 * 			The created {@link PreprocessedCorpus}
	 */
	public static PreprocessedCorpus createPreprocessedCorpus(Lang lang, Path rootDirectory) {
		try {
			AtomicInteger json = new AtomicInteger(0);
			AtomicInteger xmi = new AtomicInteger(0);
			
			Files.walk(rootDirectory)
				.filter(file -> file.toFile().isFile())
				.forEach(filePath-> {
					if(filePath.getFileName().endsWith(PreprocessedCorpus.JSON_EXTENSION))
						json.incrementAndGet();
					else if(filePath.getFileName().endsWith(PreprocessedCorpus.XMI_EXTENSION))
						xmi.incrementAndGet();
				});
			
			if(json.intValue() > xmi.intValue()) 
				return createJsonPreprocessedCorpus(lang, rootDirectory);
			return createXMIPreprocessedCorpus(lang, rootDirectory);
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
	
	public static PreprocessedCorpus createJsonPreprocessedCorpus(Lang lang, Path rootDirectory) {
		return new PreprocessedCorpus(
				lang, 
				rootDirectory, 
				PreprocessedCorpus.JSON_PATTERN, 
				PreprocessedCorpus.JSON_EXTENSION);
	}

	public static PreprocessedCorpus createXMIPreprocessedCorpus(Lang lang, Path rootDirectory) {
		return new PreprocessedCorpus(
				lang, 
				rootDirectory, 
				PreprocessedCorpus.XMI_PATTERN, 
				PreprocessedCorpus.XMI_EXTENSION);
	}

	public static TermIndex createTermIndex(Terminology terminology, TermIndexType lemmaLowerCase) {
		TermIndex index;
		try {
			index = new TermIndex(lemmaLowerCase.getProviderClass().newInstance());
			terminology.getTerms().values().forEach(index::addToIndex);
			return index;
		} catch (Exception e) {
			throw new TermSuiteException(e);
		}
	}

	public static TermIndex createTermIndex(TerminologyService termino, TermIndexType lemmaLowerCase) {
		TermIndex index;
		try {
			index = new TermIndex(lemmaLowerCase.getProviderClass().newInstance());
			termino.terms().map(TermService::getTerm).forEach(index::addToIndex);
			return index;
		} catch (Exception e) {
			throw new TermSuiteException(e);
		}
	}
}
