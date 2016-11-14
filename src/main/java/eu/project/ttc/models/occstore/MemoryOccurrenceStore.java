
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

package eu.project.ttc.models.occstore;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.project.ttc.models.Form;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.index.selectors.TermSelector;

public class MemoryOccurrenceStore extends AbstractMemoryOccStore {

	private Multimap<Term, TermOccurrence> map = HashMultimap.create();
	
	private Map<Term, Map<String, Form>> forms = new HashMap<>();

	private Form getForm(Term term, String coveredText) {
		if(!forms.containsKey(term))
			forms.put(term, new HashMap<>());
		Map<String, Form> map = forms.get(term);
		if(!map.containsKey(coveredText))
			map.put(coveredText, new Form(coveredText));
		Form form = map.get(coveredText);
		form.setCount(form.getCount()+1);
		return form;
	}
	
	@Override
	public Iterator<TermOccurrence> occurrenceIterator(Term term) {
		return getOccurrences(term).iterator();
	}

	@Override
	public Collection<TermOccurrence> getOccurrences(Term term) {
		return map.get(term);
	}

	@Override
	public Type getStoreType() {
		return Type.MEMORY;
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public void flush() {
		// nothing to do
	}

	@Override
	public State getCurrentState() {
		return State.INDEXED;
	}

	@Override
	public void makeIndex() {
		// nothing to do
	}

	@Override
	public void removeTerm(Term t) {
		map.removeAll(t);
	}

	@Override
	public void deleteMany(TermSelector selector) {
		Term t;
		for(Iterator<Term> it = map.keySet().iterator(); it.hasNext();) {
			t = it.next();
			if(selector.select(t))
				it.remove();
		}
			
		
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public List<Form> getForms(Term term) {
		if(!forms.containsKey(term))
			return Lists.newArrayList();
		else {
			List<Form> list = Lists.newArrayList(forms.get(term).values());
			Collections.sort(list);
			return list;
		}
	}

	@Override
	public void addOccurrence(Term term, String documentUrl, int begin, int end, String coveredText) {
		map.put(term, 
				new TermOccurrence(term, getForm(term, coveredText), protectedGetDocument(documentUrl), begin, end));
	}

}
