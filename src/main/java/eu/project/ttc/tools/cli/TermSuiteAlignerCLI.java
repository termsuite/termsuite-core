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
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.BilingualAligner;
import eu.project.ttc.engines.BilingualAligner.TranslationCandidate;
import eu.project.ttc.metrics.Cosine;
import eu.project.ttc.metrics.Explanation;
import eu.project.ttc.metrics.Jaccard;
import eu.project.ttc.metrics.SimilarityDistance;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.JSONTermIndexIO;
import eu.project.ttc.tools.TermSuiteAlignerBuilder;

/**
 * Command line interface for the Terminology extraction (Spotter+Indexer) engines.
 * 
 * @author Damien Cram
 */
public class TermSuiteAlignerCLI {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteAlignerCLI.class);
		

	/** Short usage description of the CLI */
	private static final String USAGE = "java [-DconfigFile=<file>] -Xms1g -Xmx2g -cp termsuite-core-x.x.jar eu.project.ttc.tools.cli.TermSuiteAlignerCLI";

	/// Parameter names
	private static final String TERM = "term";
	private static final String N = "n";
	private static final String TERM_LIST = "term-list";
	private static final String SOURCE_TERMINO = "source-termino";
	private static final String TARGET_TERMINO = "target-termino";
	private static final String DICTIONARY = "dictionary";
	private static final String DISTANCE = "distance";
	private static final String SHOW_EXPLANATION = "show-explanation";

	// Parameter options
	private static final String DISTANCE_COSINE = "cosine";
	private static final String DISTANCE_JACCARD = "jaccard";

	
	// values
	
	private static Optional<TermIndex> sourceTermino = Optional.absent();
	private static Optional<TermIndex> targetTermino = Optional.absent();
	private static String dicoPath;
	private static int n = 10;
	private static List<String> terms = Lists.newArrayList();
	private static SimilarityDistance distance = new Cosine();
	private static boolean showExplanation = false;

	
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
		String logPath = Paths.get("logs", "termsuite-aligner-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) +".log").toAbsolutePath().toString();
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
				TermSuiteCLIUtils.setGlobalLogLevel("info");
				TermSuiteCLIUtils.logCommandLineOptions(line);
				
				BilingualAligner aligner = TermSuiteAlignerBuilder.start()
						.setTargetTerminology(targetTermino.get())
						.setDicoPath(dicoPath)
						.setDistance(distance)
						.create();

				for(String term:terms) {
					Term sourceTerm = readSourceTerm(term);
					if(sourceTerm == null) {
						LOGGER.error("Cannot find term \"{}\" in {}",
								term,
								line.getOptionValue(SOURCE_TERMINO));
					} else {
						if(terms.size() > 1) {
							System.out.println("---");
							System.out.println(sourceTerm);
							System.out.println("-");
						}
						for(TranslationCandidate candidate:aligner.alignDistributional(sourceTerm, n, 1)) {
							if(showExplanation)
								System.out.format("%s\t%.3f\t%s\n",
										candidate.getTerm(),
										candidate.getScore(),
										((Explanation)candidate.getExplanation()).getTopNFeatures());
							else
								System.out.format("%s\t%.3f\n",
									candidate.getTerm(),
									candidate.getScore());
						}
					}
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
	
	private static Term readSourceTerm(String term) {
		for(Term t:sourceTermino.get().getTerms()) {
			if(t.getGroupingKey().equals(term)
					|| t.getPilot().equals(term)
					|| t.getLemma().equals(term)
					|| t.getPilot().equals(term.toLowerCase())
					|| t.getLemma().equals(term.toLowerCase()))
				return t;
		}
		return null;
	}

	private static Options declareOptions() {
		Options options = new Options();
		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				SOURCE_TERMINO, 
				true, 
				"Source terminology (json file)", 
				true));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				TARGET_TERMINO, 
				true, 
				"Target terminology (json file)", 
				true));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				TERM, 
				true, 
				"Source term to align", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				TERM_LIST, 
				true, 
				"File containing a list of source terms to align (one per line)", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				DICTIONARY, 
				true, 
				"Alignment dictionaries", 
				true));


		options.addOption(TermSuiteCLIUtils.createOption(
				N, 
				N, 
				true, 
				"The number of translation candidates to show in the output", 
				false));
		
		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				DISTANCE, 
				true, 
				"Similarity measure to compute the distance between two vectors ["+DISTANCE_COSINE+","+DISTANCE_JACCARD+"]", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				null, 
				SHOW_EXPLANATION, 
				false, 
				"Shows for each aligned term the most influencial co-terms", 
				false));

		return options;
	}

	public static void readArguments(CommandLine line) throws IOException {
		if(!line.hasOption(TERM) && !line.hasOption(TERM_LIST)) {
			String msg = String.format("ERROR: One option of --%s or --%s must be provided.", TERM, TERM_LIST);
			LOGGER.error(msg);
			System.err.flush();
			System.out.flush();
			System.err.println(msg);
			System.exit(1);
		}
		if(line.hasOption(TERM))
			terms.add(line.getOptionValue(TERM));
		if(line.hasOption(TERM_LIST)) {
			File file = new File(line.getOptionValue(TERM_LIST));
			for(String term:FileUtils.readLines(file, "UTF-8"))
				terms.add(Splitter.on("\t").splitToList(term).get(0).trim());
		}
		if(line.hasOption(N))
			n = Integer.parseInt(line.getOptionValue(N));
		if(line.hasOption(DISTANCE)) {
			if(line.getOptionValue(DISTANCE).equals(DISTANCE_COSINE))
				distance = new Cosine();
			else if(line.getOptionValue(DISTANCE).equals(DISTANCE_JACCARD))
				distance = new Jaccard();
			else 
				TermSuiteCLIUtils.exitWithErrorMessage(String.format("Unknown distance: %s. Allowed values: %s;%s",
						line.getOptionValue(DISTANCE),
						DISTANCE_COSINE,
						DISTANCE_JACCARD));
			
		}
		LOGGER.info("loading source termino {}",line.getOptionValue(SOURCE_TERMINO));
		sourceTermino = Optional.of(
				JSONTermIndexIO.load(new FileReader(line.getOptionValue(SOURCE_TERMINO)), true)
			);
		LOGGER.info("loading target termino {}",line.getOptionValue(TARGET_TERMINO));
		targetTermino = Optional.of(
				JSONTermIndexIO.load(new FileReader(line.getOptionValue(TARGET_TERMINO)), true)
			);
		dicoPath = line.getOptionValue(DICTIONARY);

		showExplanation = line.hasOption(SHOW_EXPLANATION);
	}

}
