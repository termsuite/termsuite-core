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
package eu.project.ttc.models;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import eu.project.ttc.utils.TermSuiteUtils;

public class TermBuilder {
	private static final Logger LOGGER  = LoggerFactory.getLogger(TermBuilder.class);
	
	private TermIndex termIndex;
	private OccurrenceStore occurrenceStore;
	
	private String groupingKey;
	private String spottingRule;
	private Optional<Integer> rank = Optional.absent();
	private Optional<Integer> id = Optional.absent();
	private List<TermWord> termWords = Lists.newArrayList();
	private List<TermOccurrence> termOccurrences = Lists.newArrayList();
	private Optional<Integer> frequency = Optional.absent();
	private Optional<Double> generalFrequencyNorm = Optional.absent();
	private Optional<Double> frequencyNorm = Optional.absent();
	private Optional<Double> specificity = Optional.absent();

	private Optional<ContextVector> contextVector = Optional.absent();
	
	private TermBuilder() {
	}
	
	private TermBuilder(TermIndex termIndex) {
		Preconditions.checkNotNull(termIndex);
		this.termIndex = termIndex;
	}

	public Term createAndAddToIndex() {
		Preconditions.checkNotNull(this.termIndex, "No TermIndex given.");
		if(!id.isPresent())
			id = Optional.of(this.termIndex.newId());
		Term term = create();
		this.termIndex.addTerm(term);
		return term;
	}
	
	public Term create() {
		String gKey = TermSuiteUtils.getGroupingKey(this.termWords);
		if(this.groupingKey != null && !groupingKey.equals(gKey)) {
			LOGGER.warn("Given grouping key ({}) does not match expected grouping key: {}",
					this.groupingKey,
					gKey);
		}
		Term term = new Term(termIndex.getOccurrenceStore(), id.get(), gKey, termWords, spottingRule);
		if(generalFrequencyNorm.isPresent())
			term.setGeneralFrequencyNorm(generalFrequencyNorm.get());
		if(frequencyNorm.isPresent())
			term.setFrequencyNorm(frequencyNorm.get());

		if(specificity.isPresent())
			term.setSpecificity(specificity.get());
		
		if(rank.isPresent())
			term.setRank(rank.get());
		
		if(contextVector.isPresent())
			term.setContextVector(contextVector.get());

		/*
		 *  1 - set occurrences
		 */
		for(TermOccurrence occ:termOccurrences) {
			occ.setTerm(term);
			term.addOccurrence(occ);
		}
		
		/*
		 * 2 - frequency must be set after occurrences because it overwrites 
		 * the exiting term frequency incremented by occurrences adding.
		 */
		if(frequency.isPresent())
			term.setFrequency(frequency.get());
			
		return term;
	}
	
	public TermBuilder setId(int id) {
		this.id = Optional.of(id);
		return this;
	}

	public TermBuilder setGroupingKey(String groupingKey) {
		this.groupingKey = groupingKey;
		return this;
	}

	public TermBuilder setSpottingRule(String spottingRule) {
		this.spottingRule = spottingRule;
		return this;
	}
	

	public TermBuilder addWord(Word word, String syntacticLabel) {
		this.termWords.add(new TermWord(word, syntacticLabel));
		return this;
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

	public TermBuilder addOccurrence(int begin, int end, Document sourceDocument, String coveredText) {
		this.termOccurrences.add(new TermOccurrence(null, coveredText, sourceDocument, begin, end));
		return this;
	}

	public void setContextVector(ContextVector vector) {
		this.contextVector  = Optional.of(vector);
	}
	public static TermBuilder start(TermIndex termIndex) {
		return new TermBuilder(termIndex);
	}

	public static TermBuilder start() {
		return new TermBuilder();
	}

	public TermBuilder setRank(int rank) {
		this.rank = Optional.of(rank);
		return this;
	}
	
	public TermBuilder setSpecificity(double specificity) {
		this.specificity = Optional.of(specificity);
		return this;
	}
}
