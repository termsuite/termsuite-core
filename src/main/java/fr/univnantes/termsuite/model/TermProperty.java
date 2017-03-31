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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
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
	RANK("rank", "#", "rank", Integer.class, "The rank of the term assigned by TermSuite post-processor engine."),
	IS_SINGLE_WORD("isSingleWord", "swt", "swt", Boolean.class, "Wether this term is single-word or not."),
	DOCUMENT_FREQUENCY("documentFrequency", "dfreq", "dfreq", Integer.class, "The number of documents in corpus in which the term is occurring."),
	FREQUENCY_NORM("frequencyNorm", "fnorm", "f_norm", Double.class, "The number of occurrences of the term in the corpus every 1000 words."),
	GENERAL_FREQUENCY_NORM("generalFrequencyNorm", "generalFnorm", "gf_norm", Double.class, "The number of occurrences of the term in the general language corpus every 1000 words."),
	SPECIFICITY("specificity", "sp", "spec", Double.class, "The weirdness ratio, i.e. the specificity of the term in the corpus in comparison to general language."),
	FREQUENCY("frequency", "f", "freq", Integer.class, "The number of occurrences of the term in the corpus."),
	ORTHOGRAPHIC_SCORE("OrthographicScore", "ortho", "ortho", Double.class, "The probability for the covered text of the term for being an actual term assigned by TermSuite post-processor engine."),
	INDEPENDANT_FREQUENCY("IndependantFrequency", "iFreq", "ifreq", Integer.class, "The number of times a term occurrs in corpus as it is, i.e. not as any of its variant forms, assigned by TermSuite post-processor engine."),
	INDEPENDANCE("Independance", "ind", "ind", Double.class, "The `" + INDEPENDANT_FREQUENCY.getPropertyName() + "` divided by `" + FREQUENCY + "`, assigned by TermSuite post-processor engine."),
	PILOT("pilot", "pilot", "pilot", String.class, "The most frequent form of the term."),
	LEMMA("lemma", "lm", "lemma", String.class, "The concatenation of the term's word lemmas."),
	TF_IDF("tf-idf", "tfidf", "tfidf", Double.class, "`" + FREQUENCY + "` divided by `" + DOCUMENT_FREQUENCY + "`."),
	SPEC_IDF("spec-idf", "specidf", "specidf", Double.class, "`" + SPECIFICITY + "` divided by `" + DOCUMENT_FREQUENCY + "`."),
	GROUPING_KEY("groupingKey", "gkey", "key", String.class, "The unique id of the term, built on its pattern and its lemma."),
	PATTERN("pattern", "p", "pattern", String.class, "The pattern of the term, i.e. the concatenation of syntactic labels of its words."),
	SPOTTING_RULE("spottingRule", "rule", "rule", String.class, "The name of the UIMA Tokens Regex spotting rule that found the term in the corpus."), 
	IS_FIXED_EXPRESSION("isFixedExpression", "fixedExp", "fixed_exp", Boolean.class, "Wether the term is a fixed expression."), 
	SWT_SIZE("SwtSize", "swtSize", "swtSize", Integer.class, "The number of words composing the term that are single-words."), 
	FILTERED("Filtered", "filtered", "filtered", Boolean.class, "Wether the term has been marked as filtered by TermSuite post-processor engine. Usually, such a term is not meant to be displayed."), 
	DEPTH("Depth", "depth", "depth", Integer.class,"The minimum level of extensions of the term starting from a single-word term."),
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
	
	private PropertyHolderBase<TermProperty, Term> delegate;

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}


	private TermProperty(String propertyName, String propertyShortName, String jsonField, Class<?> range, String description) {
		delegate = new PropertyHolderBase<>(propertyName, propertyShortName, jsonField, range, description);
	}
	

	@Override
	public String getPropertyName() {
		return delegate.getPropertyName();
	}


	@Override
	public String getJsonField() {
		return delegate.getJsonField();
	}


	@Override
	public Class<?> getRange() {
		return delegate.getRange();
	}


	@Override
	public String getShortName() {
		return delegate.getShortName();
	}


	@Override
	public boolean isNumeric() {
		return delegate.isNumeric();
	}


	@Override
	public boolean isDecimalNumber() {
		return delegate.isDecimalNumber();
	}


	@Override
	public Comparator<Term> getComparator() {
		return delegate.getComparator(this);
	}


	@Override
	public Comparator<Term> getComparator(boolean reverse) {
		return delegate.getComparator(this, reverse);
	}

	@Override
	public int compare(Term o1, Term o2) {
		return delegate.compare(this, o1, o2);
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

	public static Optional<TermProperty> forNameOptional(String name) {
		TermProperty termProperty = byNames.get(name);
		if(termProperty != null)
			return Optional.of(termProperty);
		else
			return Optional.empty();
	}

	public static TermProperty fromJsonString(String field) {
		return PropertyHolderBase.fromJsonString(TermProperty.class, field);
	}

	public static Stream<TermProperty> numberValues() {
		return stream().filter(p -> Number.class.isAssignableFrom(p.getRange()));
	}


	public static Stream<TermProperty> stream() {
		return Arrays.stream(values());
	}
	
}
