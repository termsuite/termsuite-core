
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

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
package fr.univnantes.termsuite.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import fr.univnantes.termsuite.alignment.BilingualAlignmentService;
import fr.univnantes.termsuite.alignment.TranslationCandidate;
import fr.univnantes.termsuite.api.BilingualAligner;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.tools.opt.TermSuiteCliOption;

/**
 * Command line interface for the Terminology extraction (Spotter+Indexer) engines.
 * 
 * @author Damien Cram
 */
public class AlignerCLI extends CommandLineClient { // NO_UCD (public entry point)
	private static final Logger LOGGER = LoggerFactory.getLogger(AlignerCLI.class);
	
	public AlignerCLI() {
		super("Translates domain-specific terms in multiligual comparable corpora from given language to given target language.");
	}
	
	@Override
	public void configureOpts() {
		declareMandatory(TermSuiteCliOption.SOURCE_TERMINO);
		declareMandatory(TermSuiteCliOption.TARGET_TERMINO);
		declareExactlyOneOf(TermSuiteCliOption.TERM, TermSuiteCliOption.TERM_LIST);
		declareMandatory(TermSuiteCliOption.DICTIONARY);
		declareFacultative(TermSuiteCliOption.EXPLAIN);
		declareFacultative(TermSuiteCliOption.DISTANCE);
		declareFacultative(TermSuiteCliOption.N);
		declareFacultative(TermSuiteCliOption.ALIGNER_TSV);
		declareFacultative(TermSuiteCliOption.MIN_CANDIDATE_FREQUENCY);
	}

	@Override
	protected void run() throws Exception {
		BilingualAligner builder = TermSuite.bilingualAligner();
		
		IndexedCorpus sourceTermino = asIndexedCorpus(TermSuiteCliOption.SOURCE_TERMINO);
		builder
			.setSourceTerminology(sourceTermino);

		builder
			.setTargetTerminology(asIndexedCorpus(TermSuiteCliOption.TARGET_TERMINO));

		builder
			.setDicoPath(asPath(TermSuiteCliOption.DICTIONARY));

		
		if(isSet(TermSuiteCliOption.DISTANCE)) {
			Class<? extends SimilarityDistance> distCls = SimilarityDistance.forName(asString(TermSuiteCliOption.DISTANCE));
			builder.setDistance(distCls);
		}
		BilingualAlignmentService aligner = builder.create();

		boolean explain = isSet(TermSuiteCliOption.EXPLAIN);
		int nbCandidates = isSet(TermSuiteCliOption.N) ? 
								asInt(TermSuiteCliOption.N) 
									: 10;
		int minCandidateFrequency = isSet(TermSuiteCliOption.MIN_CANDIDATE_FREQUENCY) ? 
								asInt(TermSuiteCliOption.MIN_CANDIDATE_FREQUENCY) 
									: 2;
		
		Injector sourceInjector = TermSuite.indexedCorpusInjector(sourceTermino);
		PrintStream printStream = getPrintStream();
		for(Term sourceTerm:getSourceTerms(sourceInjector)) {
			List<TranslationCandidate> candidates = aligner.align(
					sourceTerm, 
					nbCandidates, 
					minCandidateFrequency);
			outputs(
					printStream,
					sourceTerm, 
					candidates,
					explain);
		}
	}

	private PrintStream getPrintStream() {
		if(isSet(TermSuiteCliOption.ALIGNER_TSV))
			try {
				return new PrintStream(asPath(TermSuiteCliOption.ALIGNER_TSV).toFile());
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not write to file " + asString(TermSuiteCliOption.ALIGNER_TSV), e);
				CliUtil.throwException("Could not write to file " + asString(TermSuiteCliOption.ALIGNER_TSV));
				return null;
			}
		else
			return System.out;
	}

	
	private static final String OUTPUT_FORMAT_EXPL = "%d\t%s\t%s\t%.3f\t%s\t%s\n";
	private static final String OUTPUT_FORMAT = "%d\t%s\t%s\t%.3f\t%s\n";

	private void outputs(
			PrintStream out,
			Term sourceTerm, 
			List<TranslationCandidate> candidates, 
			boolean explain) {
		
		for(int i = 0; i< candidates.size(); i++) {
			TranslationCandidate candidate = candidates.get(i);
			if(explain)
				out.format(OUTPUT_FORMAT_EXPL,
						i+1,
						sourceTerm.getPilot(),
						candidate.getTerm().getPilot(),
						candidate.getScore(),
						candidate.getMethod(),
						candidate.getExplanation().getText());
			else
				out.format(OUTPUT_FORMAT,
						i+1,
						sourceTerm.getPilot(),
						candidate.getTerm().getPilot(),
						candidate.getScore(),
						candidate.getMethod());
		}
	}

	private List<Term> getSourceTerms(Injector sourceInjector) {
		List<Term> sourceTerms = new ArrayList<>();
		if(isSet(TermSuiteCliOption.TERM)) {
			List<String> terms = asTermString(TermSuiteCliOption.TERM);
			LOGGER.info("Loading source terms: {}", terms);
			for(String term:terms)
				sourceTerms.add(findTerm(sourceInjector, term));
		} else if(isSet(TermSuiteCliOption.TERM_LIST)) {
			try {
				Files.readAllLines(asPath(TermSuiteCliOption.TERM_LIST)).stream()
					.map(line -> line.trim())
					.filter(s -> !s.isEmpty())
					.forEach(termStr ->
						sourceTerms.add(findTerm(sourceInjector, termStr)));
			} catch (IOException e) {
				LOGGER.error("Could not read term list file " + asString(TermSuiteCliOption.TERM_LIST), e);
				CliUtil.throwException("Could not read term list file %s", asString(TermSuiteCliOption.TERM_LIST));
			}
				
		}
		return sourceTerms;
	}

	private Term findTerm(Injector sourceINjector, String termString) {
		termString = termString
					.replaceAll("\\s+", " ")
					.trim()
					;
		TerminologyService sourceTermnio = sourceINjector.getInstance(TerminologyService.class);
		IndexService indexService = sourceINjector.getInstance(IndexService.class);
		if(sourceTermnio.containsTerm(termString))
			return sourceTermnio.getTerm(termString).getTerm();
		else {
			TermIndex lemmaIndex = indexService.getIndex(TermIndexType.LEMMA_LOWER_CASE);
			if(!lemmaIndex.getTerms(termString).isEmpty()) {
				return lemmaIndex.getTerms(termString).get(0);
			} else if(!lemmaIndex.getTerms(termString.toLowerCase()).isEmpty()) {
				return lemmaIndex.getTerms(termString.toLowerCase()).get(0);
			} else {
				TermIndex pilotIndex = indexService.getIndex(TermIndexType.TERM_PILOT_LOWERCASE);
				if(!pilotIndex.getTerms(termString).isEmpty()) {
					return pilotIndex.getTerms(termString).get(0);
				} else if(!pilotIndex.getTerms(termString.toLowerCase()).isEmpty()) {
					return pilotIndex.getTerms(termString.toLowerCase()).get(0);
				} else {
					CliUtil.throwException("No such term found in source terminology: %s", termString);
				}
			}
		}
		return null;
	}

	/**
	 * Application entry point
	 * 
	 * @param args
	 *            Command line arguments
     * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		new AlignerCLI().launch(args);
	}

}
