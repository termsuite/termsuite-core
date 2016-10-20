
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

package eu.project.ttc.test.func;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.utils.TermIndexUtils;

public class TermIndexAssert extends AbstractAssert<TermIndexAssert, TermIndex> {

	public TermIndexAssert(TermIndex actual) {
		super(actual, TermIndexAssert.class);
	}

	public TermIndexAssert hasSize(int expected) {
		if(actual.getTerms().size() != expected)
			failWithMessage("Expected size was <%s>, but actual size is <%s>.", 
					expected, actual.getTerms().size());
		return this;
	}

	public TermIndexAssert containsTerm(String expectedTerm, int frequency) {
		for(Term t:actual.getTerms()) {
			if(t.getGroupingKey().equals(expectedTerm)) {
				if(t.getFrequency() != frequency)
					failWithMessage("Expected frequency for term %s was <%s>, but actually is: <%s>.",
							expectedTerm,
							frequency,
							t.getFrequency());
				return this;
			}
		}
		failWithMessage("No such term <%s> found in term index.", expectedTerm);
		return this;
	}
	
	public TermIndexAssert containsTerm(String expectedTerm) {
		for(Term t:actual.getTerms()) {
			if(t.getGroupingKey().equals(expectedTerm))
				return this;
		}
		
		failWithMessage("No such term <%s> found in term index.", expectedTerm);
		return this;
	}
	
	public TermIndexAssert containsVariation(String baseGroupingKey, VariationType type, String variantGroupingKey) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;
		
		Term baseTerm = actual.getTermByGroupingKey(baseGroupingKey);
		for(TermVariation tv:actual.getOutboundTermVariations(baseTerm, type)) {
			if(tv.getVariant().getGroupingKey().equals(variantGroupingKey))
				return this;
		}
		
		failWithMessage("No such variation <%s--%s[%s]--%s> found in term index", 
				baseGroupingKey, type, 
				info,
				variantGroupingKey
				);
		return this;
	}

	private boolean failToFindTerms(String... groupingKeys) {
		boolean failed = false;
		for(String gKey:groupingKeys) {
			if(actual.getTermByGroupingKey(gKey) == null) {
				failed = true;
				failWithMessage("Could not find term <%s> in termIndex", gKey);
			}
		}
		return failed;
	}
	
	public TermIndexAssert containsVariation(String baseGroupingKey, VariationType type, String variantGroupingKey, Object info) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;

		Term baseTerm = actual.getTermByGroupingKey(baseGroupingKey);
		for(TermVariation tv:actual.getOutboundTermVariations(baseTerm, type)) {
			if(java.util.Objects.equals(tv.getInfo(), info) 
					&& tv.getVariant().getGroupingKey().equals(variantGroupingKey))
				return this;
		}
		
		failWithMessage("No such variation <%s--%s[%s]--%s> found in term index", 
				baseGroupingKey, type, 
				info,
				variantGroupingKey
				);
		return this;
	}


	private Collection<TermVariation> getVariations() {
		return actual.getTermVariations();
	}

	public TermIndexAssert hasNVariationsOfType(int expected, VariationType type) {
		int cnt = 0;
		for(TermVariation tv:getVariations()) {
			if(tv.getVariationType() == type)
				cnt++;
		}
	
		if(cnt != expected)
			failWithMessage("Expected <%s> variations of type <%s>. Got: <%s>", expected, type, cnt);
		
		return this;
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> asTermVariationsHavingObject(Object object) {
		Set<TermVariation> variations = Sets.newHashSet();
		for(TermVariation v:getVariations())
			if(Objects.equal(v.getInfo(), object))
				variations.add(v);
		return assertThat(variations);
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> asTermVariations(VariationType... variations) {
		return assertThat(
				TermIndexUtils.selectTermVariations(actual, variations));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends Term>, Term> asCompoundList() {
		return assertThat(
				TermIndexUtils.selectCompounds(actual));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends String>, String> asMatchingRules() {
		Set<String> matchingRuleNames = Sets.newHashSet();
		for(TermVariation tv:TermIndexUtils.selectTermVariations(actual, VariationType.SYNTACTICAL, VariationType.MORPHOLOGICAL)) 
			matchingRuleNames.add((String)tv.getInfo());
		return assertThat(matchingRuleNames);
	}

	
	public TermIndexAssert hasAtLeastNVariationsOfType(Term base, int atLeastN, VariationType... vType) {
		isNotNull();
		int actualSize = actual.getOutboundTermVariations(base, vType).size();
		if (actualSize < atLeastN)
			failWithMessage(
					"Expected to find at least <%s> variations of type <%s> for term <%s>, but actually found <%s>",
					atLeastN, vType, base, actualSize);
		return this;
	}

	public TermIndexAssert hasNVariationsOfType(Term base, int n, VariationType... vType) {
		isNotNull();
		int actualSize = actual.getOutboundTermVariations(base, vType).size();
		if (actualSize != n)
			failWithMessage("Expected to find <%s> variations of type <%s> for term <%s>, but actually found <%s>", n,
					vType, base, actualSize);
		return this;
	}

	public TermIndexAssert hasAtLeastNBasesOfType(Term variant, int atLeastN, VariationType... vTypes) {
		isNotNull();
		int actualSize = actual.getInboundTermVariations(variant, vTypes).size();
		if (actualSize < atLeastN)
			failWithMessage("Expected to find at least <%s> bases <%s> for term <%s>, but actually found <%s>",
					atLeastN,
					(vTypes.length == 1 ? "of type " : "of any of these types ") + Joiner.on(" ").join(vTypes),
					variant, actualSize);
		return this;
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getVariations(Term base) {
		return assertThat(actual.getOutboundTermVariations(base));
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getVariationsOfType(Term base, VariationType... types) {
		return assertThat(actual.getOutboundTermVariations(base, types));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getBases(Term variant) {
		return assertThat(actual.getInboundTermVariations(variant));
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getBasesOfType(Term variant, VariationType... types) {
		return assertThat(actual.getInboundTermVariations(variant, types));
	}

	public TermIndexAssert hasNBases(Term variant, int expectedNumberOfBases) {
		Collection<TermVariation> bases = actual.getInboundTermVariations(variant);
		if(bases.size() != expectedNumberOfBases)
			failWithMessage("Expected <%s> bases but got <%s> (<%s>)", 
					expectedNumberOfBases,
					bases.size(),
					bases);
		return this;
	}
	
	public TermIndexAssert hasNVariations(Term base, int expectedNumberOfVariations) {
		Collection<TermVariation> variants = actual.getOutboundTermVariations(base);
		if(variants.size() != expectedNumberOfVariations)
			failWithMessage("Expected <%s> variations but got <%s> (<%s>)", 
					expectedNumberOfVariations,
					variants.size(),
					variants);
		return this;
	}
}
