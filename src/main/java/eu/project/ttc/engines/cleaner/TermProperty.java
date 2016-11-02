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
package eu.project.ttc.engines.cleaner;

import java.util.Comparator;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;

import eu.project.ttc.models.Term;

/**
 * 
 * Available term properties that can be used for AE and script configuration
 * (especially in term index cleaning sorting and exporting operations)
 * 
 * @author Damien Cram
 *
 */
public enum TermProperty {
	RANK("rank", "#", Integer.class),
	DOCUMENT_FREQUENCY("documentFrequency", "dfreq", Integer.class),
	FREQUENCY_NORM("frequencyNorm", "fnorm", Double.class),
	GENERAL_FREQUENCY_NORM("generalFrequencyNorm", "generalFnorm", Double.class),
	SPECIFICITY("specificity", "sp", Double.class),
	FREQUENCY("frequency", "f", Integer.class),
	PILOT("pilot", "pilot", String.class),
	LEMMA("lemma", "lm", String.class),
	TF_IDF("tf-idf", "tfidf", Double.class),
	GROUPING_KEY("groupingKey", "gkey", String.class),
	PATTERN("pattern", "p", String.class),
	SPOTTING_RULE("spottingRule", "rule", String.class),
	;
	
	private static Map<String, TermProperty> byNames = Maps.newHashMap();
	
	static {
		for(TermProperty p:TermProperty.values()) {
			byNames.put(p.getShortName(), p);
			byNames.put(p.getShortName().toLowerCase(), p);
			byNames.put(p.getShortName().toUpperCase(), p);
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

	private TermProperty(String propertyName, String propertyShortName, Class<?> range) {
		this.propertyName = propertyName;
		this.propertyShortName = propertyShortName;
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
		

	public double getDoubleValue(Term t) {
		switch(this) {
		case DOCUMENT_FREQUENCY:
			return t.getDocumentFrequency();
		case FREQUENCY:
			return t.getFrequency();
		case TF_IDF:
			return t.getTfIdf();
		case SPECIFICITY:
			return t.getSpecificity();
		case GENERAL_FREQUENCY_NORM:
			return t.getGeneralFrequencyNorm();
		case FREQUENCY_NORM:
			return t.getFrequencyNorm();
		default:
			throw new UnsupportedOperationException("No double value for property: " + this);
		}
	}

	public Comparable<?> getValue(Term t) {
		switch(this) {
		case PILOT:
			return t.getPilot();
		case DOCUMENT_FREQUENCY:
			return t.getDocumentFrequency();
		case RANK:
			return t.getRank();
		case SPECIFICITY:
			return t.getSpecificity();
		case FREQUENCY:
			return t.getFrequency();
		case TF_IDF:
			return t.getTfIdf();
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
