
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
import static org.assertj.core.api.Assertions.tuple;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import eu.project.ttc.models.RelationProperty;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.tools.utils.ControlFilesGenerator;
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
	
	public TermIndexAssert containsVariation(String baseGroupingKey, RelationType type, String variantGroupingKey) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;
		
		Term baseTerm = actual.getTermByGroupingKey(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations(baseTerm, type)) {
			if(tv.getTo().getGroupingKey().equals(variantGroupingKey))
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
	
	public TermIndexAssert containsVariation(String baseGroupingKey, RelationType type, String variantGroupingKey, RelationProperty p, Comparable<?> expectedValue) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;

		Term baseTerm = actual.getTermByGroupingKey(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations(baseTerm, type)) {
			if(java.util.Objects.equals(tv.getPropertyStringValue(p), expectedValue) 
					&& tv.getTo().getGroupingKey().equals(variantGroupingKey))
				return this;
		}
		
		failWithMessage("No such variation <%s--%s[%s=%s]--%s> found in term index", 
				baseGroupingKey, type, 
				p,
				expectedValue,
				variantGroupingKey
				);
		return this;
	}


	private Collection<TermRelation> getVariations() {
		return actual.getRelations().collect(Collectors.toSet());
	}

	public TermIndexAssert hasNVariationsOfType(int expected, RelationType type) {
		int cnt = 0;
		for(TermRelation tv:getVariations()) {
			if(tv.getType() == type)
				cnt++;
		}
	
		if(cnt != expected)
			failWithMessage("Expected <%s> variations of type <%s>. Got: <%s>", expected, type, cnt);
		
		return this;
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> asTermVariationsHavingRule(String ruleName) {
		Set<TermRelation> variations = Sets.newHashSet();
		for(TermRelation v:getVariations()) {
			if(Objects.equal(v.getPropertyStringValue(RelationProperty.VARIATION_RULE, null), ruleName))
				variations.add(v);
			
		}
		return assertThat(variations);
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> asTermVariations(RelationType... variations) {
		return assertThat(
				TermIndexUtils.selectTermVariations(actual, variations));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends Term>, Term> asCompoundList() {
		return assertThat(
				TermIndexUtils.selectCompounds(actual));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends String>, String> asMatchingRules() {
		Set<String> matchingRuleNames = Sets.newHashSet();
		for(TermRelation tv:TermIndexUtils.selectTermVariations(actual, RelationType.SYNTACTICAL, RelationType.MORPHOLOGICAL)) 
			matchingRuleNames.add(tv.getPropertyStringValue(RelationProperty.VARIATION_RULE));
		return assertThat(matchingRuleNames);
	}

	
	public TermIndexAssert hasAtLeastNVariationsOfType(Term base, int atLeastN, RelationType... vType) {
		isNotNull();
		int actualSize = actual.getOutboundRelations(base, vType).size();
		if (actualSize < atLeastN)
			failWithMessage(
					"Expected to find at least <%s> variations of type <%s> for term <%s>, but actually found <%s>",
					atLeastN, vType, base, actualSize);
		return this;
	}

	public TermIndexAssert hasNVariationsOfType(Term base, int n, RelationType... vType) {
		isNotNull();
		int actualSize = actual.getOutboundRelations(base, vType).size();
		if (actualSize != n)
			failWithMessage("Expected to find <%s> variations of type <%s> for term <%s>, but actually found <%s>", n,
					vType, base, actualSize);
		return this;
	}

	public TermIndexAssert hasAtLeastNBasesOfType(Term variant, int atLeastN, RelationType... vTypes) {
		isNotNull();
		int actualSize = actual.getInboundTermRelations(variant, vTypes).size();
		if (actualSize < atLeastN)
			failWithMessage("Expected to find at least <%s> bases <%s> for term <%s>, but actually found <%s>",
					atLeastN,
					(vTypes.length == 1 ? "of type " : "of any of these types ") + Joiner.on(" ").join(vTypes),
					variant, actualSize);
		return this;
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> getVariations(Term base) {
		return assertThat(actual.getOutboundRelations(base));
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> getVariationsOfType(Term base, RelationType... types) {
		return assertThat(actual.getOutboundRelations(base, types));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> getBases(Term variant) {
		return assertThat(actual.getInboundTermRelations(variant));
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> getBasesOfType(Term variant, RelationType... types) {
		return assertThat(actual.getInboundTermRelations(variant, types));
	}

	public TermIndexAssert hasNBases(Term variant, int expectedNumberOfBases) {
		Collection<TermRelation> bases = actual.getInboundTermRelations(variant);
		if(bases.size() != expectedNumberOfBases)
			failWithMessage("Expected <%s> bases but got <%s> (<%s>)", 
					expectedNumberOfBases,
					bases.size(),
					bases);
		return this;
	}
	
	public TermIndexAssert hasNVariations(Term base, int expectedNumberOfVariations) {
		Collection<TermRelation> variants = actual.getOutboundRelations(base);
		if(variants.size() != expectedNumberOfVariations)
			failWithMessage("Expected <%s> variations but got <%s> (<%s>)", 
					expectedNumberOfVariations,
					variants.size(),
					variants);
		return this;
	}

	public TermIndexAssert hasExpectedCompounds(Path diffFileIfFail, Tuple... expectedTuples) {
		CompoundTupleExtractor compoundTupleExtractor = new CompoundTupleExtractor();
		Set<Tuple> actualTuples = actual.getTerms().stream()
			.filter(Term::isCompound)
			.map(compoundTupleExtractor::extract)
			.collect(Collectors.toSet());
		return tupleDiff(diffFileIfFail, Sets.newHashSet(expectedTuples), actualTuples);
	}
	
	public TermIndexAssert tupleDiff(Path diffFileIfFail, Set<Tuple> expectedTuples, Set<Tuple> actualTuples) {
		Set<Tuple> notFound = Sets.newHashSet(expectedTuples);
		notFound.removeAll(actualTuples);
		
		Set<Tuple> notExpected = Sets.newHashSet(actualTuples);
		notExpected.removeAll(expectedTuples);

		Set<Tuple> foundAndExpected = Sets.newHashSet(expectedTuples);
		foundAndExpected.retainAll(actualTuples);
		
		if(!(notFound.isEmpty() && notExpected.isEmpty())) {
			
			diffFileIfFail.resolve("..").toFile().mkdirs();
			try(FileWriter writer = new FileWriter(diffFileIfFail.toFile())) {
				writer.write(String.format("Expected %d tuples, got %d.%nNum of tuples not found: %d%nNum of tuples not expected: %d%nNum of tuples found and expected: %d%n", 
					expectedTuples.size(),
					actualTuples.size(),
					notFound.size(),
					expectedTuples.size(),
					foundAndExpected.size()));
				
				writer.write(String.format("%n---------------------------------------------------------------%n"));
				writer.write(String.format("Tuples not found%n"));
				for(Tuple t:notFound) {
					writer.write(t.toString());
					writer.write("\n");
				};
				
				writer.write(String.format("%n---------------------------------------------------------------%n"));
				writer.write(String.format("Tuples not Expected%n"));
				for(Tuple t:notExpected) {
					writer.write(t.toString());
					writer.write("\n");
				};
				
				writer.write(String.format("%n---------------------------------------------------------------%n"));
				writer.write(String.format("Tuples found and expected%n"));
				for(Tuple t:foundAndExpected) {
					writer.write(t.toString());
					writer.write("\n");
				};

				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			
			failWithMessage("See diff file <%s>. Expected exact match of tuple sets. Expected num of tuples: <%s>, actual <%s>. Num of tuples not found: <%s>. Num of tuples not expected: <%s>", 
					diffFileIfFail.toString(),
					expectedTuples.size(),
					actualTuples.size(),
					notFound.size(),
					notExpected.size());
		} else 
			if(diffFileIfFail.toFile().exists())
				diffFileIfFail.toFile().delete();
			
		
		return this;
	}

	
	public static class CompoundTupleExtractor implements Extractor<Term, Tuple> {
		@Override
		public Tuple extract(Term compoundTerm) {
			return tuple(
					compoundTerm.getWords().get(0).getWord().getCompoundType().getShortName(),
					compoundTerm.getGroupingKey(),
					ControlFilesGenerator.toCompoundString(compoundTerm)
					);
		}
	}
}
