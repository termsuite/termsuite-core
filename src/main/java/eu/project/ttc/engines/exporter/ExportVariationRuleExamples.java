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
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ExternalResource;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.resources.YamlVariantRules;
import eu.project.ttc.utils.TermOccurrenceUtils;

public class ExportVariationRuleExamples extends AbstractTermIndexExporter {

//	private static final Logger LOGGER = LoggerFactory.getLogger(ExportVariationRuleExamples.class);
	
	public static final String YAML_VARIANT_RULES = "YamlVariantRules";
	@ExternalResource(key = YAML_VARIANT_RULES, mandatory = true)
	private YamlVariantRules yamlVariantRules;
	
	class TermPair implements Comparable<TermPair>{
		Term source;
		Term target;
		public TermPair(Term source, Term target) {
			super();
			this.source = source;
			this.target = target;
		}
		@Override
		public int compareTo(TermPair o) {
			return ComparisonChain.start()
					.compare(o.target.getFrequency(), this.target.getFrequency())
					.result();
		}
	}

	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException {
		
		final Multimap<String, TermPair> pairs = HashMultimap.create();
		
		for(Term t:acceptedTerms) {
			for(TermVariation v:t.getVariations(VariationType.MORPHOLOGICAL, VariationType.SYNTACTICAL))
				pairs.put(v.getInfo().toString(), new TermPair(t, v.getVariant()));
		}
		

		// gets all variant rules (event size-0) and sorts them
		TreeSet<VariantRule> varianRules = new TreeSet<VariantRule>(new Comparator<VariantRule>() {
			@Override
			public int compare(VariantRule o1, VariantRule o2) {
				
				return ComparisonChain.start()
						.compare(pairs.get(o2.getName()).size(), pairs.get(o1.getName()).size())
						.compare(o1.getName(), o2.getName())
						.result();
			}
		});
		varianRules.addAll(yamlVariantRules.getVariantRules());

		try {
			/*
			 * Display Summary
			 */
			int total = 0;
			int nbMatchingRules = 0;
			String summaryLine = "%-16s: %d\n";
			for(VariantRule rule:varianRules) {
				int nbPairs = pairs.get(rule.getName()).size();
				total+=nbPairs;
				if(nbPairs > 0)
					nbMatchingRules++;
				writer.write(String.format(summaryLine, rule.getName(), nbPairs));
			}
			writer.write("---\n");
			writer.write(String.format(summaryLine, "TOTAL", total));
			writer.write(String.format("%-16s: %d / %d\n", "nb matching rules", nbMatchingRules, varianRules.size()));
			writer.write("\n---\n");
		
	
			/*
			 * Display variant rules' matches.
			 */
			for(VariantRule rule:varianRules) {
				List<TermPair> sortedPairs = Lists.newArrayList(pairs.get(rule.getName()));
				Collections.sort(sortedPairs);
				
				int nbOverlappingOccs = 0;
				int nbStrictOccs = 0;
				List<String> lines = Lists.newArrayList();
				for(TermPair pair:sortedPairs) {
					List<TermOccurrence> targetStrictOccurrences = Lists.newLinkedList(pair.target.getOccurrences());
					TermOccurrenceUtils.removeOverlaps(pair.source.getOccurrences(), targetStrictOccurrences);
					nbOverlappingOccs += pair.target.getFrequency();
					nbStrictOccs += targetStrictOccurrences.size();
					lines.add(String.format("%14d%14d%35s || %-35s\n", 
							pair.target.getFrequency(),
							targetStrictOccurrences.size(),
							pair.source.getGroupingKey(), 
							pair.target.getGroupingKey()));
				}

				writer.write("\n----------------------------------------------------------------------------------------------------------------\n");
				writer.write(String.format("---------------------  %s   [nb_terms: %d, total_occs: %d, total_strict_occurrences: %d] \n", 
						rule.getName(),
						sortedPairs.size(),
						nbOverlappingOccs,
						nbStrictOccs
						));
				writer.write("----------------------------------------------------------------------------------------------------------------\n");
				writer.write(String.format("%14s%14s\n", "fr_overlaps", "fr_strict"));
				for(String line:lines)
					writer.write(line);
				writer.write(String.format("TOTAL: %7s%14s\n", nbOverlappingOccs, nbStrictOccs));
				
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		
	}
}
