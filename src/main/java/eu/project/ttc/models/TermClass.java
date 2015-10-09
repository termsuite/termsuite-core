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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import eu.project.ttc.utils.TermOccurrenceUtils;


/**
 * A group of terms, often gathered by syntactical, graphical 
 * or semantical variation.
 * 
 * @author Damien Cram
 *
 */
public class TermClass implements Iterable<Term> {
	
	
	private Term head;
	private Set<Term> terms;
	private List<TermOccurrence> _occurrences;
	
	
	public TermClass(Term head, Iterable<Term> terms) {
		super();
		this.head = head;
		this.terms = ImmutableSet.copyOf(terms);
	}
	
	public Term getHead() {
		return head;
	}
	
	public Set<Term> getTerms() {
		return terms;
	}
	
	@Override
	public Iterator<Term> iterator() {
		return terms.iterator();
	}

	public int size() {
		return terms.size();
	}

	public boolean contains(Object o) {
		return terms.contains(o);
	}
	
	@Override
	public String toString() {
		return String.format("Class[%s] |%d|", head.getGroupingKey(), terms.size());
	}
	
	public int hashCode() {
		return head.hashCode();
	}

	public boolean equals(Object obj) {
		return head.equals(obj);
	}

	public String getPilot() {
		return head.getPilot();
	}

	/**
	 * Returns the set of non-overlapping term occurrences
	 * of all terms in this {@link TermClass}, sorted by 
	 * begin inc - end desc.
	 * 
	 * @return
	 */
	public List<TermOccurrence> getOccurrences() {
		if(_occurrences == null) {
			List<TermOccurrence> occs = Lists.newArrayList();
			for(Term t:terms)
				occs.addAll(t.getOccurrences());
			Collections.sort(occs, TermOccurrenceUtils.uimaNaturalOrder);
			this._occurrences = Lists.newArrayList();
			if(occs.size() > 1) {
				
				TermOccurrence last = occs.get(0);
				this._occurrences.add(last);
				for(int i=1;i<occs.size();i++) {
					TermOccurrence current = occs.get(i);
					if(last.overlaps(current))
						continue;
					else {
						last = current;
						this._occurrences.add(current);
					}
				}
				
			}
		}
		return Collections.unmodifiableList(_occurrences);
	}
	
	public int getFrequency() {
		return getOccurrences().size();
	}
}
