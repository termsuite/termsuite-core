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

import com.google.common.collect.Lists;


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
	public int hashCode() {
		return getString(TermProperty.GROUPING_KEY).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Term) 
			return this.getString(TermProperty.GROUPING_KEY).equals(((Term) obj).getString(TermProperty.GROUPING_KEY));
		else
			return false;
	}
	
	@Override
	public String toString() {
		return this.getString(TermProperty.GROUPING_KEY);
	}
	
	@Override
	public int compareTo(Term o) {
		return getGroupingKey().compareTo(o.getGroupingKey());
	}

//	private Integer rank;
//	private Boolean singleWord;
//	private Integer documentFrequency;
//	private Double frequencyNorm;
//	private Double generalFrequencyNorm;
//	private Double specificity;
//	private Integer frequency;
//	private Double orthographicScore;
//	private Integer independantFrequency;
//	private Double independance;
//	private String pilot;
//	private String lemma;
//	private Double tfIdf;
//	private String groupingKey;
//	private String spottingRule;
//	private Boolean fixedExpression;
//	private Integer swtSize;
//	private Boolean filtered;
//	private Integer depth;

	public Integer getRank() {
		return getIntegerUnchecked(TermProperty.RANK);
	}

	public Boolean getSingleWord() {
		return getBooleanUnchecked(TermProperty.IS_SINGLE_WORD);
	}

	public Integer getDocumentFrequency() {
		return getIntegerUnchecked(TermProperty.DOCUMENT_FREQUENCY);
	}

	public Double getFrequencyNorm() {
		return getDoubleUnchecked(TermProperty.FREQUENCY_NORM);
	}

	public Double getGeneralFrequencyNorm() {
		return getDoubleUnchecked(TermProperty.GENERAL_FREQUENCY_NORM);
	}

	public Double getSpecificity() {
		return getDoubleUnchecked(TermProperty.SPECIFICITY);
	}

	public Integer getFrequency() {
		return getIntegerUnchecked(TermProperty.FREQUENCY);
	}

	public Double getOrthographicScore() {
		return getDoubleUnchecked(TermProperty.ORTHOGRAPHIC_SCORE);
	}

	public Integer getIndependantFrequency() {
		return getIntegerUnchecked(TermProperty.INDEPENDANT_FREQUENCY);
	}

	public Double getIndependance() {
		return getDoubleUnchecked(TermProperty.INDEPENDANCE);
	}

	public String getPilot() {
		return getStringUnchecked(TermProperty.PILOT);
	}

	public String getLemma() {
		return getStringUnchecked(TermProperty.LEMMA);
	}

	public Double getTfIdf() {
		return getDoubleUnchecked(TermProperty.TF_IDF);
	}

	public String getGroupingKey() {
		return getStringUnchecked(TermProperty.GROUPING_KEY);
	}

	public String getPattern() {
		return getStringUnchecked(TermProperty.PATTERN);
	}

	public String getSpottingRule() {
		return getStringUnchecked(TermProperty.SPOTTING_RULE);
	}

	public Boolean isFixedExpression() {
		return getBooleanUnchecked(TermProperty.IS_FIXED_EXPRESSION);
	}

	public Integer getSwtSize() {
		return getIntegerUnchecked(TermProperty.SWT_SIZE);
	}

	public Boolean isFiltered() {
		return getBooleanUnchecked(TermProperty.FILTERED);
	}

	public Integer getDepth() {
		return getIntegerUnchecked(TermProperty.DEPTH);
	}

	public Number getNumber(TermProperty property) {
		return (Number)getPropertyValue(property);
	}

	
}
