
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

package fr.univnantes.termsuite.test.func;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.tools.ControlFilesGenerator;

public class TerminologyAssert extends AbstractAssert<TerminologyAssert, Terminology> {

	public TerminologyAssert(Terminology actual) {
		super(actual, TerminologyAssert.class);
	}

	public TerminologyAssert hasNTerms(int expected) {
		if(actual.getTerms().size() != expected)
			failWithMessage("Expected size was <%s>, but actual size is <%s>.", 
					expected, actual.getTerms().size());
		return this;
	}

	public TerminologyAssert containsTerm(String expectedTerm, int frequency) {
		for(Term t:actual.getTerms().values()) {
			if(t.getGroupingKey().equals(expectedTerm)) {
				if(t.getFrequency() != frequency)
					failWithMessage("Expected frequency for term %s was <%s>, but actually is: <%s>.",
							expectedTerm,
							frequency,
							t.getFrequency());
				return this;
			}
		}
		failWithMessage("No such term <%s> found in termino.", expectedTerm);
		return this;
	}
	
	public TerminologyAssert containsTerm(String expectedTerm) {
		for(Term t:actual.getTerms().values()) {
			if(t.getGroupingKey().equals(expectedTerm))
				return this;
		}
		
		failWithMessage("No such term <%s> found in term index.", expectedTerm);
		return this;
	}
	
	public TerminologyAssert doesNotContainTerm(String expectedTerm) {
		for(Term t:actual.getTerms().values()) {
			if(t.getGroupingKey().equals(expectedTerm)) {
				failWithMessage("Expected term <%s> to be absent from term index, but is actually present.", expectedTerm);
				return this;
			}
		}
		return this;
		
	}

	
	public TerminologyAssert containsRelation(String baseGroupingKey, RelationType relation, String variantGroupingKey) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;
		
		Term baseTerm = actual.getTerms().get(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations().get(baseTerm)) {
			if(tv.getType() == relation && tv.getTo().getGroupingKey().equals(variantGroupingKey))
				return this;
		}
		
		failWithMessage("No such relation <%s--%s[%s]--%s> found in term index", 
				baseGroupingKey, relation, 
				info,
				variantGroupingKey
				);
		return this;

	}

	
	public TerminologyAssert containsVariation(String baseGroupingKey, VariationType type, String variantGroupingKey) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;
		
		Term baseTerm = actual.getTerms().get(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations().get(baseTerm)) {
			if(tv.getType() == RelationType.VARIATION 
					&& tv.isPropertySet(RelationProperty.VARIATION_TYPE)
					&& tv.get(RelationProperty.VARIATION_TYPE) == type
					&& tv.getTo().getGroupingKey().equals(variantGroupingKey))
				return this;
		}
		
		failWithMessage("No such variation <%s--%s[%s]--%s> found in term index", 
				baseGroupingKey, type, 
				info,
				variantGroupingKey
				);
		return this;
	}
	
	public TerminologyAssert containsRelationFrom(String fromKey, RelationType type, RelationType... types) {
		if(failToFindTerms(fromKey))
			return this;
		
		Term baseTerm = actual.getTerms().get(fromKey);
		Set<RelationType> types2 = types(type, types);
		for(TermRelation tv:actual.getOutboundRelations().get(baseTerm)) {
			if(types2.contains(tv))
				return this;
		}
		
		failWithMessage("No relation found from term <%s>%s", 
				fromKey,
				types.length == 0 ? "" : (" with types " + Joiner.on(", ").join(types))
				);
		return this;
	}

	
	private boolean failToFindTerms(String... groupingKeys) {
		boolean failed = false;
		for(String gKey:groupingKeys) {
			if(actual.getTerms().get(gKey) == null) {
				failed = true;
				failWithMessage("Could not find term <%s> in termino", gKey);
			}
		}
		return failed;
	}
	
	public TerminologyAssert containsVariation(String baseGroupingKey, VariationType type, String variantGroupingKey, RelationProperty p, Comparable<?> expectedValue) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;

		Term baseTerm = actual.getTerms().get(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations().get(baseTerm)) {
			if(tv.getType() == RelationType.VARIATION
					&& tv.get(RelationProperty.VARIATION_TYPE) == type
					&& tv.isPropertySet(p)
					&& java.util.Objects.equals(tv.getPropertyStringValue(p), expectedValue) 
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
		return actual.getOutboundRelations().values();
	}

	public TerminologyAssert hasNVariationsOfType(int expected, VariationType type) {
		int cnt = 0;
		for(TermRelation tv:getVariations()) {
			if(tv.isPropertySet(RelationProperty.VARIATION_TYPE)
					&& tv.get(RelationProperty.VARIATION_TYPE) == type)
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

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> asTermVariations(RelationType ralType, RelationType... ralTypes) {
		EnumSet<RelationType> accepted = EnumSet.of(ralType, ralTypes);
		Set<TermRelation> relations = actual.getOutboundRelations().values().stream()
				.filter(r-> accepted.contains(r.getType()))
				.collect(toSet());
		return assertThat(
				relations);
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends Term>, Term> asCompoundList() {
		Set<Term> comouponds = actual.getTerms().values().stream()
			.filter(t-> t.getWords().size() == 1 && t.getWords().get(0).getWord().isCompound())
			.collect(toSet());
		return assertThat(comouponds);
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends String>, String> asMatchingRules() {
		Set<String> matchingRuleNames = Sets.newHashSet();
		for(TermRelation tv:actual.getOutboundRelations().values().stream().filter(r->r.getType() == RelationType.VARIATION).collect(toSet())) 
			if(tv.isPropertySet(RelationProperty.VARIATION_RULE))
				matchingRuleNames.add(tv.getPropertyStringValue(RelationProperty.VARIATION_RULE));
		return assertThat(matchingRuleNames);
	}

	
	public TerminologyAssert hasAtLeastNRelationsOfType(Term base, int atLeastN, RelationType vType, RelationType... vTypes) {
		isNotNull();
		EnumSet<RelationType> accepted = EnumSet.of(vType, vTypes);
		Set<TermRelation> relations = actual.getOutboundRelations().get(base).stream()
				.filter(r-> accepted.contains(r.getType()))
				.collect(toSet());

		int actualSize = relations.size();
		if (actualSize < atLeastN)
			failWithMessage(
					"Expected to find at least <%s> variations of type <%s> for term <%s>, but actually found <%s>",
					atLeastN, vType, base, actualSize);
		return this;
	}

	public TerminologyAssert hasNVariationsOfType(Term base, int n, VariationType... vTypes) {
		isNotNull();
		Set<VariationType> vTypesSet = new HashSet<>(Arrays.asList(vTypes));
		int actualSize = (int)actual.getOutboundRelations().get(base)
				.stream()
				.filter(r -> r.getType() == RelationType.VARIATION)
				.filter(r -> 
					r.isPropertySet(RelationProperty.VARIATION_TYPE)	
						&& vTypesSet.contains(r.get(RelationProperty.VARIATION_TYPE)))
				.count();
		if (actualSize != n)
			failWithMessage("Expected to find <%s> variations of type <%s> for term <%s>, but actually found <%s>", n,
					vTypes, base, actualSize);
		return this;
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> getVariations(Term base) {
		return assertThat(actual.getOutboundRelations().get(base));
	}
	
	public TerminologyAssert hasNVariations(Term base, int expectedNumberOfVariations) {
		Collection<TermRelation> variants = actual.getOutboundRelations().get(base);
		if(variants.size() != expectedNumberOfVariations)
			failWithMessage("Expected <%s> variations but got <%s> (<%s>)", 
					expectedNumberOfVariations,
					variants.size(),
					variants);
		return this;
	}

	public TerminologyAssert hasExpectedCompounds(Path diffFileIfFail, Tuple... expectedTuples) {
		CompoundTupleExtractor compoundTupleExtractor = new CompoundTupleExtractor();
		Set<Tuple> actualTuples = actual.getTerms().values().stream()
			.filter(Term::isCompound)
			.map(compoundTupleExtractor::extract)
			.collect(Collectors.toSet());
		return tupleDiff(diffFileIfFail, Sets.newHashSet(expectedTuples), actualTuples);
	}
	
	public TerminologyAssert tupleDiff(Path diffFileIfFail, Set<Tuple> expectedTuples, Set<Tuple> actualTuples) {
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

	private Set<RelationType> types(RelationType type, RelationType... types) {
		return EnumSet.of(type, types);
	}

	
	public TerminologyAssert hasNRelations(int expected) {
		long actualCnt = actual.getOutboundRelations().values().size();
		if(actualCnt != expected) {
			failWithMessage("Expected <%s> relations. Got <%s>", 
					expected,
					actualCnt
				);
		}
		return this;
	}

	
	public TerminologyAssert hasNRelations(int expected, RelationType type, RelationType... types) {
		long actualCnt = actual.getOutboundRelations().values().stream().filter(r->types(type, types).contains(r)).count();
		if(actualCnt != expected) {
			failWithMessage("Expected <%s> relations%s. Got <%s>", 
					expected,
					types.length == 0 ? "" : (" with types " + Joiner.on(", ").join(types)),
					actualCnt
				);
		}
		return this;
	}

	public TerminologyAssert hasNRelationsFrom(int expected, String fromKey, RelationType type, RelationType... types) {
		if(failToFindTerms(fromKey))
			return this;

		Term term = actual.getTerms().get(fromKey);
		long actualCnt = actual.getOutboundRelations().get(term).stream().filter(r->types(type, types).contains(r)).count();
		if(actualCnt != expected) {
			failWithMessage("Expected <%s> relations%s from term %s. Got <%s>", 
					expected,
					types.length == 0 ? "" : (" with types " + Joiner.on(", ").join(types)),
					fromKey,
					actualCnt
				);
		}
		return this;
	}
	
	public TerminologyAssert hasNRelationsFrom(int expected, String fromKey) {
		if(failToFindTerms(fromKey))
			return this;

		Term term = actual.getTerms().get(fromKey);
		long actualCnt = actual.getOutboundRelations().get(term).size();
		if(actualCnt != expected) {
			failWithMessage("Expected <%s> relations from term %s. Got <%s>", 
					expected,
					fromKey,
					actualCnt
				);
		}
		return this;
	}

}
