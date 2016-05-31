/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.tools.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.utils.FileUtils;
import eu.project.ttc.utils.TermUtils;

/**
 * Command line interface for the Terminology extraction (Spotter+Indexer) engines.
 * 
 * @author Damien Cram
 */
public class TermSuiteTerminoCLI {
	
	private enum CollectionMode {ISTEX_API, FILESYSTEM, INLINE_TEXT}
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteTerminoCLI.class);

	/** Short usage description of the CLI */
	private static final String USAGE = "java [-DconfigFile=<file>] -Xms1g -Xmx2g -cp termsuite-core-x.x.jar eu.project.ttc.tools.cli.TermSuiteTerminoCLI";

	/// Parameter names
	/** Name of the example limit parament */
	private static final String TEXT = "text";


	/** Name of the watch parameter */
	private static final String WATCH = "watch";

	/** Name of the corpus parameter */
	private static final String PATH_TO_CORPUS = "corpus-home";

	/** Name of the resource path parameter */
	private static final String PATH_TO_RESOURCE_PACK = "resource-pack";

	
	/** Name of the corpus format parameter */
	private static final String CORPUS_FORMAT = "corpus-format";
	
	/** Name of the parameter that must be set to the tt dir */
	public static final String P_TAGGER_HOME_DIRECTORY = "tagger-home";
	
	/** Name of the parameter that must be set to disable graphical variants */
	private static final String GRAPHICAL_SIMILARITY = "graphical-similarity-th";
    
	/** Name of the paramter that shows tree tagger tags**/
	private static final String SHOW_TAGGER_TAGS = "tags";

	/** Compost configuration parameters **/
	private static final String COMPOST_COEFF = "compost-coeff";
	private static final String COMPOST_MIN_COMPONENT_SIZE = "compost-min-component-size";
	private static final String COMPOST_MAX_COMPONENT_NUM = "compost-max-component-num";
	private static final String COMPOST_SIMILARITY_THRESHOLD = "compost-similarity-threshold";
	private static final String COMPOST_SCORE_THRESHOLD = "compost-score-threshold";

	/** deactivate the occurrences saving in memory while indexing **/
	private static final String NO_OCCURRENCE = "no-occurrence";

	/** MongoDB parameters **/
	private static final String MONGODB_STORE = "mongodb-store";
	private static final String MONGODB_SOFT_LINK = "json-mongodb-soft-link";

	
	/** ISTEX API Parameter **/
	private static final String ISTEX_API_URL = "istex-api";
	private static final String ISTEX_ID_FILE = "istex-id-file";
	private static final String ISTEX_ID = "istex-id";
	
	
	/*
	 * Collection mode
	 */
	private static CollectionMode collectionMode = CollectionMode.FILESYSTEM;
	
	
	/*
	 * The mongo db options
	 */
	private static Optional<String> mongoStoreDBURL = Optional.absent();
	private static boolean mongoStoreSoftLinked = false;

	
	/** Mate tagger parameter **/
	private static final String MATE = "mate";

	/*
	 * With Mate
	 */
	private static enum Tagger{Mate, TreeTagger};
	
	/*
	 * Logging arguments
	 */
	private static final String DEBUG = "debug";
	private static final String TRACE = "trace";
	private static final String NO_LOGGING = "no-logging";
	
	/*
	 * Contextualizer
	 */
	private static final String CONTEXTUALIZE = "contextualize";
	private static final String CONTEXTUALIZE_ALL_TERMS = "contextualize-all-terms";
	private static final String CONTEXT_SCOPE = "context-scope";
	private static final String ALLOW_MWT_IN_CONTEXTS = "allow-mwts-in-contexts";

	

	
	/*
	 * Cleaning arguments
	 */
	private static final String CLEAN_THRESHOLD = "filter-th";
	private static final String CLEAN_TOP_N = "filter-top-n";
	private static final String CLEAN_PROPERTY = "filter-property";
	private static final String CLEAN_FILTER_VARIANTS = "filter-variants";
	
	/*
	 * Max size filtering
	 */
	private static final String PERIODIC_FILTER_PROPERTY = "periodic-filter-property";
	private static final String PERIODIC_FILTER_MAX_SIZE = "periodic-filter-max-size";
	
	// the tsv file path argument
	private static final String TSV = "tsv";
	private static final String TSV_PROPERTIES = "tsv-properties";
	private static final String TSV_VARIANT_SCORES = "tsv-show-scores";


	// the json file path argument
	private static final String JSON = "json";

	// the tbx file path argument
	private static final String TBX = "tbx";

	// the jsonCAS file path argument
	private static final String JSCASFILE = "jsonCasFile";

	
	
	// tagger argument
	private static Tagger tagger = Tagger.TreeTagger;

    private static String resourcePack = null;
    private static String corpusPath = null;
    private static Lang language = null;
    private static String encoding = "UTF-8";
//    private static String pipelineCRInputDirectory = null;
    private static String taggerHome = "";
    private static String inlineText = null;
	private static TermSuiteCollection corpusType = TermSuiteCollection.TXT;
	private static float graphicalSimilarityThreshold = 0.9f;

	/*
	 * Istex parameters
	 */
	private static Optional<String> istexAPIUrl = Optional.absent();
	private static Optional<List<String>> istexIds = Optional.absent();
	
	/*
	 * contetxualizer
	 */
	private static boolean contextualize = false;
	private static boolean contextualizeAllTerms = false;
	private static boolean allowMWTInContexts = false;
	private static int contextScope = 3;


	/*
	 * Cleaning parameters
	 */
	private static Optional<Float> cleaningThreshold = Optional.of(2f);
	private static Optional<Integer> cleaningTopN = Optional.absent();
	private static Optional<TermProperty> cleaningProperty = Optional.of(TermProperty.WR_LOG);
	private static boolean keepVariantsWhileCleaning = true;
	
	/*
	 * Max size periodic filtering
	 */
	private static Optional<TermProperty> periodicFilteringProperty = Optional.absent();
	private static int maxSizeFilteringMaxSize = 20000;
	
	
	/*
	 * Spotter params
	 */
	private static boolean spotWithOccurrences = true;
	
	
	
	/*
	 * Export params
	 */
	private static Optional<String> tsvFile = Optional.absent();
	private static Optional<TermProperty[]> tsvProperties = Optional.absent();
	private static boolean tsvShowVariantScores = false;
	
	private static Optional<String> jsonFile = Optional.absent();
	private static Optional<String> tbxFile = Optional.absent();

	private static Optional<String> jsonCasFile = Optional.absent();
	

	/*
	 *  compost params
	 */
	private static Optional<Float> compostAlpha = Optional.absent();
	private static Optional<Float> compostBeta = Optional.absent();
	private static Optional<Float> compostGamma = Optional.absent();
	private static Optional<Float> compostDelta = Optional.absent();
	private static Optional<Integer> compostMinComponentSize = Optional.absent();
	private static Optional<Integer> compostMaxComponentNum = Optional.absent();
	private static Optional<Float> compostSimilarityThreshold = Optional.of(1f);
	private static Optional<Float> compostScoreThreshold = Optional.absent();

	/*
	 * Ouput and display params
	 */
	private static Optional<Pattern> watch = Optional.absent();

	/**
	 * Application entry point
	 * 
	 * @param args
	 *            Command line arguments
     * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		File logDir = new File("logs");
		if(!logDir.exists()) 
			logDir.mkdir();
		String logPath = Paths.get("logs", "termsuite-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) +".log").toAbsolutePath().toString();
		TermSuiteCLIUtils.logToFile(logPath);
		Stopwatch sw = Stopwatch.createStarted();
		LOGGER.info("Logging to {}", logPath);
		try {
			
			// usage
			// java -DconfigFile=myPropertiesFileName -Xms1g  -Xmx2g -cp ttc-term-suite-1.3.jar eu.project.ttc.tools.cli.TermSuiteSpotterCLI
			// if the option -DconfigFile is missing preferencesFileName is set to TermSuiteCLIUtils.USER_HOME+PREFERENCES_FILE_NAME
			// create the command line parser
			PosixParser parser = new PosixParser();

			// create the Options
			Options options = declareOptions();
			
			try {
				// Parse and set CL options
				CommandLine line = parser.parse(options, args, false);
				readArguments(line);
				if(line.hasOption(NO_LOGGING))
					TermSuiteCLIUtils.disableLogging();
				else if(line.hasOption(DEBUG))
					TermSuiteCLIUtils.setGlobalLogLevel("debug");
				else if(line.hasOption(TRACE))
					TermSuiteCLIUtils.setGlobalLogLevel("trace");
				else
					TermSuiteCLIUtils.setGlobalLogLevel("info");

				TermSuiteCLIUtils.logCommandLineOptions(line);
				
				TermSuitePipeline pipeline = TermSuitePipeline.create(language.getCode());
				
				switch(collectionMode) {
				case INLINE_TEXT:
					pipeline.setInlineString(inlineText);
					break;
				case FILESYSTEM:
					pipeline.setCollection(corpusType, corpusPath, encoding);
					break;
				case ISTEX_API:
					pipeline.setIstexCollection(istexAPIUrl.get(), istexIds.get());
					break;
				}

				// resource
				pipeline.setResourcePath(resourcePack);
				
				// mongodb
				if(mongoStoreDBURL.isPresent())
					pipeline.setMongoDBOccurrenceStore(mongoStoreDBURL.get());
				
				// tokenizer
				pipeline.aeWordTokenizer();
				
				// tagger
				if(tagger == Tagger.TreeTagger) 
					pipeline.setTreeTaggerHome(taggerHome)
						.aeTreeTagger();
				else if(tagger == Tagger.Mate) 
					pipeline.setMateModelPath(taggerHome)
						.aeMateTaggerLemmatizer();
				

				// Filter urlsFilter
				pipeline.aeUrlFilter();

				// stemmer
				pipeline.aeStemmer();
				
				// regex spotter
				pipeline.setSpotWithOccurrences(spotWithOccurrences);
				pipeline.aeRegexSpotter();

				//export Json CAS spotter
				if(jsonCasFile.isPresent())
					pipeline.haeJsonCasExporter(jsonCasFile.get());
				// filter stop words
				pipeline.aeStopWordsFilter();

				// specificity computer
				pipeline.aeSpecificityComputer();

					
				// compost (morphology)
				if(compostAlpha.isPresent()) 
					pipeline.setCompostCoeffs(compostAlpha.get(), compostBeta.get(), compostGamma.get(), compostDelta.get());
				if(compostMinComponentSize.isPresent()) 
					pipeline.setCompostMinComponentSize(compostMinComponentSize.get());
				if(compostMaxComponentNum.isPresent()) 
					pipeline.setCompostMaxComponentNum(compostMaxComponentNum.get());
				if(compostScoreThreshold.isPresent()) 
					pipeline.setCompostScoreThreshold(compostScoreThreshold.get());
				if(compostSimilarityThreshold.isPresent()) 
					pipeline.setCompostSegmentSimilarityThreshold(compostSimilarityThreshold.get());
				pipeline.aeCompostSplitter();
				
				// syntactic variant gathering
				pipeline.aeSyntacticVariantGatherer();

				// graphical variant gathering
				pipeline.setGraphicalVariantSimilarityThreshold(graphicalSimilarityThreshold);
				pipeline.aeGraphicalVariantGatherer();
				
				// filtering
				if(cleaningThreshold.isPresent()) {
					pipeline.setKeepVariantsWhileCleaning(keepVariantsWhileCleaning);
					pipeline.aeThresholdCleaner(
							cleaningProperty.get(), cleaningThreshold.get());
				} else if(cleaningTopN.isPresent()) {
					pipeline.setKeepVariantsWhileCleaning(keepVariantsWhileCleaning);
					pipeline.aeTopNCleaner(cleaningProperty.get(), cleaningTopN.get());
				}

				if(periodicFilteringProperty.isPresent())
					pipeline.aeMaxSizeThresholdCleaner(periodicFilteringProperty.get(), maxSizeFilteringMaxSize);
				
				// contextualize
				if(contextualize) {
					pipeline
						.setContextualizeCoTermsType(allowMWTInContexts ? OccurrenceType.ALL : OccurrenceType.SINGLE_WORD)
						.aeContextualizer(contextScope, contextualizeAllTerms);
					
				}
				
				pipeline.aeExtensionDetector()
					.aeScorer()
					.aeRanker(TermProperty.SPECIFICITY, true);
				
				// stats
				pipeline.haeCasStatCounter("at end of pipeline");

				
				
				// Export
				if(tsvFile.isPresent())  {
					if(tsvProperties.isPresent()) {
						pipeline.setTsvExportProperties(tsvProperties.get());
						pipeline.setTsvShowScores(tsvShowVariantScores);
					} else 
						pipeline.setTsvExportProperties(
							TermProperty.PILOT,
							TermProperty.FREQUENCY
						);
					pipeline.haeTsvExporter(tsvFile.get());

				}
				if(tbxFile.isPresent()) 
					pipeline.haeTbxExporter(tbxFile.get());
				if(jsonFile.isPresent())  {					
					pipeline.setExportJsonWithContext(contextualize);
					pipeline.setExportJsonWithOccurrences(true);
					if(mongoStoreSoftLinked)
						pipeline.linkMongoStore();
					pipeline.haeJsonExporter(jsonFile.get());
				}

				// run the pipeline
				final String termIndexName = "ScriptTermIndex_" + System.currentTimeMillis();
	            if(collectionMode == CollectionMode.INLINE_TEXT) {
	            	LOGGER.info("Running TermSuite pipeline (inline mode)");
	            	JCas cas = JCasFactory.createJCas();
	            	cas.setDocumentText(inlineText);
	            	cas.setDocumentLanguage(language.getCode());
	            	pipeline.run(cas);
	            	System.err.flush();
	            	System.out.println("Term index: ");
					TermIndex index = MemoryTermIndexManager.getInstance().getIndex(termIndexName);
	            	TermUtils.showIndex(index, System.out, watch);
	            } else {
	            	LOGGER.info("Running TermSuite pipeline in corpus mode");
	            	pipeline.run();
	            	if(watch.isPresent()) 
	            		TermUtils.showIndex(
	            				MemoryTermIndexManager.getInstance().getIndex(termIndexName), 
	            				new PrintStream(System.err, true, "UTF-8"), 
	            				watch);
	            }
	            LOGGER.info("Script executed in " + sw.toString());
				
			} catch (ParseException e) {
				TermSuiteCLIUtils.printUsage(e, USAGE, options); 
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
            LOGGER.error(e.getMessage());
		}
	}

	private static Options declareOptions() {
		Options options = new Options();
		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				ISTEX_API_URL, 
				true, 
				"URL to the istex API", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				ISTEX_ID_FILE, 
				true, 
				"File containing the list of Istex document ids (one per line).", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				ISTEX_ID, 
				true, 
				"List of comma-separated Istex docuement ids", 
				false));

		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				NO_OCCURRENCE, 
				false, 
				"Deactivate the occurrence store in memory (recommended for big corpus).", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				PERIODIC_FILTER_PROPERTY, 
				true, 
				"Activate a periodic cleaning of the on-going terminology by a given property.", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				PERIODIC_FILTER_MAX_SIZE, 
				true, 
				"The maximum allowed size of the on-going terminology in memory.", 
				false));

		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				MATE, 
				false, 
				"Use Mate tagger instead of TreeTagger.", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				TEXT, 
				true, 
				"The text to analyze", 
				false));
		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				COMPOST_MAX_COMPONENT_NUM, 
				true, 
				"The maximum number of components that a compound can have", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				COMPOST_MIN_COMPONENT_SIZE, 
				true, 
				"The minimum size allowed in a component", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				COMPOST_SCORE_THRESHOLD, 
				true, 
				"The segmentation score threshold of COMPOST algo.", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				COMPOST_SIMILARITY_THRESHOLD, 
				true, 
				"The segment similarity threshold above which an existing string in COMPOST index is considered as recognized.", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				COMPOST_COEFF, 
				true, 
				"COMPOST alpha, beta, gamma and delta parameters, separated with a hyphen \"-\". Sum must be 1", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				NO_LOGGING, 
				false, 
				"Disable logging", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				DEBUG, 
				false, 
				"fine-grained logging", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				TRACE, 
				false, 
				"very fine grained logging", 
				false));
		

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				CONTEXTUALIZE, 
				false, 
				"Enable the contextualizer. Compute a context vector for each SWT term.", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				CONTEXTUALIZE_ALL_TERMS, 
				false, 
				"Compute a context vector for MWTs too.", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				ALLOW_MWT_IN_CONTEXTS, 
				false, 
				"Allow to set MWTs as cooccurrences in context vectors.", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				CONTEXT_SCOPE, 
				true, 
				"The window size for term contexts capture", 
				false));

		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				CORPUS_FORMAT, 
				true, 
				"The file format in the input corpus. txt and tei supported", 
				false));
		
		options.addOption(TermSuiteCLIUtils.createOption(
				"c", 
				PATH_TO_CORPUS, 
				true, 
				"Path to the corpus", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				"r", 
				PATH_TO_RESOURCE_PACK, 
				true, 
				"Path to the TermSuite resource pack", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				"l", 
				TermSuiteCLIUtils.P_LANGUAGE, 
				true, 
				"language of the input files: fr/en/etc.", 
				true));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				TermSuiteCLIUtils.P_ENCODING, 
				true, 
				"encoding of the input files", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				"t",
				P_TAGGER_HOME_DIRECTORY,
				true, 
				"TreeTagger home directory or Mate model directory", 
				true));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				GRAPHICAL_SIMILARITY, 
				false, 
				"The similarity threshold (a value between 0 and 1, 0.9 advised) for graphical variant gathering.", 
				false));
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				SHOW_TAGGER_TAGS, 
				false, 
				"Show tree tagger tags", 
				false));
		
		options.addOption(
				null, 
				WATCH, 
				true,
				"Show infos about terms matching this string");

		options.addOption(
				null, 
				CLEAN_PROPERTY, 
				true,
				"The name of the term property used for cleaning filtering  the term index");
		options.addOption(
				null, 
				CLEAN_FILTER_VARIANTS, 
				false,
				"Also filter variants with terms.");
		
		options.addOption(
				null, 
				CLEAN_THRESHOLD, 
				true,
				"The filtering threshold");

		options.addOption(
				null, 
				CLEAN_TOP_N, 
				true,
				"The number of terms to keep after filtering");

		options.addOption(
				null, 
				TSV, 
				true,
				"The tsv file path where to export the term index");
		options.addOption(
				null, 
				TSV_PROPERTIES, 
				true,
				"comma-separated list of term properties to export as a column in TSV file");
		options.addOption(
				null, 
				TSV_VARIANT_SCORES, 
				false,
				"shows variant scores next to the \"V\" label");

		options.addOption(
				null, 
				TBX, 
				true,
				"The tbx file path where to export the term index");

		options.addOption(
				null, 
				JSON, 
				true,
				"The json file path where to export the term index");

		options.addOption(
				null,
				JSCASFILE,
				true,
				"The directory path where to export the TreeTagger token of each files give in entry of TermSuite in " +
						"Json Format");

		options.addOption(
				null,
				MONGODB_STORE,
				true,
				"The mongo db url of the database where to store the occurrences");

		options.addOption(
				null, 
				MONGODB_SOFT_LINK, 
				false,
				"shows variant scores next to the \"V\" label");


		return options;
	}

	public static void readArguments(CommandLine line) throws IOException {
		

		/*
		 * Collection Reader arguments
		 */
		if(line.hasOption(ISTEX_API_URL))  {
			collectionMode = CollectionMode.ISTEX_API;
			istexAPIUrl = Optional.of(line.getOptionValue(ISTEX_API_URL));
			List<String> ids = Lists.newLinkedList();
			if(line.hasOption(ISTEX_ID_FILE))  {
				ids = FileUtils.getUncommentedLines(
						new File(line.getOptionValue(ISTEX_ID_FILE)), 
						Charset.forName("UTF-8"));
			} else if(line.hasOption(ISTEX_ID)) {
				ids = Splitter.on(",").splitToList(line.getOptionValue(ISTEX_ID));
			} else
				TermSuiteCLIUtils.exitWithErrorMessage("On argument of --" 
						+ ISTEX_ID_FILE + ", --" 
						+ ISTEX_ID + "  must be set.");
			istexIds = Optional.of(ids);
		} else if(line.hasOption(TEXT)) {
			inlineText = line.getOptionValue(TEXT);
			if(inlineText == null)
				inlineText = TermSuiteCLIUtils.readIn(encoding);
			collectionMode = CollectionMode.INLINE_TEXT;
		} else if(line.hasOption(PATH_TO_CORPUS)) {
			corpusPath = line.getOptionValue(PATH_TO_CORPUS);
			collectionMode = CollectionMode.FILESYSTEM;
		}
		else
			TermSuiteCLIUtils.exitWithErrorMessage("On argument of --" 
					+ TEXT + ", --" 
					+ PATH_TO_CORPUS + ", --" 
					+ ISTEX_API_URL + "  must be set.");

		resourcePack = line.getOptionValue(PATH_TO_RESOURCE_PACK);
		
		if(line.hasOption(NO_OCCURRENCE))
			spotWithOccurrences = false;
		
		language = Lang.forName(line.getOptionValue(TermSuiteCLIUtils.P_LANGUAGE));
		
		encoding = line.getOptionValue(TermSuiteCLIUtils.P_ENCODING, "UTF-8");
		
		taggerHome = line.getOptionValue(P_TAGGER_HOME_DIRECTORY);
			
		if(line.hasOption(CORPUS_FORMAT)) {
			if(line.getOptionValue(CORPUS_FORMAT).equals(TermSuiteCollection.TEI.name())) {
				corpusType = TermSuiteCollection.TEI;
			} else if(line.getOptionValue(CORPUS_FORMAT).equals(TermSuiteCollection.TXT.name())) {
				corpusType = TermSuiteCollection.TXT;
			} else
				TermSuiteCLIUtils.exitWithErrorMessage("Unknown corpus format: " + line.getOptionValue(CORPUS_FORMAT) + ". Supported formats: " + Joiner.on(',').join(TermSuiteCollection.values()));
		}
//		pipelineCRInputDirectory = TermSuiteCLIUtils.getCorpusLanguagePath(corpusPath, language, corpusType.name().toLowerCase());
		

		if(line.hasOption(GRAPHICAL_SIMILARITY))
			graphicalSimilarityThreshold = Float.parseFloat(line.getOptionValue(GRAPHICAL_SIMILARITY));
			
		if(line.hasOption(COMPOST_MIN_COMPONENT_SIZE))
			compostMinComponentSize = Optional.of(Integer.parseInt(line.getOptionValue(COMPOST_MIN_COMPONENT_SIZE)));

		if(line.hasOption(COMPOST_MAX_COMPONENT_NUM))
			compostMaxComponentNum = Optional.of(Integer.parseInt(line.getOptionValue(COMPOST_MAX_COMPONENT_NUM)));
		
		if(line.hasOption(COMPOST_SCORE_THRESHOLD))
			compostScoreThreshold = Optional.of(Float.parseFloat(line.getOptionValue(COMPOST_SCORE_THRESHOLD)));

		if(line.hasOption(WATCH))
			watch = Optional.of(Pattern.compile(line.getOptionValue(WATCH)));

		if(line.hasOption(COMPOST_SIMILARITY_THRESHOLD))
			compostSimilarityThreshold = Optional.of(Float.parseFloat(line.getOptionValue(COMPOST_SIMILARITY_THRESHOLD)));
		

		if(line.hasOption(COMPOST_COEFF)) {
			List<String> strings = Splitter.on('-').splitToList(line.getOptionValue(COMPOST_COEFF));
			compostAlpha = Optional.of(Float.parseFloat(strings.get(0)));
			compostBeta = Optional.of(Float.parseFloat(strings.get(1)));
			compostGamma = Optional.of(Float.parseFloat(strings.get(2)));
			compostDelta = Optional.of(Float.parseFloat(strings.get(3)));
			Preconditions.checkArgument(
					1.0f == compostAlpha.get() + compostBeta.get() + compostGamma.get() + compostDelta.get(),
					String.format("The sum of Compost coeffs must be 1 (%3.2f+%3.2f+%3.2f+%3.2f=%3.2f)",
							compostAlpha.get(),
							compostBeta.get(),
							compostGamma.get(),
							compostDelta.get(),
							compostAlpha.get() + compostBeta.get() + compostGamma.get() + compostDelta.get()
							)
					);
		}
		
		
		/*
		 * Contextualizer
		 */
		contextualize = line.hasOption(CONTEXTUALIZE);
		allowMWTInContexts = line.hasOption(ALLOW_MWT_IN_CONTEXTS);
		contextualizeAllTerms = line.hasOption(CONTEXTUALIZE_ALL_TERMS);
		if(line.hasOption(CONTEXT_SCOPE)) {
			contextScope = Integer.parseInt(line.getOptionValue(CONTEXT_SCOPE));
		}
		
		if(line.hasOption(CLEAN_THRESHOLD))
			cleaningThreshold = Optional.of(Float.parseFloat(line.getOptionValue(CLEAN_THRESHOLD)));

		if(line.hasOption(CLEAN_TOP_N)) 
			cleaningTopN = Optional.of(Integer.parseInt(line.getOptionValue(CLEAN_TOP_N)));

		if(line.hasOption(CLEAN_PROPERTY)) {
			cleaningProperty = Optional.of(TermProperty.forName(line.getOptionValue(CLEAN_PROPERTY)));
		}
		
		if(line.hasOption(CLEAN_FILTER_VARIANTS))
			keepVariantsWhileCleaning = false;

		if(line.hasOption(PERIODIC_FILTER_PROPERTY)) {
			periodicFilteringProperty = Optional.of(TermProperty.forName(line.getOptionValue(PERIODIC_FILTER_PROPERTY)));
			if(line.hasOption(PERIODIC_FILTER_MAX_SIZE))
				maxSizeFilteringMaxSize = Integer.parseInt(line.getOptionValue(PERIODIC_FILTER_MAX_SIZE).trim()); 
		}

		
		if(line.hasOption(TSV))
			tsvFile = Optional.of(line.getOptionValue(TSV));
		if(line.hasOption(TSV_PROPERTIES)) {
			List<TermProperty> list = Lists.newArrayList();
			for(String pName:Splitter.on(",").split(line.getOptionValue(TSV_PROPERTIES))) {
				list.add(TermProperty.forName(pName));
			}
			TermProperty[] ary = new TermProperty[list.size()];
			tsvProperties = Optional.of(list.toArray(ary));
		}
		if(line.hasOption(TSV_VARIANT_SCORES))
			tsvShowVariantScores = true;

		if(line.hasOption(TBX))
			tbxFile = Optional.of(line.getOptionValue(TBX));
		if(line.hasOption(JSON))
			jsonFile = Optional.of(line.getOptionValue(JSON));

		if(line.hasOption(JSCASFILE))
			jsonCasFile = Optional.of(line.getOptionValue(JSCASFILE));
		
		if(line.hasOption(MATE))
			tagger =  Tagger.Mate;
		
		if(line.hasOption(MONGODB_STORE))
			mongoStoreDBURL = Optional.of(line.getOptionValue(MONGODB_STORE));
		
		if(line.hasOption(MONGODB_SOFT_LINK)) {
			Preconditions.checkArgument(line.hasOption(MONGODB_STORE), "The option %s requires the option %s", MONGODB_SOFT_LINK, MONGODB_STORE);
			mongoStoreSoftLinked = true;
		}

	}
}
