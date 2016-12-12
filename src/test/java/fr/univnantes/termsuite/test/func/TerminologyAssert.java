
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
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
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.tools.ControlFilesGenerator;
import fr.univnantes.termsuite.utils.TerminologyUtils;

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
		failWithMessage("No such term <%s> found in termino.", expectedTerm);
		return this;
	}
	
	public TerminologyAssert containsTerm(String expectedTerm) {
		for(Term t:actual.getTerms()) {
			if(t.getGroupingKey().equals(expectedTerm))
				return this;
		}
		
		failWithMessage("No such term <%s> found in term index.", expectedTerm);
		return this;
	}
	
	public TerminologyAssert doesNotContainTerm(String expectedTerm) {
		for(Term t:actual.getTerms()) {
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
		
		Term baseTerm = actual.getTermByGroupingKey(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations(baseTerm, relation)) {
			if(tv.getTo().getGroupingKey().equals(variantGroupingKey))
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
		
		Term baseTerm = actual.getTermByGroupingKey(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations(baseTerm, RelationType.VARIATION)) {
			if(tv.isPropertySet(RelationProperty.VARIATION_TYPE)
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
	
	public TerminologyAssert containsRelationFrom(String fromKey, RelationType... types) {
		if(failToFindTerms(fromKey))
			return this;
		
		Term baseTerm = actual.getTermByGroupingKey(fromKey);
		for(TermRelation tv:actual.getOutboundRelations(baseTerm, types))
			return this;
		
		failWithMessage("No relation found from term <%s>%s", 
				fromKey,
				types.length == 0 ? "" : (" with types " + Joiner.on(", ").join(types))
				);
		return this;
	}

	
	public TerminologyAssert containsRelationTo(String toKey, RelationType... types) {
		if(failToFindTerms(toKey))
			return this;
		
		Term term = actual.getTermByGroupingKey(toKey);
		for(TermRelation tv:actual.getInboundRelations(term, types))
			return this;
		
		failWithMessage("No relation found to term <%s>%s", 
				toKey,
				types.length == 0 ? "" : (" with types " + Joiner.on(", ").join(types))
				);
		return this;
	}

	private boolean failToFindTerms(String... groupingKeys) {
		boolean failed = false;
		for(String gKey:groupingKeys) {
			if(actual.getTermByGroupingKey(gKey) == null) {
				failed = true;
				failWithMessage("Could not find term <%s> in termino", gKey);
			}
		}
		return failed;
	}
	
	public TerminologyAssert containsVariation(String baseGroupingKey, VariationType type, String variantGroupingKey, RelationProperty p, Comparable<?> expectedValue) {
		if(failToFindTerms(baseGroupingKey, variantGroupingKey))
			return this;

		Term baseTerm = actual.getTermByGroupingKey(baseGroupingKey);
		for(TermRelation tv:actual.getOutboundRelations(baseTerm, RelationType.VARIATION)) {
			if(tv.get(RelationProperty.VARIATION_TYPE) == type
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
		return actual.getRelations().collect(Collectors.toSet());
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

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> asTermVariations(RelationType... variations) {
		return assertThat(
				TerminologyUtils.selectTermVariations(actual, variations));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends Term>, Term> asCompoundList() {
		return assertThat(
				TerminologyUtils.selectCompounds(actual));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends String>, String> asMatchingRules() {
		Set<String> matchingRuleNames = Sets.newHashSet();
		for(TermRelation tv:TerminologyUtils.selectTermVariations(actual, RelationType.VARIATION)) 
			if(tv.isPropertySet(RelationProperty.VARIATION_RULE))
				matchingRuleNames.add(tv.getPropertyStringValue(RelationProperty.VARIATION_RULE));
		return assertThat(matchingRuleNames);
	}

	
	public TerminologyAssert hasAtLeastNVariationsOfType(Term base, int atLeastN, RelationType... vType) {
		isNotNull();
		int actualSize = actual.getOutboundRelations(base, vType).size();
		if (actualSize < atLeastN)
			failWithMessage(
					"Expected to find at least <%s> variations of type <%s> for term <%s>, but actually found <%s>",
					atLeastN, vType, base, actualSize);
		return this;
	}

	public TerminologyAssert hasNVariationsOfType(Term base, int n, VariationType... vTypes) {
		isNotNull();
		Set<VariationType> vTypesSet = new HashSet<>(Arrays.asList(vTypes));
		int actualSize = (int)actual.getOutboundRelations(base, RelationType.VARIATION)
				.stream()
				.filter(r -> 
					r.isPropertySet(RelationProperty.VARIATION_TYPE)	
						&& vTypesSet.contains(r.get(RelationProperty.VARIATION_TYPE)))
				.count();
		if (actualSize != n)
			failWithMessage("Expected to find <%s> variations of type <%s> for term <%s>, but actually found <%s>", n,
					vTypes, base, actualSize);
		return this;
	}

	public TerminologyAssert hasAtLeastNBasesOfType(Term variant, int atLeastN, VariationType... vTypes) {
		isNotNull();
		
		Set<VariationType> vTypesSet = new HashSet<>(Arrays.asList(vTypes));
		int actualSize = (int)actual.getInboundRelations(variant, RelationType.VARIATION)
				.stream()
				.filter(r -> 
					r.isPropertySet(RelationProperty.VARIATION_TYPE)	
					&& vTypesSet.contains(r.get(RelationProperty.VARIATION_TYPE)))
				.count();
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
		return assertThat(actual.getInboundRelations(variant));
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermRelation>, TermRelation> getBasesOfType(Term variant, RelationType... types) {
		return assertThat(actual.getInboundRelations(variant, types));
	}

	public TerminologyAssert hasNBases(Term variant, int expectedNumberOfBases) {
		Collection<TermRelation> bases = actual.getInboundRelations(variant);
		if(bases.size() != expectedNumberOfBases)
			failWithMessage("Expected <%s> bases but got <%s> (<%s>)", 
					expectedNumberOfBases,
					bases.size(),
					bases);
		return this;
	}
	
	public TerminologyAssert hasNVariations(Term base, int expectedNumberOfVariations) {
		Collection<TermRelation> variants = actual.getOutboundRelations(base);
		if(variants.size() != expectedNumberOfVariations)
			failWithMessage("Expected <%s> variations but got <%s> (<%s>)", 
					expectedNumberOfVariations,
					variants.size(),
					variants);
		return this;
	}

	public TerminologyAssert hasExpectedCompounds(Path diffFileIfFail, Tuple... expectedTuples) {
		CompoundTupleExtractor compoundTupleExtractor = new CompoundTupleExtractor();
		Set<Tuple> actualTuples = actual.getTerms().stream()
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


	public TerminologyAssert hasNRelations(int expected, RelationType... types) {
		long actualCnt = actual.getRelations(types).count();
		if(actualCnt != expected) {
			failWithMessage("Expected <%s> relations%s. Got <%s>", 
					expected,
					types.length == 0 ? "" : (" with types " + Joiner.on(", ").join(types)),
					actualCnt
				);
		}
		return this;
	}

	public TerminologyAssert hasNRelationsFrom(int expected, String fromKey, RelationType... types) {
		if(failToFindTerms(fromKey))
			return this;

		long actualCnt = actual.getOutboundRelations(actual.getTermByGroupingKey(fromKey), types).size();
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

	public TerminologyAssert hasNRelationsTo(int expected, String toKey, RelationType... types) {
		if(failToFindTerms(toKey))
			return this;

		long actualCnt = actual.getInboundRelations(actual.getTermByGroupingKey(toKey), types).size();
		if(actualCnt != expected) {
			failWithMessage("Expected <%s> relations%s to term %s. Got <%s>", 
					expected,
					types.length == 0 ? "" : (" with types " + Joiner.on(", ").join(types)),
					toKey,
					actualCnt
				);
		}
		return this;
	}

}
