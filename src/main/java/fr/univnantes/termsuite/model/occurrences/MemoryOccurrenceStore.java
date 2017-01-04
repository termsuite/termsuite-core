
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

package fr.univnantes.termsuite.model.occurrences;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;

public class MemoryOccurrenceStore extends AbstractMemoryOccStore {

	public MemoryOccurrenceStore(Lang lang) {
		super(lang);
	}

	private Multimap<Term, TermOccurrence> map = Multimaps.synchronizedMultimap(HashMultimap.create());
	
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
	public void removeTerm(Term t) {
		map.removeAll(t);
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public void addOccurrence(Term term, String documentUrl, int begin, int end, String coveredText) {
		map.put(term, 
				new TermOccurrence(term, coveredText, protectedGetDocument(documentUrl), begin, end));
	}

}
