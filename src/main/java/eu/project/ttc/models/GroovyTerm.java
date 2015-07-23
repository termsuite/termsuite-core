/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package eu.project.ttc.models;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class GroovyTerm {
	public boolean neoclassical;
	public boolean compound;
	public boolean isSingleWord;
	public String pattern;
	public String lemma;
	public String stem;
	public List<GroovyWord> words;
	
	public GroovyTerm(Term term) {
		this.compound = term.isCompound();
		this.isSingleWord = term.isSingleWord();
		this.neoclassical = term.isCompound() && term.firstWord().getWord().getCompoundType() == CompoundType.NEOCLASSICAL;
		this.pattern = term.getPattern();
		this.lemma = term.getGroupingKey();
		this.stem = term.firstWord().getWord().getStem();
		List<GroovyWord> aux = Lists.newArrayListWithCapacity(term.getWords().size());
		
		for(TermWord w:term.getWords()) 
			aux.add(w.asGroovyWord());
		this.words = ImmutableList.copyOf(aux);
	}

	public GroovyWord getAt(int index) {
		return this.words.get(index);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("pattern", this.pattern)
				.add("lemma", this.lemma)
				.toString()
				;
	}
}