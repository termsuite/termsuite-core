package fr.univnantes.termsuite.api;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.framework.PreprocessingPipelineBuilder;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.resources.PostProcessorOptions;
import fr.univnantes.termsuite.uima.readers.TermSuiteJsonCasDeserializer;
import fr.univnantes.termsuite.utils.FileSystemUtils;
import fr.univnantes.termsuite.utils.TermHistory;

/**
 * 
 * A builder and launcher class for execute a terminology extraction
 * pipeline from raw text files or from TermSuite preprocessed files.
 * 
 * @author Damien Cram
 * 
 * @see Preprocessor
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
	 * If present, start the extraction from an already imported term index
	 */
	private Optional<Terminology> termino = Optional.empty();

	
	/*
	 * Custom resources
	 */
	private Optional<String> customResourceDir = Optional.empty();
	
	/*
	 * Contextualizer properties
	 */
	private boolean contextualizerEnabled = false;
	private Optional<ContextualizerOptions> contextualizerOptions = Optional.of(new ContextualizerOptions());

	/*
	 * true if the input is preprocessed, false otherwise.
	 */
	private boolean preprocessed = false;
	
	
//	private boolean mergeGraphicalVariants = true;

	
	
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
	 * 
	 */
	private boolean scoringEnabled = true;
	
	
	/*
	 * 
	 */
	private boolean variationDetectionEnabled = true;
	
	
	/*
	 * Filter properties
	 */
	private Optional<TerminoFilterOptions> postFilterConfig = Optional.empty();
	private Optional<TerminoFilterOptions> preFilterConfig  = Optional.empty();

	
	/*
	 * 
	 */
	private Optional<PostProcessorOptions> scorerConfig = Optional.empty();
	
	
	private boolean semanticAlignerEnabled = false;
	
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
	
	public static TerminoExtractor fromTerminology(Terminology termino) {
		TerminoExtractor extractor = new TerminoExtractor();
		extractor.termino = Optional.of(termino);
		extractor.preprocessed = true;
		extractor.lang = termino.getLang();
		return extractor;
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

	public TerminoExtractor disableVariationDetection() {
		this.variationDetectionEnabled = false;
		return this;
	}

	
	public TerminoExtractor setSemanticAlignerEnabled(boolean semanticAlignerEnabled) {
		this.semanticAlignerEnabled = semanticAlignerEnabled;
		if(semanticAlignerEnabled)
			this.contextualizerEnabled = true;
		return this;
	}
	public TerminoExtractor enableSemanticAlignment() {
		return setSemanticAlignerEnabled(true);
	}

	public TerminoExtractor configureScoring(PostProcessorOptions scorerConfig) {
		this.scorerConfig = Optional.of(scorerConfig);
		return this;
	}

	public TerminoExtractor disableScoring() {
		this.scoringEnabled = false;
		return this;
	}

//	public TerminoExtractor disableGraphVariantMerging() {
//		this.mergeGraphicalVariants = false;
//		return this;
//	}

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
	
	public TerminoExtractor useContextualizer(ContextualizerOptions options) {
		this.contextualizerEnabled = true;
		this.contextualizerOptions = Optional.of(options);
		return this;
	}
	
	/**
	 * Filters the {@link Terminology} before the term variant detection phase.
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
	 * @see  #postFilter(TerminoFilterOptions)
	 * 
	 */
	public TerminoExtractor preFilter(TerminoFilterOptions filterConfig) {
		this.preFilterConfig = Optional.of(filterConfig);
		return this;
	}

	
	/**
	 * 
	 * Filters the {@link Terminology} dynamically during the term spotting phase (RegexSpotter)
	 * of terminology extraction by cleaning by frequency whenever the number of terms in-memory 
	 * exceeds a max number of terms allowed.
	 * 
	 * 
	 * @param maxTerminoSize
	 * 			the maximum number of {@link Term} instances allowed to be kept in memory 
	 * 			during the terminology extraction process.
	 * @return
	 * 		this {@link TerminoExtractor} launcher class
	 * 
	 * @see PreprocessingPipelineBuilder#aeMaxSizeThresholdCleaner(TermProperty, int)
	 * 
	 */
	public TerminoExtractor dynamicMaxSizeFilter(int maxTerminoSize) {
		this.maxSizeFilter = Optional.of(maxTerminoSize);
		return this;
	}

	
	
	/**
	 * Filters the {@link Terminology} at the end of the pipeline,
	 * i.e. after the term variant detection phase.
	 * 
	 * This filtering is loss-less when configured with {@link TerminoFilterOptions#keepVariants(true)}.
	 * 
	 * @param filterConfig
	 * 			The filtering configuration
	 * @return
	 * 			this {@link TerminoExtractor} launcher class
	 * 
	 * @see  #preFilter(TerminoFilterOptions)
	 * 
	 */
	public TerminoExtractor postFilter(TerminoFilterOptions filterConfig) {
		this.postFilterConfig = Optional.of(filterConfig);
		return this;
	}	
	
	private Optional<TermHistory> history = Optional.empty();
	
	public TerminoExtractor setWatcher(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	
	private Optional<String> occStorePath = Optional.empty();
	
	public TerminoExtractor setPersistentOccurrenceStore(String occStorePath) {
		this.occStorePath = Optional.ofNullable(occStorePath);
		return this;
	}

	private boolean skipOccurrenceIndexing = false;
	public TerminoExtractor skipOccurrenceIndexing() {
		return setSkipOccurrenceIndexing(true);
	}

	public TerminoExtractor setSkipOccurrenceIndexing(boolean b) {
		this.skipOccurrenceIndexing = b;
		return this;
	}

}
