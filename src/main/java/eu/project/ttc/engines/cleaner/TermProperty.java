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

/**
 * 
 * Available term properties that can be used for AE and script configuration
 * (especially in term index cleaning sorting and exporting operations)
 * 
 * @author Damien Cram
 *
 */
public enum TermProperty {
	DOCUMENT_FREQUENCY("documentFrequency", "dfreq", false),
	WR("wr", "wr", true),
	WR_LOG("wrLog", "wrlog", true),
	WR_LOG_Z_SCORE("wrLogZScore", "zscore", true),
	FREQUENCY("frequency", "f", false),
	PILOT("pilot", "pilot", false),
	LEMMA("lemma", "lemma", false),
	GROUPING_KEY("groupingKey", "gkey", false),
	PATTERN("pattern", "p", false),
	SPOTTING_RULE("spottingRule", "rule", false),
	TERM_CLASS_HEAD("termClassHead", "cls", false),
	TERM_CLASS_FREQUENCY("termClassFrequency", "clsfreq", false)
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
	private boolean measure;

	private TermProperty(String propertyName, String propertyShortName, boolean isMeasure) {
		this.propertyName = propertyName;
		this.propertyShortName = propertyShortName;
		this.measure = isMeasure;
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
		default:
			return getValue(t);
		}
	}

	public Comparable<?> getValue(Term t) {
		switch(this) {
		case WR:
		case WR_LOG:
		case WR_LOG_Z_SCORE:
			throw new IllegalStateException("This property is a termIndex measure. Should use #getValue(TermIndex termIndex, Term t) instead.");
		case DOCUMENT_FREQUENCY:
			return t.getDocumentFrequency();
		case FREQUENCY:
			return t.getFrequency();
		case LEMMA:
			return t.getLemma();
		case GROUPING_KEY:
			return t.getGroupingKey();
		case PILOT:
			return t.getPilot();
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
