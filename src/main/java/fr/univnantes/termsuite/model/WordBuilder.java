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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.Terminology;

public class WordBuilder {
	private static final String LEMMA_EXIST_MSG = "Word with lemma %s already exists in termino";

	private Optional<Word> word = Optional.empty();
	private String stem;
	private String lemma;
	private Optional<CompoundType> type = Optional.empty();

	private List<Component> components = Lists.newArrayList();

	private Terminology termino;
	
	public WordBuilder() {
		
	}
	private WordBuilder(Terminology termino) {
		super();
		this.termino = termino;
	}
	
	public WordBuilder setStem(String stem) {
		this.stem = stem;
		return this;
	}
	
	public WordBuilder setLemma(String lemma) {
		if(termino != null)
			Preconditions.checkArgument(
					!termino.getWords().containsKey(lemma),
					LEMMA_EXIST_MSG, 
					lemma);
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
			w.setCompoundType(type.get());
			w.setComponents(components);
		}
		return w;
	}


	public WordBuilder addComponent(int begin, int end, String subString) {
		Component component = new Component(begin, end,subString);
		components.add(component);
		return this;		
	}
	
	public WordBuilder addComponent(int begin, int end, String subString, String lemma) {
		return addComponent(begin, end, subString, lemma, false);
	}
	
	
	public WordBuilder addComponent(int begin, int end, String substring, String compLemma, boolean neoclassicalAffix) {
		Component component = new Component(begin, end,substring,compLemma);
		if(neoclassicalAffix)
			component.setNeoclassicalAffix(true);
		components.add(component);
		return this;
	}


	public static WordBuilder start(Terminology terminology) {
		return new WordBuilder(terminology);
	}

	public String getLemma() {
		return this.lemma;
	}

	public WordBuilder setNeoclassicalAffix(int begin, int end) {
		for(Component component:components) {
			if(component.getBegin() == begin && component.getEnd() == end) {
				component.setNeoclassicalAffix(true);
				return this;
			}
		}
		throw new IllegalArgumentException(String.format("Not a component: [%d,%d]",begin, end));
		
	}

}
