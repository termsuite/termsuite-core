/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class SuffixDerivationExporter extends AbstractTermIndexExporter {

	private static final String TERM_LINE_FORMAT = "%-35s %d%n";
	private static final String DERIVATE_LINE_FORMAT = "  -> %-30s %d%n";

	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException {
		
		try {
			Multimap<Term,Term> derivates = HashMultimap.create();
			for(Term t:acceptedTerms) {
				if(t.isSingleWord()) {
					for(TermVariation v:t.getVariations()) {
						if(v.getVariationType() == VariationType.DERIVATIONAL) {
							derivates.put(t, v.getVariant());
						}
					}
 				}
			}
			
			Set<Term> sortedTerms = new TreeSet<Term>(TermProperty.SPECIFICITY.getComparator(
					this.termIndexResource.getTermIndex(), 
					true));
			sortedTerms.addAll(derivates.keySet());
			
			for(Term t:sortedTerms) {
				Set<Term> devs = Sets.newHashSet(derivates.get(t));
				writer.write(String.format(TERM_LINE_FORMAT, 
					t.getGroupingKey(),
					t.getFrequency()));
				for(Term d:devs) {
					writer.write(String.format(DERIVATE_LINE_FORMAT, 
							d.getGroupingKey(),
							d.getFrequency()));
				}
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}		
	}
}
