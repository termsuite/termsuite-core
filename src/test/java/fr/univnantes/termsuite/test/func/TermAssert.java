
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

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Objects;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.WordUtils;

public class TermAssert extends AbstractAssert<TermAssert, Term> {

	public TermAssert(Term actual) {
		super(actual, TermAssert.class);
	}

	public TermAssert hasGroupingKey(String gKey) {
		isNotNull();
		if (!Objects.areEqual(actual.getGroupingKey(), gKey))
			failWithMessage("Expected term's grouping key to be <%s> but was <%s>", gKey, actual.getGroupingKey());
		return this;
	}

	public TermAssert hasPropertyValue(TermProperty p, Object value) {
		isNotNull();
		if (!Objects.areEqual(actual.getPropertyValue(p), value))
			failWithMessage("Expected term's <%s> value to be <%s> but was <%s>", p, value, actual.getPropertyValue(p));
		return this;

	}

	public TermAssert hasFrequency(int f) {
		isNotNull();
		if (!Objects.areEqual(actual.getFrequency(), f))
			failWithMessage("Expected term's frequency key to be <%s> but was <%s>", f, actual.getFrequency());
		return this;
	}

	public TermAssert isCompound() {
		isNotNull();
		if(!actual.isCompound())
			failWithMessage("Expected term <%s> to be a compound but it is not.", actual);
		return this;
	}
	
	public TermAssert hasCompoundType(CompoundType type) {
		isNotNull();
		isCompound();
		Word word = actual.getWords().get(0).getWord();
		if(word.getCompoundType() != type)
			failWithMessage("Expected compound type <%s> for term %s, but got compound type <%s>",
					type,
					actual,
					word.getCompoundType());
		return this;
	}

	public TermAssert hasCompositionSubstrings(String... substrings) {
		isNotNull();
		isCompound();
		Word word = actual.getWords().get(0).getWord();
		List<String> expectedComps = Lists.newArrayList(substrings);
		List<String> actualComps = word.getComponents().stream()
				.map(c-> WordUtils.getComponentSubstring(word, c))
				.collect(Collectors.toList());
		if(word.getComponents().size() != substrings.length) {
			failWithMessage("Expected <%s> components for term %s, but got <%s> components: %s", 
					substrings.length,
					actual,
					word.getComponents().size(),
					actualComps
					);
		} else {
			if(!expectedComps.equals(actualComps))
				failWithMessage("Expected composition <%s> for term %s, but got composition <%s>", 
						expectedComps,
						actual,
						actualComps);
		}
		return this;
	}

	public TermAssert hasCompositionLemmas(String... lemmas) {
		isNotNull();
		isCompound();
		Word word = actual.getWords().get(0).getWord();
		List<String> expectedComps = Lists.newArrayList(lemmas);
		List<String> actualComps = word.getComponents().stream()
				.map(Component::getLemma)
				.collect(Collectors.toList());
		if(word.getComponents().size() != lemmas.length) {
			failWithMessage("Expected <%s> components for term %s, but got <%s> components: %s", 
					lemmas.length,
					actual,
					word.getComponents().size(),
					actualComps);
		} else {
			if(!expectedComps.equals(actualComps))
				failWithMessage("Expected composition <%s> for term %s, but got composition <%s>", 
						expectedComps,
						actual,
						actualComps);
		}
		return this;

		
	}

}
