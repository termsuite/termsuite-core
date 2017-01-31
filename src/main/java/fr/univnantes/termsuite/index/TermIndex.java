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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import fr.univnantes.termsuite.model.Term;

public class TermIndex {
	
	private ListMultimap<String, Term> classes;

	private TermIndexValueProvider valueProvider;
	
	private Semaphore mutex = new Semaphore(1);
	
	public TermIndex(TermIndexValueProvider valueProvider) {
		super();
		this.valueProvider = valueProvider;
		this.classes =  Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
	}

	public Collection<String> keySet() {
		return this.classes.keySet();
	}

	public List<Term> getTerms(String key) {
		return this.classes.get(key);
	}

	public void addToIndex(Term term) {
		mutex.acquireUninterruptibly();
		Collection<String> classes = valueProvider.getClasses(term);
		if(classes != null) {
			for(String cls:classes) {
				if(cls!= null)			
					this.classes.put(cls, term);
			}
		}
		mutex.release();
	}

	public int size() {
		return this.classes.size();
	}

	public void removeTerm(Term t) {
		mutex.acquireUninterruptibly();
		for(String k:valueProvider.getClasses(t))
			this.classes.remove(k, t);
		mutex.release();

	}


	public boolean containsKey(String key) {
		return !classes.get(key).isEmpty();
	}

	public void dropEntry(String key) {
		mutex.acquireUninterruptibly();
		classes.removeAll(key);
		mutex.release();
	}
	
	
	public ListMultimap<String, Term> getClasses() {
		return classes;
	}

}
