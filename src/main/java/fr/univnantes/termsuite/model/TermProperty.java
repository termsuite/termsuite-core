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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 
 * Available term properties that can be used for AE and script configuration
 * (especially in term index cleaning sorting and exporting operations)
 * 
 * @author Damien Cram
 *
 */
public enum TermProperty implements Property<Term> {
	RANK("rank", "rank", Integer.class, "The rank of the term assigned by TermSuite post-processor engine."),
	IS_SINGLE_WORD("isSingleWord", "isSwt", Boolean.class, "Wether this term is single-word or not."),
	DOCUMENT_FREQUENCY("documentFrequency", "dFreq", Integer.class, "The number of documents in corpus in which the term is occurring."),
	FREQUENCY_NORM("frequencyNorm", "fNorm", Double.class, "The number of occurrences of the term in the corpus every 1000 words."),
	GENERAL_FREQUENCY_NORM("generalFrequencyNorm", "gfNorm", Double.class, "The number of occurrences of the term in the general language corpus every 1000 words."),
	SPECIFICITY("specificity", "spec",Double.class, "The weirdness ratio, i.e. the specificity of the term in the corpus in comparison to general language."),
	FREQUENCY("frequency", "freq", Integer.class, "The number of occurrences of the term in the corpus."),
	ORTHOGRAPHIC_SCORE("OrthographicScore", "ortho", Double.class, "The probability for the covered text of the term for being an actual term assigned by TermSuite post-processor engine."),
	INDEPENDANT_FREQUENCY("IndependantFrequency", "iFreq", Integer.class, "The number of times a term occurrs in corpus as it is, i.e. not as any of its variant forms, assigned by TermSuite post-processor engine."),
	INDEPENDANCE("Independance", "ind", Double.class, "The `" + INDEPENDANT_FREQUENCY.getPropertyName() + "` divided by `" + FREQUENCY + "`, assigned by TermSuite post-processor engine."),
	PILOT("pilot", "pilot", String.class, "The most frequent form of the term."),
	LEMMA("lemma", "lem", String.class, "The concatenation of the term's word lemmas."),
	TF_IDF("tf-idf", "tfIdf", Double.class, "`" + FREQUENCY + "` divided by `" + DOCUMENT_FREQUENCY + "`."),
	SPEC_IDF("spec-idf", "specIdf", Double.class, "`" + SPECIFICITY + "` divided by `" + DOCUMENT_FREQUENCY + "`."),
	GROUPING_KEY("groupingKey", "key", String.class, "The unique id of the term, built on its pattern and its lemma."),
	PATTERN("pattern", "pattern", String.class, "The pattern of the term, i.e. the concatenation of syntactic labels of its words."),
	SPOTTING_RULE("spottingRule", "rule", String.class, "The name of the UIMA Tokens Regex spotting rule that found the term in the corpus."), 
	IS_FIXED_EXPRESSION("isFixedExpression", "isFixedExp", Boolean.class, "Wether the term is a fixed expression."), 
	SWT_SIZE("SwtSize", "swtSize", Integer.class, "The number of words composing the term that are single-words."), 
	FILTERED("Filtered", "isFiltered", Boolean.class, "Wether the term has been marked as filtered by TermSuite post-processor engine. Usually, such a term is not meant to be displayed."), 
	DEPTH("Depth", "depth", Integer.class,"The minimum level of extensions of the term starting from a single-word term."),
	;
	
	
	private PropertyHolderBase<TermProperty, Term> delegate;

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}


	private TermProperty(String propertyName, String jsonField, Class<?> range, String description) {
		delegate = new PropertyHolderBase<>(propertyName, jsonField, range, description);
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
		return (TermProperty) Property.forName(name, TermProperty.values());
	}

	public static Optional<TermProperty> forNameOptional(String name) {
		Optional<Property<Term>> opt = Property.forNameOptional(name, TermProperty.values());
		if(opt.isPresent())
			return Optional.of((TermProperty)opt.get());
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
