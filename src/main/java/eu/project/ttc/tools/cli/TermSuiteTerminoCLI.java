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

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.utils.TermUtils;
import fr.univnantes.lina.UIMAProfiler;

/**
 * Command line interface for the Terminology extraction (Spotter+Indexer) engines.
 * 
 * @author Damien Cram
 */
public class TermSuiteTerminoCLI {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteTerminoCLI.class);
		

	/** Short usage description of the CLI */
	private static final String USAGE = "java [-DconfigFile=<file>] -Xms1g -Xmx2g -cp termsuite-core-x.x.jar eu.project.ttc.tools.cli.TermSuiteTerminoCLI";

	/// Parameter names
	/** Name of the example limit parament */
	private static final String TEXT = "text";


	/** Name of the watch parameter */
	private static final String WATCH = "watch";

	/// Parameter names
	/** Name of the example limit parament */
	private static final String EXAMPLE_LIMIT = "nb-examples";

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
	 * Cleaning arguments
	 */
	private static final String CLEAN_THRESHOLD = "filter-th";
	private static final String CLEAN_TOP_N = "filter-top-n";
	private static final String CLEAN_PROPERTY = "filter-property";

	
	// the tsv file path argument
	private static final String TSV = "tsv";

	// the json file path argument
	private static final String JSON = "json";

	// the tbx file path argument
	private static final String TBX = "tbx";

	// the argument whether to activate the profiling
	private static final String PROFILE = "profile";

	// tagger argument
	private static Tagger tagger = Tagger.TreeTagger;

    private static String resourcePack = null;
    private static String corpusPath = null;
    private static Lang language = null;
    private static String encoding = "UTF-8";
//    private static String pipelineCRInputDirectory = null;
    private static String taggerHome = "";
    private static String inlineText = null;
	private static int exampleLimit = 10;
	private static TermSuiteCollection corpusType = TermSuiteCollection.TXT;
	private static float graphicalSimilarityThreshold = 0.9f;
	
	/*
	 * Cleaning parameters
	 */
	private static Optional<Float> cleaningThreshold = Optional.absent();
	private static Optional<Integer> cleaningTopN = Optional.absent();
	private static Optional<TermProperty> cleaningProperty = Optional.absent();


	/*
	 * Export params
	 */
	private static Optional<String> tsvFile = Optional.absent();
	private static Optional<String> jsonFile = Optional.absent();
	private static Optional<String> tbxFile = Optional.absent();
	

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

	/*
	 * Profiling parameter
	 */
	private static boolean profilingOn = false;
	
	
	/**
	 * Application entry point
	 * 
	 * @param args
	 *            Command line arguments
     * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		Stopwatch sw = Stopwatch.createStarted();
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

				
				if(profilingOn) {
					UIMAProfiler.setActivated(true);
					UIMAProfiler.setExampleLimit(exampleLimit);
				}
				TermSuiteCLIUtils.logCommandLineOptions(line);
				
				TermSuitePipeline pipeline = TermSuitePipeline.create(language.getCode());
				
				if(isInlineMode()) {
					pipeline.setInlineString(inlineText);
				} else {
					pipeline.setCollection(corpusType, corpusPath, encoding);
				}

				// resource
				pipeline.setResourcePath(resourcePack);
				
				// tokenizer
				pipeline.aeWordTokenizer();
				
				// tagger
				if(tagger == Tagger.TreeTagger) 
					pipeline.setTreeTaggerHome(taggerHome)
						.aeTreeTagger();
				else if(tagger == Tagger.Mate) 
					pipeline.setMateModelPath(taggerHome)
						.aeMateTaggerLemmatizer();
				
				
				// stemmer
				pipeline.aeStemmer();
				
				// regex spotter
				pipeline.aeRegexSpotter();
				
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
				
				// stats
				pipeline.haeCasStatCounter("at end of pipeline");

				// cleaning
				if(cleaningThreshold.isPresent()) 
					pipeline.aeThresholdCleaner(cleaningProperty.get(), cleaningThreshold.get());
				else if(cleaningTopN.isPresent()) 
					pipeline.aeTopNCleaner(cleaningProperty.get(), cleaningTopN.get());
				
				
				// Export
				if(tsvFile.isPresent())  {
					pipeline.haeScorifier();
					pipeline.setTsvExportProperties(
						TermProperty.PILOT,
						TermProperty.FREQUENCY,
						TermProperty.WR_LOG
					);
					pipeline.haeTsvExporter(tsvFile.get());

				}
				if(tbxFile.isPresent()) 
					pipeline.haeTbxExporter(tbxFile.get());
				if(jsonFile.isPresent()) 
					pipeline.haeJsonExporter(jsonFile.get());

				// run the pipeline
				final String termIndexName = "ScriptTermIndex_" + System.currentTimeMillis();
	            if(isInlineMode()) {
	            	LOGGER.info("Running TermSuite pipeline (inline mode)");
	            	JCas cas = JCasFactory.createJCas();
	            	cas.setDocumentText(inlineText);
	            	cas.setDocumentLanguage(language.getCode());
	            	pipeline.run(cas);
	            	UIMAProfiler.displayAll(new PrintStream(System.err, true, "UTF-8"));
	            	System.err.flush();
	            	System.out.println("Term index: ");
					TermIndex index = MemoryTermIndexManager.getInstance().getIndex(termIndexName);
	            	TermUtils.showIndex(index, System.out, watch);
	            } else {
	            	LOGGER.info("Running TermSuite pipeline in corpus mode");
	            	pipeline.run();
	            	if(profilingOn) {
		            	UIMAProfiler.setLatexFormat(false);
		            	UIMAProfiler.displayAll(new PrintStream(System.err, true, "UTF-8"));
	            	}
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

	private static boolean isInlineMode() {
		return inlineText != null && inlineText.trim().length() > 0;
	}

	private static Options declareOptions() {
		Options options = new Options();
		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				MATE, 
				false, 
				"Use Mate tagger instead of TreeTagger.", 
				false));

		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				PROFILE, 
				false, 
				"Allows profiling", 
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
				EXAMPLE_LIMIT, 
				true, 
				"The number of examples to display for each profiling hit", 
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
				TBX, 
				true,
				"The tbx file path where to export the term index");

		options.addOption(
				null, 
				JSON, 
				true,
				"The json file path where to export the term index");

		
		return options;
	}

	public static void readArguments(CommandLine line) throws IOException {
		inlineText = line.getOptionValue(TEXT);
		if(inlineText == null)
			inlineText = TermSuiteCLIUtils.readIn(encoding);
		
		corpusPath = line.getOptionValue(PATH_TO_CORPUS);
		if(inlineText == null && corpusPath == null)
			TermSuiteCLIUtils.exitWithErrorMessage("Either the argument --" + TEXT + " or --" + PATH_TO_CORPUS + " must be set.");
		
		resourcePack = line.getOptionValue(PATH_TO_RESOURCE_PACK);
		
		
		language = Lang.forName(line.getOptionValue(TermSuiteCLIUtils.P_LANGUAGE));
		
		encoding = line.getOptionValue(TermSuiteCLIUtils.P_ENCODING, "UTF-8");
		
		taggerHome = line.getOptionValue(P_TAGGER_HOME_DIRECTORY);
		
		if(line.hasOption(PROFILE))
			profilingOn = true;
			
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
			
		
		exampleLimit = Integer.parseInt(line.getOptionValue(EXAMPLE_LIMIT, "5"));
		
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

		if(line.hasOption(CLEAN_THRESHOLD))
			cleaningThreshold = Optional.of(Float.parseFloat(line.getOptionValue(CLEAN_THRESHOLD)));

		if(line.hasOption(CLEAN_TOP_N)) 
			cleaningTopN = Optional.of(Integer.parseInt(line.getOptionValue(CLEAN_TOP_N)));

		if(line.hasOption(CLEAN_PROPERTY)) 
			cleaningProperty = Optional.of(TermProperty.forName(line.getOptionValue(CLEAN_PROPERTY)));

		if(line.hasOption(TSV))
			tsvFile = Optional.of(line.getOptionValue(TSV));
		if(line.hasOption(TBX))
			tbxFile = Optional.of(line.getOptionValue(TBX));
		if(line.hasOption(JSON))
			jsonFile = Optional.of(line.getOptionValue(JSON));
		
		if(line.hasOption(MATE))
			tagger =  Tagger.Mate;

	}

}
