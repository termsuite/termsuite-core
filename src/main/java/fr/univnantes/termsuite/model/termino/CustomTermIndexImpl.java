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
package fr.univnantes.termsuite.model.termino;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;

public class CustomTermIndexImpl implements CustomTermIndex {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTermIndexImpl.class);
	
	private ListMultimap<String, Term> index;

	private TermValueProvider valueProvider;
	
	CustomTermIndexImpl(TermValueProvider valueProvider) {
		super();
		this.valueProvider = valueProvider;
		this.index =  Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
	}

	@Override
	public Collection<String> keySet() {
		return this.index.keySet();
	}

	@Override
	public List<Term> getTerms(String key) {
		return this.index.get(key);
	}

	@Override
	public void indexTerm(Terminology termino, Term term) {
		Collection<String> classes = valueProvider.getClasses(termino, term);
		if(classes != null) {
			for(String cls:classes) {
				if(cls!= null)			
					this.index.put(cls, term);
			}
		}
	}

	@Override
	public void cleanSingletonKeys() {
		Iterator<String> it = this.index.keySet().iterator();
		while(it.hasNext())
			if(this.index.get(it.next()).size() == 1)
				it.remove();
	}

	@Override
	public int size() {
		return this.index.size();
	}

	@Override
	public void removeTerm(Terminology termino, Term t) {
		for(String k:valueProvider.getClasses(termino, t))
			this.index.remove(k, t);
	}

	@Override
	public void dropBiggerEntries(int threshholdSize, boolean logWarning) {
		Set<String> toRemove = Sets.newHashSet();
		for(String key:index.keySet()) {
			if(index.get(key).size() >= threshholdSize)
				toRemove.add(key);
		}
		for(String rem:toRemove) {
			LOGGER.warn("Removing key {} from custom index {} because its size {} is bigger than the threshhold {}",
					rem,
					this.valueProvider.getName(),
					this.index.get(rem).size(),
					threshholdSize);
			index.removeAll(rem);
		}
	}

	@Override
	public void cleanEntriesByMaxSize(int maxSize) {
		String msg = "Index entry {} had too many elements. Applied th={} filter. Before -> after filtering: {} -> {}";
		int th;
		Iterator<Term> it;
		Term t;
		int initialSize;
		for(String key:index.keySet()) {
			th = 1;
			initialSize = index.get(key).size();
			while (index.get(key).size() > maxSize) {
				th++;
				it = index.get(key).iterator();
				while(it.hasNext()) {
					t = it.next();
					if(t.getFrequency()<th)
						it.remove();
				}
			}
			if(th>1) {
				LOGGER.warn(msg,
						key,
						th,
						initialSize,
						index.get(key).size()
						);
			}
		}
		
	}

	@Override
	public boolean containsKey(String key) {
		return !index.get(key).isEmpty();
	}

	@Override
	public void dropEntry(String key) {
		index.removeAll(key);
		
	}

}
