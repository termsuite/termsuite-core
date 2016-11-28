
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

package eu.project.ttc.test.unit;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.assertj.core.util.Lists;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Word;

public class TermFactory {
	private TermIndex termIndex;
	private static final Pattern TERM_WORD_PATTERN = Pattern.compile("(\\w+)\\:(\\S+)(?:\\|(\\S+))");
	private static final Pattern COMPONENT_PATTERN = Pattern.compile("(\\S+)\\|(\\S+)");
	
	public TermFactory(TermIndex termIndex) {
		super();
		this.termIndex = termIndex;
	}

	public Term create(String... termWordSpecs) {
		TermBuilder builder = TermBuilder.start(termIndex);
		for(String termWordSpec:termWordSpecs) {
			Matcher matcher = TERM_WORD_PATTERN.matcher(termWordSpec);
			Preconditions.checkArgument(matcher.find(), "Bad term word spec: %s", termWordSpec);
			String label = matcher.group(1);
			String lemma = matcher.group(2);
			String stem = lemma;
			if(matcher.groupCount() == 3)
				stem = matcher.group(3);
			builder.addWord(lemma, stem, label);
		}
		return builder.createAndAddToIndex();
	}

	public void addPrefix(Term term1, Term term2) {
		termsExist(term1, term2);
		termIndex.addRelation(new TermRelation(RelationType.IS_PREFIX_OF, term1, term2));
	}

	public void addDerivesInto(String type, Term term1, Term term2) {
		termsExist(term1, term2);
		TermRelation relation = new TermRelation(RelationType.DERIVES_INTO, term1, term2);
		relation.setProperty(RelationProperty.DERIVATION_TYPE, type);
		termIndex.addRelation(relation);
	}

	private void termsExist(Term... terms) {
		for(Term t:terms)
			Preconditions.checkArgument(
					this.termIndex.getTermByGroupingKey(t.getGroupingKey()) != null,
					"Term %s does not exists in term index",
					t.getGroupingKey());
	}

	public void wordComposition(CompoundType type, String wordLemma, String... componentSpecs) {
		Word word = this.termIndex.getWord(wordLemma);
		Preconditions.checkArgument(
				word != null,
				"No such word: %s", wordLemma);
		
		List<Component> components = Lists.newArrayList();
		
		for(String componentSpec:componentSpecs) {
			Matcher matcher = COMPONENT_PATTERN.matcher(componentSpec);
			Preconditions.checkArgument(matcher.find(), "Bad component word spec: %s", componentSpec);
			String substring = matcher.group(1);
			String lemma = matcher.group(2);
			int start = wordLemma.indexOf(substring);
			Component component = new Component(lemma, start, start + substring.length());
			components.add(component);
		}
		
		word.setComposition(type, components);
	}
	
	public static Term termMock(String groupingKey, int freq, int rank, double specificity) {
		Term term = TermBuilder.start()
						.setGroupingKey(groupingKey, true)
						.setFrequency(freq)
						.setRank(rank)
						.setSpecificity(specificity)
						.create();
		return term;
	}

	public void setProperty(TermProperty p, Comparable<?> value) {
		this.termIndex.getTerms().stream().forEach(t-> t.setProperty(p, value));
	}
}
