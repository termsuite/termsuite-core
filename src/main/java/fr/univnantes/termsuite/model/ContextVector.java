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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fr.univnantes.termsuite.metrics.AssociationRate;

/**
 * 
 * A sorted Term/Frequency map used as a context vector during alignment operations.
 * 
 * @author Damien Cram
 *
 */
public class ContextVector {
	private Map<Term, Entry> entries = Maps.newHashMap();
	private List<Entry> _sortedEntries = null;
	private int totalCooccurrences = 0;

	private Term term;
	
	/**
	 * Default constructor for {@link ContextVector}. Must be used if no normalization is required.
	 */
	public ContextVector() {
	}

	
	/**
	 * 
	 * Construct a context vector with a back reference to its owner term.
	 * The parent term is useful for normalization.
	 * 
	 * @see ContextVector#toAssocRateVector(CrossTable, AssociationRate, boolean)
	 * @param term
	 * 			the owner term
	 */
	public ContextVector(Term term) {
		this.term = term;
	}

	public void addAllCooccurrences(Iterator<TermOccurrence> it) {
		while(it.hasNext()) {
			addCooccurrence(it.next());
		}
	}

	public void addCooccurrence(TermOccurrence occ) {
		Term term2 = occ.getTerm();
		if(!entries.containsKey(term2))
			entries.put(term2, new Entry(term2));
		entries.get(term2).increment();
		this.totalCooccurrences++;
		setDirty();
	}

	public void addEntry(Term coTerm, int nbCooccs, double assocRate) {
		Term termToAdd = coTerm;
		
		if(entries.containsKey(termToAdd))
			this.totalCooccurrences -= entries.get(termToAdd).getNbCooccs();
		entries.put(termToAdd, new Entry(termToAdd, nbCooccs, assocRate));
		this.totalCooccurrences+=nbCooccs;
		setDirty();
	}
	
	public void removeCoTerm(Term term) {
		Term termToAdd = term;
		if(this.entries.containsKey(termToAdd)) {
			Entry e = this.entries.remove(termToAdd);
			this.totalCooccurrences -= e.getNbCooccs();
		}
		setDirty();
	}

	private void setDirty() {
		this._sortedEntries = null;
	}
	
	/**
	 * Gives the list of terms in this context vector sorted by frequency
	 * (most frequent first)
	 * 
	 * @return
	 */
	public List<Entry> getEntries() {
		if(_sortedEntries == null) {
			this._sortedEntries = Lists.newArrayListWithCapacity(entries.size());
			for(Map.Entry<Term, Entry> e:entries.entrySet())
				this._sortedEntries.add(e.getValue());
			Collections.sort(this._sortedEntries);
		}
		return _sortedEntries;
	}
	
	/**
	 * Normalizes all <code>assocRates</code> so that their sum is 1
	 */
	public void normalize() {
		double sum = 0;
		for(Entry e:entries.values())
			sum+=e.getAssocRate();
		if(sum != 0) {
			for(Entry e:entries.values())
				e.setAssocRate(e.getAssocRate()/sum);
		}
	}
		

	public class Entry implements Comparable<Entry> {
		private static final String STRING_FORMAT = "%s: %.3f (%d)";
		private Term coTerm;
		private MutableInt nbCooccs;
		private double assocRate;
		private Entry(Term term) {
			this(term, 0, 0d);
		}
		private Entry(Term coTerm, int nbCoccurrences, double assocRate) {
			this.coTerm = coTerm;
			this.nbCooccs = new MutableInt(nbCoccurrences);
			this.assocRate = assocRate;
		}
		private void increment() {
			this.nbCooccs.increment();
		}
		public void setAssocRate(double assocRate) {
			this.assocRate = assocRate;
		}
		public double getAssocRate() {
			return assocRate;
		}
		public Term getCoTerm() {
			return coTerm;
		}
		public int getNbCooccs() {
			return nbCooccs.intValue();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Entry) 
				return Objects.equal(((Entry) obj).coTerm, this.coTerm) 
					&& Objects.equal(((Entry) obj).nbCooccs, this.nbCooccs)
					&& Objects.equal(((Entry) obj).assocRate, this.assocRate);
			else
				return false;
		}
		
		@Override
		public int hashCode() {
			return this.coTerm.getGroupingKey().hashCode();
		}
		
		@Override
		public int compareTo(Entry o) {
			return ComparisonChain.start()
					.compare(o.assocRate, this.assocRate)
					.compare(o.nbCooccs, this.nbCooccs)
					.result();
		}
		
		@Override
		public String toString() {
			return String.format(STRING_FORMAT, coTerm.getLemma(), assocRate, nbCooccs.intValue());
		}
		
	}
	
	public int getNbCooccs(Term term) {
		return entries.containsKey(term) ? entries.get(term).getNbCooccs() : 0;
	}

	public double getAssocRate(Term term) {
		return entries.containsKey(term) ? entries.get(term).getAssocRate() : 0d;
	}

	public Set<Term> terms() {
		return entries.keySet();
	}

	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.term);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContextVector) {
			ContextVector v = (ContextVector) obj;
			return Objects.equal(this.term, v.term) && Iterables.elementsEqual(this.getEntries(), v.getEntries());
		} else
			return false;
	}
	
	public Term getTerm() {
		return term;
	}
	
	/**
	 * Normalize this vector according to a cross table
	 * and an association rate measure.
	 * 
	 * This method recomputes all <code>{@link Entry}.frequency</code> values
	 * with the normalized ones.
	 * 
	 * @param table
	 * 			the pre-computed co-occurrences {@link CrossTable}
	 * @param assocRateFunction
	 * 			the {@link AssociationRate} measure implementation
	 * @param normalize 
	 */
	public void toAssocRateVector(CrossTable table, AssociationRate assocRateFunction, boolean normalize) {
		double assocRate;
		for(Term coterm:entries.keySet()) {
			assocRate = table.computeRate(assocRateFunction, this.term, coterm);
			entries.get(coterm).setAssocRate(assocRate);
		}
		if(normalize)
			normalize();

		setDirty();
	}
	
	private final static String STRING_FORMAT = "<%s> {%s}";
	@Override
	public String toString() {
		return String.format(STRING_FORMAT, this.term == null ? "no parent term" : this.term.getLemma(), Joiner.on(", ").join(getEntries()));
	}


	public int getTotalCoccurrences() {
		return this.totalCooccurrences;
	}
}
