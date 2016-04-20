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
package eu.project.ttc.engines.cleaner;

import java.util.Comparator;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.utils.TermUtils;

/**
 * 
 * Available term properties that can be used for AE and script configuration
 * (especially in term index cleaning sorting and exporting operations)
 * 
 * @author Damien Cram
 *
 */
public enum TermProperty {
	RANK("rank", "#", false, Integer.class),
	DOCUMENT_FREQUENCY("documentFrequency", "dfreq", false, Integer.class),
	FREQUENCY_NORM("frequencyNorm", "fnorm", false, Double.class),
	GENERAL_FREQUENCY_NORM("generalFrequencyNorm", "generalFnorm", false, Double.class),
	SPECIFICITY("specificity", "sp", true, Double.class),
	WR("wr", "wr", true, Double.class),
	WR_LOG("wrLog", "wrlog", true, Double.class),
	WR_LOG_Z_SCORE("wrLogZScore", "zscore", true, Double.class),
	FREQUENCY("frequency", "f", false, Integer.class),
	PILOT("pilot", "pilot", false, String.class),
	LEMMA("lemma", "lemma", false, String.class),
	GROUPING_KEY("groupingKey", "gkey", false, String.class),
	PATTERN("pattern", "p", false, String.class),
	SPOTTING_RULE("spottingRule", "rule", false, String.class),
	TERM_CLASS_HEAD("termClassHead", "cls", false, Term.class),
	TERM_CLASS_FREQUENCY("termClassFrequency", "clsfreq", false, Integer.class)
	;
	
	private static Map<String, TermProperty> byNames = Maps.newHashMap();
	
	static {
		for(TermProperty p:TermProperty.values()) {
			byNames.put(p.getPropertyName(), p);
			byNames.put(p.getPropertyName().toLowerCase(), p);
			byNames.put(p.getPropertyName().toUpperCase(), p);
			byNames.put(p.toString(), p);
			byNames.put(p.toString().toLowerCase(), p);
		}
	}
	
	private String propertyName;
	private String propertyShortName;
	private Class<?> range;
	private boolean measure;

	private TermProperty(String propertyName, String propertyShortName, boolean isMeasure, Class<?> range) {
		this.propertyName = propertyName;
		this.propertyShortName = propertyShortName;
		this.measure = isMeasure;
		this.range = range;
	}
	
	public Class<?> getRange() {
		return range;
	}
	
	public boolean isDecimalNumber() {
		return getRange().equals(Double.class) || getRange().equals(Float.class);
	}
	
	public boolean isNumber() {
		return Number.class.isAssignableFrom(getRange());
	}

	
	public boolean isMeasure() {
		return this.measure;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Comparator<Term> getComparator(final boolean reverse) {
		return new Comparator<Term>() {
			@Override
			public int compare(Term o1, Term o2) {
				return reverse ? TermProperty.this.compare(o2, o1) : TermProperty.this.compare(o1, o2) ;
			}
		};
	}
	
	public Comparator<Term> getComparator(final TermIndex termIndex, final boolean reverse) {
		return new Comparator<Term>() {
			@Override
			public int compare(Term o1, Term o2) {
				return reverse ? TermProperty.this.compare(termIndex, o2, o1) : TermProperty.this.compare(termIndex, o1, o2) ;
			}
		};
	}

	/**
	 * The "compare" method that can apply on property measures.
	 * 
	 * @param termIndex
	 * @param o1
	 * @param o2
	 * @return
	 */
	public int compare(TermIndex termIndex, Term o1, Term o2) {
		return ComparisonChain.start().compare(getValue(termIndex, o1), getValue(termIndex, o2)).result();
	}


	public int compare(Term o1, Term o2) {
		return ComparisonChain.start().compare(getValue(o1), getValue(o2)).result();
	}
		

	public double getDoubleValue(TermIndex termIndex, Term t) {
		switch(this) {
		case WR:
			return (double)termIndex.getWRMeasure().getValue(t);
		case WR_LOG:
			return (double)termIndex.getWRLogMeasure().getValue(t);
		case WR_LOG_Z_SCORE:
			return (double)termIndex.getWRLogMeasure().getZScore(t);
		default:
			return getDoubleValue(t);
		}

	}
	
	public double getDoubleValue(Term t) {
		switch(this) {
		case DOCUMENT_FREQUENCY:
			return t.getDocumentFrequency();
		case FREQUENCY:
			return t.getFrequency();
		case GENERAL_FREQUENCY_NORM:
			return t.getGeneralFrequencyNorm();
		case FREQUENCY_NORM:
			return t.getFrequencyNorm();
		case TERM_CLASS_FREQUENCY:
			return t.getTermClass().getFrequency();
		default:
			if(measure)
				throw new IllegalStateException("No double value for property: " + this);
				
			throw new UnsupportedOperationException("No double value for property: " + this);
		}
	}
	
	public Comparable<?> getValue(TermIndex termIndex, Term t) {
		switch(this) {
		case WR:
			return termIndex.getWRMeasure().getValue(t);
		case WR_LOG:
			return termIndex.getWRLogMeasure().getValue(t);
		case WR_LOG_Z_SCORE:
			return termIndex.getWRLogMeasure().getZScore(t);
		case PILOT:
			/*
			 * Not optimal at all. Use TermFormGetter
			 */
			return TermUtils.formGetter(termIndex, true).getPilot(t);
		default:
			return getValue(t);
		}
	}

	public Comparable<?> getValue(Term t) {
		switch(this) {
		case WR:
		case WR_LOG:
		case WR_LOG_Z_SCORE:
		case PILOT:
			throw new IllegalStateException("Should use #getValue(TermIndex termIndex, Term t) instead.");
		case DOCUMENT_FREQUENCY:
			return t.getDocumentFrequency();
		case RANK:
			return t.getRank();
		case SPECIFICITY:
			return t.getSpecificity();
		case FREQUENCY:
			return t.getFrequency();
		case LEMMA:
			return t.getLemma();
		case GENERAL_FREQUENCY_NORM:
			return t.getGeneralFrequencyNorm();
		case FREQUENCY_NORM:
			return t.getFrequencyNorm();
		case GROUPING_KEY:
			return t.getGroupingKey();
		case PATTERN:
			return t.getPattern();
		case SPOTTING_RULE:
			return t.getSpottingRule();
		case TERM_CLASS_HEAD:
			return t.getTermClass().getHead();
		case TERM_CLASS_FREQUENCY:
			return t.getTermClass().getFrequency();
		}
		throw new IllegalStateException("Unexpected property: " + this);
	}

	public static TermProperty forName(String name) {
		TermProperty termProperty = byNames.get(name);
		if(termProperty != null)
			return termProperty;
		else
			throw new IllegalArgumentException(
				String.format(
						"Bad term property name: %s. Allowed: %s", 
						name,
						Joiner.on(',').join(TermProperty.values())
				)
		);
	}

	public String getShortName() {
		return propertyShortName;
	}
}
