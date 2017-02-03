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
package fr.univnantes.termsuite.model;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.utils.TermSuiteUtils;

public class TermBuilder {
	private static final Logger LOGGER  = LoggerFactory.getLogger(TermBuilder.class);

	private Optional<String> groupingKey = Optional.empty();
	private java.util.Optional<String> pilot = java.util.Optional.empty();
	private java.util.Optional<Integer> documentFrequency = java.util.Optional.empty();
	private Optional<String> spottingRule = Optional.empty();
	private Optional<Integer> rank = Optional.empty();
	private List<TermWord> termWords = Lists.newArrayList();
	private Optional<Integer> frequency = Optional.empty();
	private Optional<Double> generalFrequencyNorm = Optional.empty();
	private Optional<Double> frequencyNorm = Optional.empty();
	private Optional<Double> specificity = Optional.empty();
	private boolean forceManualGroupingKey = false;
	private Optional<ContextVector> contextVector = Optional.empty();

	private Terminology termino;
	
	public TermBuilder() {
	}
	
	private TermBuilder(Terminology termino) {
		this.termino = termino;
	}
	
	public Term create() {
		String expectedGroupingKey = TermSuiteUtils.getGroupingKey(this.termWords);
		if(this.groupingKey.isPresent()) {
			if(!forceManualGroupingKey && !groupingKey.get().equals(expectedGroupingKey)) 
				LOGGER.warn("Given grouping key ({}) does not match expected grouping key: {}",
						this.groupingKey.get(),
						expectedGroupingKey);
		} else
			this.groupingKey = Optional.of(expectedGroupingKey);
		
		Term term = new Term(groupingKey.get(), termWords);

		if(spottingRule.isPresent())
			term.setProperty(TermProperty.SPOTTING_RULE,spottingRule.get());
		
		if(generalFrequencyNorm.isPresent())
			term.setProperty(TermProperty.GENERAL_FREQUENCY_NORM,generalFrequencyNorm.get());
		
		if(frequencyNorm.isPresent())
			term.setProperty(TermProperty.FREQUENCY_NORM,frequencyNorm.get());


		if(pilot.isPresent())
			term.setProperty(TermProperty.PILOT,pilot.get());

		if(documentFrequency.isPresent())
			term.setProperty(TermProperty.DOCUMENT_FREQUENCY,documentFrequency.get());

		if(specificity.isPresent())
			term.setProperty(TermProperty.SPECIFICITY,specificity.get());
		
		if(rank.isPresent())
			term.setProperty(TermProperty.RANK,rank.get());
		
		if(contextVector.isPresent())
			term.setContext(contextVector.get());

		/*
		 * Sets the pattern
		 */
		List<String> labels = Lists.newArrayListWithCapacity(termWords.size());
		for(TermWord w:termWords) 
			labels.add(w.getSyntacticLabel());
		String pattern = Joiner.on(' ').join(labels);
		term.setProperty(TermProperty.PATTERN, pattern);
		
		
		/*
		 * 2 - frequency must be set after occurrences because it overwrites 
		 * the exiting term frequency incremented by occurrences adding.
		 */
		if(frequency.isPresent()) {
			term.setProperty(TermProperty.FREQUENCY, frequency.get());
		} 
//		else
//			term.setFrequency(occurrences.size());

		return term;
	}
	
	public TermBuilder setGroupingKey(String groupingKey) {
		return setGroupingKey(groupingKey, false);
	}

	public TermBuilder setGroupingKey(String groupingKey, boolean forceManualGroupingKey) {
		if(termino != null)
			Preconditions.checkArgument(
					!termino.getTerms().containsKey(groupingKey), 
					"Term %s already exists", 
					groupingKey);
		
		this.groupingKey = Optional.of(groupingKey);
		this.forceManualGroupingKey = forceManualGroupingKey;
		return this;
	}

	public TermBuilder setSpottingRule(String spottingRule) {
		this.spottingRule = Optional.of(spottingRule);
		return this;
	}
	

	public TermBuilder addWord(Word word, String syntacticLabel, boolean isSWT) {
		TermWord e = new TermWord(word, syntacticLabel, isSWT);
		this.termWords.add(e);
		return this;
	}

	public TermBuilder addWord(Word word, String syntacticLabel) {
		return addWord(word, syntacticLabel, false);
	}

	public TermBuilder setFrequency(int freq) {
		this.frequency = Optional.of(freq);
		return this;
	}

	public TermBuilder setFrequencyNorm(double frequencyNorm) {
		this.frequencyNorm = Optional.of(frequencyNorm);
		return this;
	}

	public TermBuilder setGeneralFrequencyNorm(double generalFrequencyNorm) {
		this.generalFrequencyNorm = Optional.of(generalFrequencyNorm);
		return this;
	}

	public void setContextVector(ContextVector vector) {
		this.contextVector  = Optional.of(vector);
	}

	public static TermBuilder start(Terminology terminology) {
		return new TermBuilder(terminology);
	}

	public TermBuilder setRank(int rank) {
		this.rank = Optional.of(rank);
		return this;
	}
	
	public TermBuilder setSpecificity(double specificity) {
		this.specificity = Optional.of(specificity);
		return this;
	}

	public void addWord(String lemma, String stem, String label, boolean isSWT) {
		Word word;
		if(!this.termino.getWords().containsKey(lemma))
			word = WordBuilder.start(termino).setLemma(lemma).setStem(stem).create();
		else
			word = this.termino.getWords().get(lemma);
		TermWord w = new TermWord(word, label, isSWT);
		this.termWords.add(w);
	}
	
//	private List<Object[]> occurrences=Lists.newArrayList();

//	public TermBuilder addOccurrence(int begin, int end, String docUrl, String text) {
//		occurrences.add(new Object[]{docUrl, begin, end, text});
//		return this;
//	}

	public TermBuilder setPilot(String pilot) {
		this.pilot = java.util.Optional.of(pilot);
		return this;
	}
	
	public TermBuilder setDocumentFrequency(int documentFrequency) {
		this.documentFrequency = java.util.Optional.of(documentFrequency);
		return this;
	}


}
