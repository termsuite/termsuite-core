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

import java.util.Comparator;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;

/**
 * 
 * Available term properties that can be used for AE and script configuration
 * (especially in term index cleaning sorting and exporting operations)
 * 
 * @author Damien Cram
 *
 */
public enum TermProperty implements Property<Term> {
	RANK("rank", "#", "rank", Integer.class),
	IS_SINGLE_WORD("isSingleWord", "swt", "swt", Boolean.class),
	DOCUMENT_FREQUENCY("documentFrequency", "dfreq", "dfreq", Integer.class),
	FREQUENCY_NORM("frequencyNorm", "fnorm", "f_norm", Double.class),
	GENERAL_FREQUENCY_NORM("generalFrequencyNorm", "generalFnorm", "gf_norm", Double.class),
	SPECIFICITY("specificity", "sp", "spec", Double.class),
	FREQUENCY("frequency", "f", "freq", Integer.class),
	ORTHOGRAPHIC_SCORE("OrthographicScore", "ortho", "ortho", Double.class),
	INDEPENDANT_FREQUENCY("IndependantFrequency", "iFreq", "ifreq", Integer.class),
	INDEPENDANCE("Independance", "ind", "ind", Double.class),
	PILOT("pilot", "pilot", "pilot", String.class),
	LEMMA("lemma", "lm", "lemma", String.class),
	TF_IDF("tf-idf", "tfidf", "tfidf", Double.class),
	GROUPING_KEY("groupingKey", "gkey", "key", String.class),
	PATTERN("pattern", "p", "pattern", String.class),
	SPOTTING_RULE("spottingRule", "rule", "rule", String.class), 
	IS_FIXED_EXPRESSION("isFixedExpression", "fixedExp", "fixed_exp", Boolean.class), 
	SWT_SIZE("SwtSize", "swtSize", "swtSize", Integer.class), 
	FILTERED("Filtered", "filtered", "filtered", Boolean.class),
	
	;
	
	private static Map<String, TermProperty> byNames = Maps.newConcurrentMap();
	
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
	private String jsonField;
	private Class<?> range;

	private TermProperty(String propertyName, String propertyShortName, String propertyJsonName, Class<?> range) {
		this.propertyName = propertyName;
		this.propertyShortName = propertyShortName;
		this.jsonField = propertyJsonName;
		this.range = range;
	}
	
	@Override
	public Class<?> getRange() {
		return range;
	}
	
	@Override
	public boolean isDecimalNumber() {
		return Property.isDecimalNumber(range);
	}
	
	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public Comparator<Term> getComparator(final boolean reverse) {
		return new Comparator<Term>() {
			@Override
			public int compare(Term o1, Term o2) {
				return reverse ? TermProperty.this.compare(o2, o1) : TermProperty.this.compare(o1, o2) ;
			}
		};
	}
	

	@Override
	public int compare(Term o1, Term o2) {
		if(o1.getPropertyValueUnchecked(this) == o2.getPropertyValueUnchecked(this))
			return 0;
		else if(o1.getPropertyValueUnchecked(this)==null)
			return -1;
		else if(o2.getPropertyValueUnchecked(this)==null)
			return 1;
		else
			return ComparisonChain.start()
				.compare(
						o1.getPropertyValueUnchecked(this), 
						o2.getPropertyValueUnchecked(this))
				.result();
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

	@Override
	public String getShortName() {
		return propertyShortName;
	}

	@Override
	public boolean isNumeric() {
		return Property.isNumeric(range);
	}

	@Override
	public String getJsonField() {
		return jsonField;
	}

	@Override
	public Comparator<Term> getComparator() {
		return getComparator(false);
	}

	public static TermProperty fromJsonString(String field) {
		Preconditions.checkNotNull(field);
		for(TermProperty p:values())
			if(p.jsonField.equals(field))
				return p;
		throw new IllegalArgumentException("No TermProperty with such json field: " + field);
	}
}
