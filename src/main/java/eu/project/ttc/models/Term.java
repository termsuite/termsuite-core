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
package eu.project.ttc.models;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import eu.project.ttc.utils.TermSuiteConstants;


public class Term implements Comparable<Term> {
	
	private ContextVector context;
	
	/*
	 * The identifier and display string of this term
	 */
	private String groupingKey;

	/*
	 * The numerical id in the term index
	 */
	private int id;
	
	/*
	 * The most frequent form
	 */
	private String pilot;
	
	/*
	 * The term rank
	 */
	private int rank;

	
	private int documentFrequency;

	
	private double normalizedTermFrequency;
	
	private double normalizedGeneralTermFrequency;
	
	/*
	 * The weirdness ratio of this term
	 */
	private double specificity;
	
	/*
	 * 
	 */
	private double tfIdf;

	/*
	 * The frequency of this term
	 */
	private int frequency = 0;
	
	/*
	 * The syntactic pattern of this term
	 */
	private String pattern;
	
	/*
	 * The spotting rule
	 */
	private String spottingRule;

	/*
	 * A flag that is true if this term is a fixed expression
	 */
	private boolean fixedExpression = false;
	
	/*
	 * The morphological components of this term
	 */
	private List<TermWord> termWords = Lists.newArrayList();
	
	Term(int id) {
		this.id = id;
	}
	
	Term(int id, String termId, List<TermWord> termWords, String spottingRule) {
		this(id);
		this.groupingKey = termId;
		this.spottingRule = spottingRule;
		this.termWords = termWords;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public void setPilot(String pilot) {
		this.pilot = pilot;
	}
	
	@Override
	public int compareTo(Term o) {
		return ComparisonChain.start()
				.compare(o.groupingKey.length(), this.groupingKey.length())
				.compare(o.groupingKey, this.groupingKey)
				.result();
	}
	
	@Override
	public int hashCode() {
		return groupingKey.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Term) 
			return this.groupingKey.equals(((Term) obj).groupingKey);
		else
			return false;
	}
	
	public void setDocumentFrequency(int documentFrequency) {
		this.documentFrequency = documentFrequency;
	}
	
	public String getGroupingKey() {
		return groupingKey;
	}
	
	@Override
	public String toString() {
		return this.groupingKey;
	}
	
	public boolean isSingleWord() {
		return termWords.size() == 1;
	}

	public boolean isMultiWord() {
		return termWords.size() > 1;
	}

	public String getPattern() {
		if(pattern == null) {
			List<String> labels = Lists.newArrayListWithCapacity(termWords.size());
			for(TermWord w:termWords) 
				labels.add(w.getSyntacticLabel());
			pattern = Joiner.on(' ').join(labels);
		}
		return pattern;
	}
	
	public List<TermWord> getWords() {
		return this.termWords;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public TermWord firstWord() {
		return this.termWords.get(0);
	}
	public boolean isCompound() {
		return isSingleWord() && firstWord().getWord().isCompound();
	}
	
	public int getId() {
		return id;
	}
	
	
	public String getSpottingRule() {
		return spottingRule;
	}
	
	public String getPilot() {
		return this.pilot;
	}
	
	
	/**
	 * Returns the concatenation of inner words' lemmas.
	 */
	public String getLemma() {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for(TermWord tw:this.getWords()) {
			if(i>0)
				builder.append(TermSuiteConstants.WHITESPACE);
			builder.append(tw.getWord().getLemma());
			i++;
		}
		return builder.toString();
	}
	

	public int getDocumentFrequency() {
		return this.documentFrequency;
	}
	
	public void normalize(CrossTable crossTable) {
		
	}
	
	public Number getValue() {
		return 0;
	}
	
	public void setFrequencyNorm(double normalizedTermFrequency) {
		this.normalizedTermFrequency = normalizedTermFrequency;
	}
	
	public void setGeneralFrequencyNorm(double normalizedGeneralTermFrequency) {
		this.normalizedGeneralTermFrequency = normalizedGeneralTermFrequency;
	}
	
	/**
	 * The average number of occurrences of this term in the 
	 * general language corpus for each slice of 1000 words.
	 * 
	 * @return
	 */
	public double getGeneralFrequencyNorm() {
		return normalizedGeneralTermFrequency;
	}
	
	/**
	 * The average number of occurrences of this term in the 
	 * corpus for each slice of 1000 words.
	 * 
	 * @return
	 */
	public double getFrequencyNorm() {
		return normalizedTermFrequency;
	}
	
	
	public int getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public double getSpecificity() {
		return specificity;
	}
	
	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}
	
	public void setFixedExpression(boolean fixedExpression) {
		this.fixedExpression = fixedExpression;
	}

	public boolean isFixedExpression() {
		return this.fixedExpression;
	}

	public double getTfIdf() {
		return tfIdf;
	}

	public void setTfIdf(double tfIdf) {
		this.tfIdf = tfIdf;
	}
	
	public ContextVector getContext() {
		return context;
	}
	
	public void setContext(ContextVector context) {
		this.context = context;
	}
}
