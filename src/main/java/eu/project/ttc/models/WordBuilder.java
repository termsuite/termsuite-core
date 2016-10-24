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
package eu.project.ttc.models;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

public class WordBuilder {
	
	private Optional<Word> word = Optional.empty();
	private String stem;
	private String lemma;
	private Optional<CompoundType> type = Optional.empty();

	private List<Component> components = Lists.newArrayList();
	private Optional<Component> neoclassicalAffix = Optional.empty();

	
	private WordBuilder() {
		super();
	}
	
	public WordBuilder(Word word) {
		super();
		this.word = Optional.of(word);
	}

	public WordBuilder setStem(String stem) {
		this.stem = stem;
		return this;
	}
	
	public WordBuilder setLemma(String lemma) {
		this.lemma = lemma;
		return this;
	}
	
	public WordBuilder setCompoundType(CompoundType type) {
		this.type = Optional.of(type);
		return this;
	}
	
	public Word create() {
		Word w = word.isPresent() ? word.get() : new Word(lemma, stem);
		Collections.sort(components);
		if(!components.isEmpty()) {
			if(!type.isPresent())
				type = Optional.of(CompoundType.NATIVE);
			w.setComposition(type.get(), components);
		}
		return w;
	}

	public WordBuilder addComponent(int begin, int end, String lemma) {
		return addComponent(begin, end, lemma, false);
	}
	public WordBuilder addComponent(int begin, int end, String compLemma, boolean neoclassicalAffix) {
		Component component = new Component(compLemma, begin, end);
		if(neoclassicalAffix)
			component.setNeoclassical();
		components.add(component);
		return this;
	}


	public static WordBuilder start() {
		return new WordBuilder();
	}

	public String getLemma() {
		return this.lemma;
	}

	public WordBuilder setNeoclassicalAffix(int begin, int end) {
		for(Component component:components) {
			if(component.getBegin() == begin && component.getEnd() == end) {
				neoclassicalAffix = Optional.of(component);
				component.setNeoclassical();
				return this;
			}
		}
		throw new IllegalArgumentException(String.format("Not a component: [%d,%d]",begin, end));
		
	}

}
