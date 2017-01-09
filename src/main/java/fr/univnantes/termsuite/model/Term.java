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

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.utils.TermSuiteConstants;


public class Term extends PropertyHolder<TermProperty> implements Comparable<Term> {
	
	private ContextVector context;
	
	/*
	 * The morphological components of this term
	 */
	private List<TermWord> termWords = Lists.newArrayList();
	
	Term(String groupingKey, List<TermWord> termWords) {
		super(TermProperty.class);
		this.termWords = termWords;
		setProperty(TermProperty.GROUPING_KEY, groupingKey);
	}

	public ContextVector getContext() {
		return context;
	}
	
	public void setContext(ContextVector context) {
		this.context = context;
	}
	
	public List<TermWord> getWords() {
		return this.termWords;
	}
		
	@Override
	public int compareTo(Term o) {
		return ComparisonChain.start()
				.compare(o.getGroupingKey().length(), this.getGroupingKey().length())
				.compare(o.getGroupingKey(), this.getGroupingKey())
				.result();
	}
	
	@Override
	public int hashCode() {
		return getGroupingKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Term) 
			return this.getGroupingKey().equals(((Term) obj).getGroupingKey());
		else
			return false;
	}
	
	@Override
	public String toString() {
		return this.getGroupingKey();
	}
	
	public boolean isSingleWord() {
		return termWords.size() == 1;
	}
	
	public boolean isMultiWord() {
		return termWords.size() > 1;
	}
	
	public boolean isCompound() {
		return isSingleWord() && this.termWords.get(0).getWord().isCompound();
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


	/* 
	 * *******************************************************************************
	 * PROPERTY GETTERS/SETTERS
	 * *******************************************************************************
	 */
	

	/*
	 * GROUPING_KEY
	 */
	public String getGroupingKey() {
		return getPropertyStringValue(TermProperty.GROUPING_KEY);		
	}
	
	/*
	 * DOCUMENT_FREQUENCY
	 */
	public Integer getDocumentFrequency() {
		return getPropertyIntegerValue(TermProperty.DOCUMENT_FREQUENCY);
	}

	public void setDocumentFrequency(int documentFrequency) {
		setProperty(TermProperty.DOCUMENT_FREQUENCY, documentFrequency);
	}
	
	/*
	 * FREQUENCY
	 */
	public Integer getFrequency() {
		return getPropertyIntegerValue(TermProperty.FREQUENCY);		
	}

	public void setFrequency(int frequency) {
		setProperty(TermProperty.FREQUENCY, frequency);
	}
	
	/*
	 * PATTERN
	 */
	public String getPattern() {
		return getPropertyStringValue(TermProperty.PATTERN);		
	}
	
	public void setPattern(String pattern) {
		setProperty(TermProperty.PATTERN, pattern);
	}
	
	/*
	 * PILOT
	 */
	public String getPilot() {
		return getPropertyStringValue(TermProperty.PILOT);
	}
	public void setPilot(String pilot) {
		setProperty(TermProperty.PILOT, pilot);
	}

	/*
	 * SPOTTING_RULE
	 */
	public String getSpottingRule() {
		return getPropertyStringValue(TermProperty.SPOTTING_RULE);		
	}

	public void setSpottingRule(String spottingRule) {
		setProperty(TermProperty.SPOTTING_RULE, spottingRule);
	}
	
	/*
	 * GENERAL_FREQUENCY_NORM
	 */
	public Double getGeneralFrequencyNorm() {
		return getPropertyDoubleValue(TermProperty.GENERAL_FREQUENCY_NORM);
	}
	
	public void setGeneralFrequencyNorm(double normalizedGeneralTermFrequency) {
		setProperty(TermProperty.GENERAL_FREQUENCY_NORM, normalizedGeneralTermFrequency);
	}
	
	/*
	 * FREQUENCY_NORM
	 */
	public Double getFrequencyNorm() {
		return getPropertyDoubleValue(TermProperty.FREQUENCY_NORM);
	}
	
	public void setFrequencyNorm(double normalizedTermFrequency) {
		setProperty(TermProperty.FREQUENCY_NORM, normalizedTermFrequency);
	}
	
	/*
	 * RANK
	 */
	public Integer getRank() {
		return getPropertyIntegerValue(TermProperty.RANK);
	}
	
	public void setRank(int rank) {
		setProperty(TermProperty.RANK, rank);
	}
	
	/*
	 * SPECIFICITY
	 */
	public Double getSpecificity() {
		return getPropertyDoubleValue(TermProperty.SPECIFICITY);
	}
	
	public void setSpecificity(double specificity) {
		setProperty(TermProperty.SPECIFICITY, specificity);
	}
	
	/*
	 * IS_FIXED_EXPRESSION
	 */
	public Boolean isFixedExpression() {
		return getPropertyBooleanValue(TermProperty.IS_FIXED_EXPRESSION);
	}
	
	public void setFixedExpression(boolean fixedExpression) {
		setProperty(TermProperty.IS_FIXED_EXPRESSION, fixedExpression);
	}

	/*
	 * TF_IDF
	 */
	public Double getTfIdf() {
		return getPropertyDoubleValue(TermProperty.TF_IDF);
	}

	public void setTfIdf(double tfIdf) {
		setProperty(TermProperty.TF_IDF, tfIdf);
	}

	public Number getPropertyNumberValue(TermProperty p) {
		return (Number)get(p);
	}

	public void setDepth(int depth) {
		setProperty(TermProperty.DEPTH, depth);
	}
	
	public Integer getDepth() {
		return getPropertyIntegerValue(TermProperty.DEPTH);
	}
}
