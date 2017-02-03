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
package fr.univnantes.termsuite.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.MoreObjects;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Word;

/**
 * The in-memory implementation of a {@link Terminology}.
 * 
 * @author Damien Cram
 *
 */
public class Terminology  {

	/*
	 * The root index of terms. Variants must not be referenced at 
	 * this level of index. They me be indexed from their base-term
	 * instead. 
	 */
	private ConcurrentMap<String, Term> terms = new ConcurrentHashMap<>();
	private ConcurrentMap<String, Word> words = new ConcurrentHashMap<>();
	private List<Relation> relations = new ArrayList<>();
	
	private String name;
	private Lang lang;
	private String corpusId;
	
	private AtomicLong nbWordAnnotations = new AtomicLong();
	private AtomicLong nbSpottedTerms = new AtomicLong();
	
	public Terminology(String name, Lang lang) {
		this.lang = lang;
		this.name = name;
	}
	
	public Map<String, Term> getTerms() {
		return this.terms;
	}

	public Map<String, Word> getWords() {
		return words;
	}
	
	public Collection<Relation> getRelations() {
		return relations;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(name)
				.add("terms", this.terms.size())
				.toString();
	}
	
	public Lang getLang() {
		return this.lang;
	}
	
	public String getCorpusId() {
		return corpusId;
	}
	
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}

	public AtomicLong getNbWordAnnotations() {
		return nbWordAnnotations;
	}

	public void setNbWordAnnotations(AtomicLong nbWordAnnotations) {
		this.nbWordAnnotations = nbWordAnnotations;
	}

	public AtomicLong getNbSpottedTerms() {
		return nbSpottedTerms;
	}

	public void setNbSpottedTerms(AtomicLong nbSpottedTerms) {
		this.nbSpottedTerms = nbSpottedTerms;
	}
}
