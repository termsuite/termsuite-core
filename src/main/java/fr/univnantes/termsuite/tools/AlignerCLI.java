
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
import fr.univnantes.termsuite.tools.opt.CliOption;

/**
 * Command line interface for the Terminology extraction (Spotter+Indexer) engines.
 * 
 * @author Damien Cram
 */
public class AlignerCLI extends CommandLineClient { // NO_UCD (public entry point)
	private static final Logger LOGGER = LoggerFactory.getLogger(AlignerCLI.class);
	
	public AlignerCLI() {
		super("translates domain-specific terms in multiligual comparable corpora from given language to given target language");
	}
	
	@Override
	public void configureOpts() {
		declareMandatory(CliOption.SOURCE_TERMINO);
		declareMandatory(CliOption.TARGET_TERMINO);
		declareExactlyOneOf(CliOption.TERM, CliOption.TERM_LIST);
		declareMandatory(CliOption.DICTIONARY);
		declareFacultative(CliOption.EXPLAIN);
		declareFacultative(CliOption.DISTANCE);
		declareFacultative(CliOption.N);
		declareFacultative(CliOption.ALIGNER_TSV);
		declareFacultative(CliOption.MIN_CANDIDATE_FREQUENCY);
	}

	@Override
	protected void run() throws Exception {
		BilingualAligner builder = TermSuite.bilingualAligner();
		
		IndexedCorpus sourceTermino = asIndexedCorpus(CliOption.SOURCE_TERMINO);
		builder
			.setSourceTerminology(sourceTermino);

		builder
			.setTargetTerminology(asIndexedCorpus(CliOption.TARGET_TERMINO));

		builder
			.setDicoPath(asPath(CliOption.DICTIONARY));

		
		if(isSet(CliOption.DISTANCE)) {
			Class<? extends SimilarityDistance> distCls = SimilarityDistance.forName(asString(CliOption.DISTANCE));
			builder.setDistance(distCls);
		}
		BilingualAlignmentService aligner = builder.create();

		boolean explain = isSet(CliOption.EXPLAIN);
		int nbCandidates = isSet(CliOption.N) ? 
								asInt(CliOption.N) 
									: 10;
		int minCandidateFrequency = isSet(CliOption.MIN_CANDIDATE_FREQUENCY) ? 
								asInt(CliOption.MIN_CANDIDATE_FREQUENCY) 
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
		if(isSet(CliOption.ALIGNER_TSV))
			try {
				return new PrintStream(asPath(CliOption.ALIGNER_TSV).toFile());
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not write to file " + asString(CliOption.ALIGNER_TSV), e);
				CliUtil.throwException("Could not write to file " + asString(CliOption.ALIGNER_TSV));
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
		if(isSet(CliOption.TERM)) {
			String term = asTermString(CliOption.TERM);
			LOGGER.info("Loading source term: {}", term);
			sourceTerms.add(findTerm(sourceInjector, term));
		} else if(isSet(CliOption.TERM_LIST)) {
			try {
				Files.readAllLines(asPath(CliOption.TERM_LIST)).stream()
					.map(line -> line.trim())
					.filter(s -> !s.isEmpty())
					.forEach(termStr ->
						sourceTerms.add(findTerm(sourceInjector, termStr)));
			} catch (IOException e) {
				LOGGER.error("Could not read term list file " + asString(CliOption.TERM_LIST), e);
				CliUtil.throwException("Could not read term list file %s", asString(CliOption.TERM_LIST));
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
