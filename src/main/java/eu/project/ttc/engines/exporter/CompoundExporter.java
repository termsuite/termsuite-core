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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.Component;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.Word;

public class CompoundExporter extends AbstractTermIndexExporter {

	private static final String LINE_FORMAT = "%-30s %-10s %-35s %d\n";

	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException {
		
		try {
			Multimap<Word,Term> terms = HashMultimap.create();
			Set<Word> compounds = Sets.newHashSet();
			for(Term t:acceptedTerms) {
				Word w = t.getWords().get(0).getWord();
				if(t.getWords().size() == 1 && w.isCompound()) {
					compounds.add(w);
					terms.put(w, t);
				}
			}
			final Map<Word,Integer> frequencies = Maps.newHashMap();
			for(Word w: terms.keySet()) {
				int f = 0;
				for(Term t:terms.get(w))
					f += t.getFrequency();
				
				frequencies.put(w, f);
			}
			
			
			Set<Word> sortedCompounds = new TreeSet<Word>(new Comparator<Word>() {
				@Override
				public int compare(Word o1, Word o2) {
					return ComparisonChain.start()
							.compare(frequencies.get(o2), frequencies.get(o1))
							.result();
				}
			});
			sortedCompounds.addAll(compounds);
			
			for(Word w:sortedCompounds) {
				List<String> compLemmas = Lists.newArrayList();
				for(Component c:w.getComponents())
					compLemmas.add(c.getLemma());
				writer.write(String.format(LINE_FORMAT, 
					w.getLemma(),
					w.getCompoundType(),
					Joiner.on('|').join(compLemmas),
					frequencies.get(w)
				));
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}		
	}
}
