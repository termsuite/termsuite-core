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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class TermBuilder {
	
	private TermIndex termIndex;
	private String groupingKey;
	private String spottingRule;
	private Optional<Integer> id = Optional.absent();
	private List<TermWord> termWords = Lists.newArrayList();
	private List<TermOccurrence> termOccurrences = Lists.newArrayList();
	private Optional<Integer> frequency = Optional.absent();
	private Optional<Float> specificity = Optional.absent();
	private Optional<ContextVector> contextVector = Optional.absent();
	
	public TermBuilder() {
	}
	
	public TermBuilder(TermIndex termIndex) {
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
		Preconditions.checkNotNull(groupingKey);
		Term term = new Term(id.get(), groupingKey, termWords, spottingRule);
		if(specificity.isPresent())
			term.setWR(specificity.get());
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

	public TermBuilder setSpecificity(float spec) {
		this.specificity = Optional.of(spec);
		return this;
	}

	public TermBuilder addOccurrence(int begin, int end, Document sourceDocument, String coveredText) {
		this.termOccurrences.add(new TermOccurrence(null, coveredText, sourceDocument, begin, end));
		return this;
	}

	public void setContextVector(ContextVector vector) {
		this.contextVector  = Optional.of(vector);
	}
}
