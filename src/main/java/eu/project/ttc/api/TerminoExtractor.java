package eu.project.ttc.api;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.history.TermHistory;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.readers.TermSuiteJsonCasDeserializer;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.tools.api.internal.FileSystemUtils;
import eu.project.ttc.tools.api.internal.PipelineUtils;
import eu.project.ttc.utils.JCasUtils;

/**
 * 
 * A builder and launcher class for execute a terminology extraction
 * pipeline from raw text files or from TermSuite preprocessed files.
 * 
 * @author Damien Cram
 * 
 * @see TermSuitePreprocessor
 *
 */
public class TerminoExtractor {
	
	public static enum ContextualizerMode {ON_ALL_TERMS, ON_SWT_TERMS}
	
	/*
	 * TreeTagger home
	 */
	private String treeTaggerHome;
	
	/*
	 * Pipeline language
	 */
	private Lang lang = null;
	
	/*
	 * Custom resources
	 */
	private Optional<String> customResourceDir = Optional.empty();
	
	/*
	 * Contextualizer properties
	 */
	private boolean useContextualizer = false;
	private int contextualizerScope = 3;
	private ContextualizerMode contextualizerMode = ContextualizerMode.ON_SWT_TERMS;

	/*
	 * true if the input is preprocessed, false otherwise.
	 */
	private boolean preprocessed = false;
	
	/*
	 * The maximum number of terms allowed in memory. empty 
	 * if maxSizeFiltering is deactivated.
	 */
	private Optional<Integer> maxSizeFilter = Optional.empty();
	
	
	/*
	 * The total number of documents
	 */
	private long nbDocuments = -1;
	
	/*
	 * The input streams.
	 */
	// document stream when the input is not preprocessed
	private Stream<Document> documentStream;
	
	// jcas stream when the input is preprocessed
	private Stream<JCas> preprocessedCasStream;
	
	
	/*
	 * Filter properties
	 */
	private Optional<TerminoFilterConfig> postFilterConfig = Optional.empty();
	private Optional<TerminoFilterConfig> preFilterConfig  = Optional.empty();
	
	public static TerminoExtractor fromTextString(Lang lang, String text) {
		return fromSingleDocument(lang, new Document(lang, "file://inline.text", text));
	}

	public static TerminoExtractor fromSingleDocument(Lang lang, Document document) {
		return fromDocumentCollection(lang, Lists.newArrayList(document));
	}

	public static TerminoExtractor fromPreprocessedJsonFiles(Lang lang, String directory) {
		return fromPreprocessedJsonFiles(lang, directory, Charset.defaultCharset().name());
	}

	public static TerminoExtractor fromPreprocessedJsonFiles(Lang lang, String directory, String encoding) {
		Function<Path, JCas> mapper = path -> {
			try {
				JCas cas = JCasFactory.createJCas();
				cas.setDocumentLanguage(lang.getCode());
				TermSuiteJsonCasDeserializer.deserialize(
						new FileInputStream(path.toFile()), 
						cas.getCas(),
						encoding);
				return cas;
			} catch (Exception e) {
				throw new TermSuiteException("Unable to parse cas file " + path, e);
			}
		};

		return fromPreprocessedDocumentStream(
				lang, 
				FileSystemUtils.pathWalker(directory, "**/*.json", mapper),
				FileSystemUtils.pathDocumentCount(directory, "**/*.json")
			);
		
	}

	/**
	 * WARNING : encoding of XMI file must be UTF-8.
	 */
	public static TerminoExtractor fromPreprocessedXmiFiles(Lang lang, String directory) {
		Function<Path, JCas> mapper = path -> {
			try {
				JCas cas = JCasFactory.createJCas();
				cas.setDocumentLanguage(lang.getCode());

				XmiCasDeserializer.deserialize(new FileInputStream(path.toFile()), cas.getCas());
				return cas;
			} catch (Exception e) {
				throw new TermSuiteException("Unable to parse cas file " + path, e);
			}
		};

		return fromPreprocessedDocumentStream(
				lang, 
				FileSystemUtils.pathWalker(directory, "**/*.xmi", mapper),
				FileSystemUtils.pathDocumentCount(directory, "**/*.xmi")
			);

	}

	public static TerminoExtractor fromPreprocessedDocumentStream(Lang lang, Stream<JCas> casStream, long streamSize) {
		TerminoExtractor extractor = new TerminoExtractor();
		extractor.preprocessedCasStream = casStream;
		extractor.nbDocuments = streamSize;
		extractor.lang = lang;
		extractor.preprocessed = true;
		return extractor;
	}

	public static TerminoExtractor fromDocumentStream(Lang lang, Stream<Document> documentStream, long streamSize) {
		TerminoExtractor extractor = new TerminoExtractor();
		extractor.documentStream = documentStream;
		extractor.lang = lang;
		extractor.nbDocuments = streamSize;
		return extractor;
	}
	
	public static TerminoExtractor fromDocumentCollection(Lang lang, Collection<Document> documents) {
		return fromDocumentStream(lang, documents.stream(), documents.size());
	}

	public static TerminoExtractor fromTxtCorpus(Lang lang, String directory, String pattern) {
		return fromTxtCorpus(lang, directory, pattern, Charset.defaultCharset().name());
	}

	public static TerminoExtractor fromTxtCorpus(Lang lang, String directory, String pattern, String encoding) {
		return fromDocumentStream(
				lang, 
				FileSystemUtils.pathWalker(
						directory, 
						pattern, 
						FileSystemUtils.pathToDocumentMapper(lang, encoding)),
				FileSystemUtils.pathDocumentCount(directory, pattern));
	}

	
	public static TerminoExtractor fromSinglePreprocessedDocument(Lang lang, JCas cas) {
		return fromPreprocessedDocumentStream(
				lang, 
				Lists.newArrayList(cas).stream(),
				1);
	}

	
	public TerminoExtractor setTreeTaggerHome(String treeTaggerHome) {
		this.treeTaggerHome = treeTaggerHome;
		return this;
	}

	public TerminoExtractor usingCustomResources(String resourceDir) {
		Preconditions.checkArgument(new File(resourceDir).exists(), "Directory %s does not exist", resourceDir);
		Preconditions.checkArgument(new File(resourceDir).isDirectory(), "Not a directory: %s", resourceDir);
		this.customResourceDir = Optional.empty();
		return this;
	}
	
	public TerminoExtractor useContextualizer(int scope, ContextualizerMode contextualizerMode) {
		this.useContextualizer = true;
		this.contextualizerScope = 3;
		this.contextualizerMode = contextualizerMode;
		return this;
	}
	
	/**
	 * Filters the {@link TermIndex} before the term variant detection phase.
	 * 
	 * This early-stage filtering will result in missing several low-frequency variations
	 * during the term variation detection but is often necessary 
	 * when detecting variant takes too long.
	 * 
	 * @param filterConfig
	 * 			The filtering configuration
	 * @return
	 * 			this {@link TerminoExtractor} launcher class
	 * 
	 * @see  #postFilter(TerminoFilterConfig)
	 * 
	 */
	public TerminoExtractor preFilter(TerminoFilterConfig filterConfig) {
		this.preFilterConfig = Optional.of(filterConfig);
		return this;
	}

	
	/**
	 * 
	 * Filters the {@link TermIndex} dynamically during the term spotting phase (RegexSpotter)
	 * of terminology extraction by cleaning by frequency whenever the number of terms in-memory 
	 * exceeds a max number of terms allowed.
	 * 
	 * 
	 * @param maxTermIndexSize
	 * 			the maximum number of {@link Term} instances allowed to be kept in memory 
	 * 			during the terminology extraction process.
	 * @return
	 * 		this {@link TerminoExtractor} launcher class
	 * 
	 * @see TermSuitePipeline#aeMaxSizeThresholdCleaner(TermProperty, int)
	 * 
	 */
	public TerminoExtractor dynamicMaxSizeFilter(int maxTermIndexSize) {
		this.maxSizeFilter = Optional.of(maxTermIndexSize);
		return this;
	}

	
	
	/**
	 * Filters the {@link TermIndex} at the end of the pipeline,
	 * i.e. after the term variant detection phase.
	 * 
	 * This filtering is loss-less when configured with {@link TerminoFilterConfig#keepVariants(true)}.
	 * 
	 * @param filterConfig
	 * 			The filtering configuration
	 * @return
	 * 			this {@link TerminoExtractor} launcher class
	 * 
	 * @see  #preFilter(TerminoFilterConfig)
	 * 
	 */
	public TerminoExtractor postFilter(TerminoFilterConfig filterConfig) {
		this.postFilterConfig = Optional.of(filterConfig);
		return this;
	}	
	
	public TermIndex execute() {
		Preconditions.checkNotNull(this.lang, "Language cannot be null");
		
		TermSuitePipeline pipeline = TermSuitePipeline
				.create(lang.getCode());
		
		if(history.isPresent())
			pipeline.setHistory(history.get());
		
		if(customResourceDir.isPresent())
			pipeline.setResourceDir(this.customResourceDir.get());
		
		if(!preprocessed) {
			
			pipeline.aeWordTokenizer()
					.setTreeTaggerHome(this.treeTaggerHome)
					.aeTreeTagger()
					.aeUrlFilter()
					.aeStemmer()
					.aeRegexSpotter();
		} else {
			pipeline.aeUrlFilter()
					.aeTermOccAnnotationImporter();
		}
		
		
		if(preFilterConfig.isPresent()) 
			PipelineUtils.filter(pipeline, preFilterConfig.get());
			
		if(useContextualizer)
			pipeline.aeContextualizer(
					contextualizerScope, 
					contextualizerMode == ContextualizerMode.ON_ALL_TERMS ? true : false);
		
		if(nbDocuments != -1)
			pipeline.aeDocumentLogger(this.nbDocuments);
		
		if(maxSizeFilter.isPresent());
			pipeline.aeMaxSizeThresholdCleaner(TermProperty.FREQUENCY, maxSizeFilter.get());
		
		pipeline
				.aeSpecificityComputer()
				.aeCompostSplitter()
				.aePrefixSplitter()
				.aeSuffixDerivationDetector()
				.aeSyntacticVariantGatherer()
				.aeGraphicalVariantGatherer()
				.aeExtensionDetector()
				.aeScorer()
				.aeRanker(TermProperty.SPECIFICITY, true);

		if(postFilterConfig.isPresent()) 
			PipelineUtils.filter(pipeline, postFilterConfig.get());
		
	    ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();
	    
		try {
			// Create AAE
			AnalysisEngineDescription aaeDesc = createEngineDescription(pipeline.createDescription());

			// Instantiate AAE
			final AnalysisEngine aae = UIMAFramework.produceAnalysisEngine(aaeDesc, resMgr, null);
			
			
			
			if(preprocessed) {
				preprocessedCasStream.forEach(cas -> {
					try {
						aae.process(cas);
					} catch (UIMAException e) {
						throw new TermSuiteException(e);
					}				
				});
			} else {
				documentStream.forEach(document -> {
					JCas cas;
					try {
						cas = JCasFactory.createJCas();
						cas.setDocumentLanguage(document.getLang().getCode());
						cas.setDocumentText(document.getText());
						JCasUtils.initJCasSDI(
								cas, 
								document.getLang().getCode(), 
								document.getText(), 
								document.getUrl());
						aae.process(cas);
					} catch (UIMAException e) {
						throw new TermSuiteException(e);
					}
				});
			}
			
			aae.collectionProcessComplete();
		} catch (ResourceInitializationException | AnalysisEngineProcessException e1) {
			throw new TermSuiteException(e1);
		}

		return pipeline.getTermIndex();
	}

	private Optional<TermHistory> history = Optional.empty();
	
	public TerminoExtractor setWatcher(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
}
