
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

import eu.project.ttc.models.Component;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;

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
		term1.addTermVariation(term2, VariationType.IS_PREFIX_OF, "");
	}

	public void addDerivesInto(String type, Term term1, Term term2) {
		termsExist(term1, term2);
		term1.addTermVariation(term2, VariationType.DERIVES_INTO, type);
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
	
}
