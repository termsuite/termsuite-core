package fr.univnantes.termsuite.api;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.PreprocessingPipelineBuilder;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ImporterModule;
import fr.univnantes.termsuite.framework.modules.PreprocessingModule;
import fr.univnantes.termsuite.framework.modules.TermSuiteModule;
import fr.univnantes.termsuite.framework.service.CorpusService;
import fr.univnantes.termsuite.framework.service.ImporterService;
import fr.univnantes.termsuite.framework.service.PreprocessorService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.CorpusMetadata;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.uima.PipelineListener;
import fr.univnantes.termsuite.uima.readers.JsonCasSerializer;
import fr.univnantes.termsuite.uima.readers.TSVCasSerializer;
import fr.univnantes.termsuite.utils.JCasUtils;
import fr.univnantes.termsuite.utils.TermHistory;

public class Preprocessor {
	private static final Logger logger = LoggerFactory.getLogger(Preprocessor.class);
	
	@Inject
	private CorpusService corpusService;
	
	private Optional<Tagger> tagger = Optional.of(Tagger.TREE_TAGGER);
	private Optional<Boolean> documentLoggingEnabled = Optional.of(true);
	private Optional<Boolean> fixedExpressionEnabled = Optional.of(false);


	private Optional<Path> xmiPath = Optional.empty();
	private Optional<Path> tsvPath = Optional.empty();
	private Optional<Path> jsonPath = Optional.empty();

	
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
	
	public IndexedCorpus toPersistentIndexedCorpus(TXTCorpus textCorpus, String storeUrl, int maxSize) {
		OccurrenceStore store = TermSuiteFactory.createPersitentOccurrenceStore(storeUrl, textCorpus.getLang());
		return toIndexedCorpus(textCorpus, maxSize, store);
	}

	public IndexedCorpus toIndexedCorpus(TXTCorpus textCorpus, int maxSize) {
		OccurrenceStore occurrenceStore = TermSuiteFactory.createMemoryOccurrenceStore(textCorpus.getLang());
		return toIndexedCorpus(textCorpus, maxSize, occurrenceStore);
	}

	public IndexedCorpus toIndexedCorpus(TXTCorpus textCorpus, int maxSize, OccurrenceStore occurrenceStore) {
		String name = asService(textCorpus.getLang()).generateTerminologyName(textCorpus);
		Terminology termino = TermSuiteFactory.createTerminology(textCorpus.getLang(), name);
		
		return  toIndexedCorpus(textCorpus, maxSize, TermSuiteFactory.createIndexedCorpus(termino, occurrenceStore));
	}

	public IndexedCorpus toIndexedCorpus(TextualCorpus textCorpus, int maxSize, IndexedCorpus indexedCorpus) {
		Lang lang = textCorpus.getLang();

		return toIndexedCorpus(
				lang, 
				textCorpus.documents()
					.map(doc -> toCas(textCorpus, doc, textCorpus.readDocumentText(doc))), 
				maxSize, 
				indexedCorpus);
	}
	
	public IndexedCorpus toIndexedCorpus(TextualCorpus textCorpus, int maxSize) {
		return toIndexedCorpus(
				textCorpus.getLang(),
				textCorpus.documents()
					.map(doc -> toCas(textCorpus, doc, textCorpus.readDocumentText(doc))),
				maxSize
			);
	}
	
	public static JCas toCas(Document doc, String documentText, int nbDocuments, long corpusSize) {
		JCas cas;
		try {
			cas = JCasFactory.createJCas();
			cas.setDocumentLanguage(doc.getLang().getCode());
			cas.setDocumentText(documentText);
			SourceDocumentInformation sdi = JCasUtils.initJCasSDI(
				cas, 
				doc.getLang().getCode(), 
				documentText, 
				doc.getUrl(),
				doc.getSize()
				);
			sdi.setCorpusSize(corpusSize);
			sdi.setNbDocuments(nbDocuments);
			return cas;
		} catch (UIMAException e) {
			throw new TermSuiteException(
					"Could not initialize JCas for document " + doc.getUrl(), 
					e);
		}
	}
	public static JCas toCas(TextualCorpus corpus, Document doc, String documentText) {
		return toCas(doc, documentText, corpus.getNbDocuments(), corpus.getTotalSize());
	}
			
	public IndexedCorpus toIndexedCorpus(
			Lang lang, 
			Stream<JCas> blankCasStream, 
			int maxSize) {
		return toIndexedCorpus(
				lang, 
				blankCasStream, 
				maxSize, 
				TermSuiteFactory.createIndexedCorpus(lang));
	}

	public IndexedCorpus toIndexedCorpus(
			Lang lang, 
			Stream<JCas> blankCasStream, 
			int maxSize,
			IndexedCorpus indexedCorpus) {
		PreprocessorService preprocService = asService(lang);
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
		
		Stream<JCas> preparedStream = blankCasStream.map(preprocService::prepare);
		
		Injector injector = Guice.createInjector(new ImporterModule(indexedCorpus, maxSize));
		ImporterService importer = injector.getInstance(ImporterService.class);

		
		preparedStream = configureCASExport(preprocService, preparedStream);
		
		
//		
		logger.info("Starting preprocessing pipeline");
		preparedStream.forEach(importer::importCas);
		
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

	private Stream<JCas> configureCASExport(PreprocessorService preprocService, Stream<JCas> preparedStream) {
		if(xmiPath.isPresent())
			preparedStream = preparedStream.map(cas -> preprocService.toXMICas(
					cas, 
					toCasFile(xmiPath.get(), cas, "xmi")));
		if(tsvPath.isPresent())
			preparedStream = preparedStream.map(cas -> preprocService.toTSVCas(
					cas, 
					toCasFile(tsvPath.get(), cas, "tsv")));
		if(jsonPath.isPresent())
			preparedStream = preparedStream.map(cas -> preprocService.toJSONCas(
					cas, 
					toCasFile(jsonPath.get(), cas, "json")));
		return preparedStream;
	}
	
	private Path toCasFile(Path parentDestination, JCas cas, String newExtension) {

		String txtUri = JCasUtils.getSourceDocumentAnnotation(cas).get()
				.getUri()
				.replaceAll("\\.txt$", "." + newExtension);

		Path resolve = parentDestination.resolve(Paths.get(txtUri).getFileName());

		return resolve;
	}
	
	public void run(TXTCorpus textCorpus) {
		asStream(textCorpus).count();
	}

	/**
	 * 
	 * Returns this preprocessor as a stream of prepared CASes.
	 * 
	 * @param textCorpus
	 * 			The input text corpus
	 * @return
	 * 			The stream of preprocessed CASes
	 */
	public Stream<JCas> asStream(TXTCorpus textCorpus) {
		PreprocessorService asService = asService(textCorpus);
		Stream<JCas> stream = asService.prepare(textCorpus);
		return configureCASExport(asService, stream);
	}
	
	private Optional<Path> preprocessedCorpusCachePath = Optional.empty();
	public Preprocessor setPreprocessedCorpusCache(Path cachedPath) {
		preprocessedCorpusCachePath = Optional.of(cachedPath);
		return this;
	}

	public PreprocessorService asService(Lang lang) {
		return asService(lang, new CorpusMetadata());
	}
	
	private PreprocessorService asService(TXTCorpus textCorpus) {
		try {
			return asService(
					textCorpus.getLang(), 
					corpusService.computeMetadata(textCorpus));
		} catch(IOException e) {
			throw new TermSuiteException(e);
		}
	}
	
	private PreprocessorService asService(Lang lang, CorpusMetadata corpusMetadata) {
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
	
	/**
	 * Activates export of CAS files to xmi.
	 * 
	 * @param xmiPath
	 * 			the path to directory where to write annotation files
	 * @return
	 * 		This preprocessor builder object
	 * 
	 * @see XmiCasSerializer
	 */
	public Preprocessor exportAnnotationsToXMI(Path xmiPath) {
		this.xmiPath = Optional.of(xmiPath);
		return this;
	}

	/**
	 * Activates export of CAS files to tsv annotation format.
	 * 
	 * @param tsvPath
	 * 			the path to directory where to write annotation files
	 * @return
	 * 		This preprocessor builder object
	 * 
	 * @see TSVCasSerializer
	 */
	public Preprocessor exportAnnotationsToTSV(Path tsvPath) {
		this.tsvPath = Optional.of(tsvPath);
		return this;
	}

	
	/**
	 * Activates export of CAS files to json annotation format.
	 * 
	 * @param tsvPath
	 * 			the path to directory where to write annotation files
	 * @return
	 * 		This preprocessor builder object
	 * 
	 * @see JsonCasSerializer
	 */
	public Preprocessor exportAnnotationsToJSON(Path jsonPath) {
		this.jsonPath = Optional.of(jsonPath);
		return this;
	}
}
