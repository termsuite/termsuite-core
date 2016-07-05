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
package eu.project.ttc.engines.exporter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

/**
 * Exports a {@link TermIndex} in the tsv evaluation format.
 * 
 * @author Damien Cram
 *
 */
public class VariantEvalExporter extends AbstractTermIndexExporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(VariantEvalExporter.class);

	public static final String TOP_N = "TopN";
	@ConfigurationParameter(name = TOP_N, mandatory = false, defaultValue = "50")
	private int topN;

	public static final String NB_EXAMPLE_OCCURRENCES = "NbExampleOccurrences";
	@ConfigurationParameter(name = NB_EXAMPLE_OCCURRENCES, mandatory = false, defaultValue = "5")
	private int nbExampleOccurrences;

	public static final String OCCURRENCE_CONTEXT_SIZE = "OccurrenceContextSize";

	@ConfigurationParameter(name = OCCURRENCE_CONTEXT_SIZE, mandatory = false, defaultValue = "50")
	private int contextSize;
	
	public static final String NB_VARIANTS_PER_TERM = "NbVariantsPerTerm";
	@ConfigurationParameter(name = NB_VARIANTS_PER_TERM, mandatory = false, defaultValue = "100000")
	private int nbVariantsPerTerm;

	private void printTermOccurrences(Term term) throws IOException {
		List<TermOccurrence> occurrences = Lists.newArrayList(term.getOccurrences());
		Collections.shuffle(occurrences);
		int occCnt = 0;
		for(TermOccurrence occurrence:occurrences) {
			if(occCnt > this.nbExampleOccurrences)
				break;
			printOccurrence(occurrence);
			occCnt++;
		}
	}

	private void printOccurrence(TermOccurrence occurrence) throws IOException {
		writer.write("#\t\t  ...");
		String textualContext = occurrence.getTextualContext(contextSize);
		writer.write(textualContext);
		writer.write("\n");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}

	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms)
			throws AnalysisEngineProcessException {
		
		try {
			int rank = 0;
			int variantCnt = 0;
			for(Term t:acceptedTerms) {
				if(t.isVariant())
					continue;
				printBase(++rank, t);
				int variantRank = 0;
				for(TermVariation variation:t.getVariations(VariationType.MORPHOLOGICAL, VariationType.SYNTACTICAL)) {
					if(variantRank >= nbVariantsPerTerm)
						break;
					variantCnt++;
					variantRank++;
					printVariation(rank, variantRank, variation);
					printTermOccurrences(variation.getVariant());
				}
				
				if(variantCnt>this.topN)
					break;
			}

		} catch (IOException e) {
			LOGGER.error("An error occurred during export.");
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void printVariation(int termRank, int variantRank, TermVariation variation) throws IOException {
		Term variant = variation.getVariant();
		String pilot = variant.getForms().iterator().next();
		writer.write(Integer.toString(termRank));
		writer.write("\t");
		writer.write("V_" + Integer.toString(variantRank));
		writer.write("\t");
		writer.write(String.format("<%s>", variation.getInfo().toString()));
		writer.write("\t");
		writer.write(String.format("%s (%d)", pilot, variant.getFrequency()));
		writer.write("\t");
		writer.write(String.format("[%s]", variant.getGroupingKey()));
		writer.write("\t");
		writer.write("{is_variant: _0_or_1_, variant_type: _syn_termino_other_}");
		writer.write("\n");		
	}

	private void printBase(int rank, Term t) throws IOException {
		writer.write(Integer.toString(rank));
		writer.write("\t");
		writer.write("T");
		writer.write("\t");
		writer.write(t.getForms().iterator().next());
		writer.write("\t");
		writer.write(String.format("[%s]", t.getGroupingKey()));
		writer.write("\n");
	}
}
