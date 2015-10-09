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

import java.util.Map;

import com.google.common.collect.Maps;

public class GroovyAdapter {

	private Map<TermWord, GroovyWord> groovyWords = Maps.newHashMap();
	private Map<Term, GroovyTerm> groovyTerms = Maps.newHashMap();
	private Map<Component, GroovyComponent> groovyComponents = Maps.newHashMap();

	public GroovyTerm asGroovyTerm(Term term) {
		if(this.groovyTerms.get(term) == null)
			this.groovyTerms.put(term, new GroovyTerm(term, this));
		return this.groovyTerms.get(term);
	}

	public GroovyComponent asGroovyComponent(Component component) {
		if(this.groovyComponents.get(component) == null)
			this.groovyComponents.put(component, new GroovyComponent(component));
		return this.groovyComponents.get(component);
	}
	
	public GroovyWord asGroovyWord(TermWord termWord) {
		if(this.groovyWords.get(termWord) == null)
			this.groovyWords.put(termWord, new GroovyWord(termWord, this));
		return groovyWords.get(termWord);
	}
	
	public void clear() {
		this.groovyComponents.clear();
		this.groovyWords.clear();
		this.groovyTerms.clear();
	}

}
