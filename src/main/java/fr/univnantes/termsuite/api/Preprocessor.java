package fr.univnantes.termsuite.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ImportModule;
import fr.univnantes.termsuite.framework.service.PreprocessorService;
import fr.univnantes.termsuite.framework.service.TermOccAnnotationImporter;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.uima.PipelineListener;
import fr.univnantes.termsuite.uima.PreparationPipelineOptions;
import fr.univnantes.termsuite.utils.TermHistory;

public class Preprocessor {
	
	private static final Logger logger = LoggerFactory.getLogger(Preprocessor.class);
	
	@Inject
	PreprocessorService preprocessorService;

	private boolean parallel;
	private Path taggerPath;
	private PreparationPipelineOptions options = new PreparationPipelineOptions();
	private Optional<ResourceConfig> resourceOptions = Optional.empty();
	private Optional<TermHistory> history = Optional.empty();
	private Optional<PipelineListener> listener = Optional.empty();
	private List<AnalysisEngineDescription> customAEs = new ArrayList<>();
	
	public Preprocessor setTaggerPath(Path taggerPath) {
		this.taggerPath = taggerPath;
		return this;
	}
	
	public Preprocessor setListener(PipelineListener listener) {
		this.listener = Optional.of(listener);
		return this;
	}
	
	public Preprocessor setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	public Preprocessor setOptions(PreparationPipelineOptions options) {
		this.options = options;
		return this;
	}
	
	public Preprocessor setResourceOptions(ResourceConfig resourceOptions) {
		this.resourceOptions = Optional.of(resourceOptions);
		return this;
	}
	
	public Preprocessor parallel() {
		this.parallel = true;
		return this;
	}
	
	public Preprocessor addCustomAE(AnalysisEngineDescription customAE) {
		this.customAEs.add(customAE);
		return this;
	}
	
	public IndexedCorpus toPersistentIndexedCorpus(TextCorpus textCorpus, String storeUrl, int maxSize) {
		OccurrenceStore store = TermSuiteFactory.createPersitentOccurrenceStore(storeUrl, textCorpus.getLang());
		return consumeToIndexedCorpus(textCorpus, maxSize, store);
	}

	public IndexedCorpus toIndexedCorpus(TextCorpus textCorpus, int maxSize) {
		OccurrenceStore occurrenceStore = TermSuiteFactory.createMemoryOccurrenceStore(textCorpus.getLang());
		return  consumeToIndexedCorpus(textCorpus, maxSize, occurrenceStore);
	}

	public IndexedCorpus consumeToIndexedCorpus(TextCorpus textCorpus, int maxSize, OccurrenceStore occurrenceStore) {
		String name = preprocessorService.generateTerminologyName(textCorpus);
		
		if(preprocessedCorpusCachePath.isPresent()) {
			if(preprocessedCorpusCachePath.get().toFile().exists()) {
				logger.info("Cached preprocessed terminology found at path {}", preprocessedCorpusCachePath.get());
				try {
					return TermSuiteFactory.createJsonLoader().load(preprocessedCorpusCachePath.get());
				} catch (IOException e) {
					logger.error("Could not load cached preprocessed terminology due to unexpected error", e);
					logger.info("Ignoring cache");
				}
				
			} else
				logger.info("No cached terminology found");
		}
		
		Terminology termino = TermSuiteFactory.createTerminology(textCorpus.getLang(), name);
		OccurrenceStore store = occurrenceStore;
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(termino, store);
		Injector injector = Guice.createInjector(new ImportModule(indexedCorpus, maxSize));
		TermOccAnnotationImporter importer = injector.getInstance(TermOccAnnotationImporter.class);

		logger.info("Starting preprocessing pipeline");
		asStream(textCorpus).forEach(importer::importCas);
		
		if(preprocessedCorpusCachePath.isPresent()) {
			logger.info("Saving preprocessed terminology to cache path {}", preprocessedCorpusCachePath.get());
			try {
				TermSuiteFactory.createJsonExporter().export(indexedCorpus, preprocessedCorpusCachePath.get());
			} catch (IOException e) {
				logger.error("Could not save preprocessed terminology to cache due to unexpected error", e);
			}
		}
		return indexedCorpus;
	}
	
	public PreprocessedCorpus toPreparedCorpusJSON(TextCorpus textCorpus, Path jsonDir) {
		final PreprocessedCorpus targetCorpus = new PreprocessedCorpus(textCorpus.getLang(), jsonDir, PreprocessedCorpus.JSON_PATTERN, PreprocessedCorpus.JSON_EXTENSION);
		
		asStream(textCorpus)
			.forEach(cas -> preprocessorService.consumeToTargetXMICorpus(cas, textCorpus, targetCorpus));
		return targetCorpus;
	}

	public PreprocessedCorpus toPreparedCorpusXMI(TextCorpus textCorpus, Path xmiDir) {
		final PreprocessedCorpus targetCorpus = new PreprocessedCorpus(textCorpus.getLang(), xmiDir, PreprocessedCorpus.XMI_PATTERN, PreprocessedCorpus.XMI_EXTENSION);
		
		asStream(textCorpus)
			.forEach(cas -> preprocessorService.consumeToTargetXMICorpus(cas, textCorpus, targetCorpus));
		return targetCorpus;
	}

	public Stream<JCas> asStream(TextCorpus textCorpus) {
		Stream<JCas> stream = preprocessorService.prepare(
				textCorpus, 
				taggerPath, 
				options, 
				resourceOptions, 
				history, 
				listener, 
				customAEs.toArray(new AnalysisEngineDescription[customAEs.size()]));
		if(parallel)
			stream.parallel();
		return stream;
	}
	
	private Optional<Path> preprocessedCorpusCachePath = Optional.empty();

	public Preprocessor setPreprocessedCorpusCache(Path cachedPath) {
		preprocessedCorpusCachePath = Optional.of(cachedPath);
		return this;
	}
}
