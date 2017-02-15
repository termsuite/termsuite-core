package fr.univnantes.termsuite.api;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.PreprocessingPipelineBuilder;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ImportModule;
import fr.univnantes.termsuite.framework.modules.PreprocessingModule;
import fr.univnantes.termsuite.framework.modules.TermSuiteModule;
import fr.univnantes.termsuite.framework.service.CorpusService;
import fr.univnantes.termsuite.framework.service.PreprocessorService;
import fr.univnantes.termsuite.framework.service.TermOccAnnotationImporter;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.CorpusMetadata;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.model.TextCorpus;
import fr.univnantes.termsuite.uima.PipelineListener;
import fr.univnantes.termsuite.utils.JCasUtils;
import fr.univnantes.termsuite.utils.TermHistory;

public class Preprocessor {
	private static final Logger logger = LoggerFactory.getLogger(Preprocessor.class);
	
	@Inject
	private CorpusService corpusService;
	
	private Optional<Tagger> tagger = Optional.of(Tagger.TREE_TAGGER);
	private Optional<Boolean> documentLoggingEnabled = Optional.of(true);
	private Optional<Boolean> fixedExpressionEnabled = Optional.of(false);

	private Path taggerPath;
	private Optional<ResourceConfig> resourceOptions = Optional.empty();
	private Optional<TermHistory> history = Optional.empty();
	private Optional<PipelineListener> listener = Optional.empty();
	private List<AnalysisEngineDescription> customAEs = new ArrayList<>();
	
	public Preprocessor setTaggerPath(Path taggerPath) {
		this.taggerPath = taggerPath;
		return this;
	}
	
	public Preprocessor setTagger(Tagger tagger) {
		this.tagger = Optional.of(tagger);
		return this;
	}
	
	public Preprocessor setDocumentLoggingEnabled(boolean documentLoggingEnabled) {
		this.documentLoggingEnabled = Optional.of(documentLoggingEnabled);
		return this;
	}
	
	public Preprocessor setFixedExpressionEnabled(boolean fixedExpressionEnabled) {
		this.fixedExpressionEnabled = Optional.of(fixedExpressionEnabled);
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
	
	public Preprocessor setResourceOptions(ResourceConfig resourceOptions) {
		this.resourceOptions = Optional.of(resourceOptions);
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
		PreprocessorService preprocService = asService(textCorpus.getLang());
		String name = preprocService.generateTerminologyName(textCorpus);
		
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

		
		Stream<JCas> preapredStream = asStream(textCorpus);
		if(xmiPath.isPresent())
			preapredStream = preapredStream.map(cas -> preprocService.toXMICas(
					cas, 
					toCasFile(xmiPath.get(), cas, "xmi")));
		if(tsvPath.isPresent())
			preapredStream = preapredStream.map(cas -> preprocService.toTSVCas(
					cas, 
					toCasFile(tsvPath.get(), cas, "tsv")));
		if(jsonPath.isPresent())
			preapredStream = preapredStream.map(cas -> preprocService.toJSONCas(
					cas, 
					toCasFile(jsonPath.get(), cas, "json")));
		
		
		
		logger.info("Starting preprocessing pipeline");
		preapredStream.forEach(importer::importCas);
		
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
	
	private Path toCasFile(Path parentDestination, JCas cas, String newExtension) {

		String txtUri = JCasUtils.getSourceDocumentAnnotation(cas).get()
				.getUri()
				.replaceAll("\\.txt$", "." + newExtension);

		Path resolve = parentDestination.resolve(Paths.get(txtUri).getFileName());

		return resolve;
	}

	public PreprocessedCorpus toPreparedCorpusJSON(TextCorpus textCorpus, Path jsonDir) {
		final PreprocessedCorpus targetCorpus = new PreprocessedCorpus(textCorpus.getLang(), jsonDir, PreprocessedCorpus.JSON_PATTERN, PreprocessedCorpus.JSON_EXTENSION);
		
		asStream(textCorpus)
			.forEach(cas -> asService(textCorpus.getLang()).consumeToTargetXMICorpus(cas, textCorpus, targetCorpus));
		return targetCorpus;
	}

	public PreprocessedCorpus toPreparedCorpusXMI(TextCorpus textCorpus, Path xmiDir) {
		final PreprocessedCorpus targetCorpus = new PreprocessedCorpus(textCorpus.getLang(), xmiDir, PreprocessedCorpus.XMI_PATTERN, PreprocessedCorpus.XMI_EXTENSION);
		
		asStream(textCorpus)
			.forEach(cas -> asService(textCorpus.getLang()).consumeToTargetXMICorpus(cas, textCorpus, targetCorpus));
		return targetCorpus;
	}

	public Stream<JCas> asStream(TextCorpus textCorpus) {
		Stream<JCas> stream = asService(textCorpus).prepare(textCorpus);
		return stream;
	}
	
	private Optional<Path> preprocessedCorpusCachePath = Optional.empty();
	public Preprocessor setPreprocessedCorpusCache(Path cachedPath) {
		preprocessedCorpusCachePath = Optional.of(cachedPath);
		return this;
	}

	public Stream<JCas> asStream(Lang lang, Stream<Document> documents) {
		return asService(lang).prepare(
				documents
				);
	}
	
	public PreprocessorService asService(Lang lang) {
		return asService(lang, new CorpusMetadata(Charset.defaultCharset()));
	}
	
	public PreprocessorService asService(TextCorpus textCorpus) {
		try {
			return asService(
					textCorpus.getLang(), 
					corpusService.computeMetadata(textCorpus));
		} catch(IOException e) {
			throw new TermSuiteException(e);
		}
	}
	
	public PreprocessorService asService(Lang lang, CorpusMetadata corpusMetadata) {
		PreprocessingPipelineBuilder builder = PreprocessingPipelineBuilder
				.create(lang, taggerPath)
				.setNbDocuments(corpusMetadata.getNbDocuments())
				.setCorpusSize(corpusMetadata.getTotalSize());
		
		if(tagger.isPresent()) 
			builder.setTagger(tagger.get());
		
		if(documentLoggingEnabled.isPresent())
			builder.setDocumentLoggingEnabled(documentLoggingEnabled.get());
		
		if(fixedExpressionEnabled.isPresent())
			builder.setFixedExpressionEnabled(fixedExpressionEnabled.get());
			
		if(listener.isPresent())
			builder.addPipelineListener(listener.get());
		
		for(AnalysisEngineDescription customAE:customAEs) 
			builder.addCustomAE(customAE);
		
		if(resourceOptions.isPresent()) 
			builder.setResourceConfig(resourceOptions.get());


		if(history.isPresent())
				builder.setHistory(history.get());
		
		final AnalysisEngine aae;
		try {
			logger.info("Initializing analysis engine");
			ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();
	    	AnalysisEngineDescription aaeDesc;
			aaeDesc = createEngineDescription(builder.create());
			// Instantiate AAE
			aae = UIMAFramework.produceAnalysisEngine(aaeDesc, resMgr, null);
		} catch (ResourceInitializationException e) {
			throw new TermSuiteException(e);
		}
		
		
		return Guice.createInjector(
					new TermSuiteModule(),
					new PreprocessingModule(lang, aae))
				.getInstance(PreprocessorService.class);
	}

	private Optional<Path> xmiPath = Optional.empty();
	private Optional<Path> tsvPath = Optional.empty();
	private Optional<Path> jsonPath = Optional.empty();
	
	public void toXMI(Path xmiPath) {
		this.xmiPath = Optional.of(xmiPath);
	}

	public void toTSV(Path tsvPath) {
		this.tsvPath = Optional.of(tsvPath);
	}

	public void toJSON(Path jsonPath) {
		this.jsonPath = Optional.of(jsonPath);
	}

}
