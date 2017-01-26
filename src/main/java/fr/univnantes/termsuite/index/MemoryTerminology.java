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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * The in-memory implementation of a {@link Terminology}.
 * 
 * @author Damien Cram
 *
 */
public class MemoryTerminology implements Terminology {
	/**
	 * The occurrence store 
	 */
	private OccurrenceStore occurrenceStore;

	/*
	 * The root index of terms. Variants must not be referenced at 
	 * this level of index. They me be indexed from their base-term
	 * instead. 
	 */
	private ConcurrentMap<String, Term> terms = new ConcurrentHashMap<>();
	private ConcurrentMap<String, Word> wordIndex = new ConcurrentHashMap<>();
	private Multimap<Term, TermRelation> outboundVariations = Multimaps.synchronizedListMultimap(LinkedListMultimap.create());
	private Multimap<Term, TermRelation> inboundVariations =  Multimaps.synchronizedListMultimap(LinkedListMultimap.create());
	
	private String name;
	private Lang lang;
	private String corpusId;
	
	private AtomicLong nbWordAnnotations = new AtomicLong();
	private AtomicLong nbSpottedTerms = new AtomicLong();
	
	public MemoryTerminology(String name, Lang lang, OccurrenceStore occurrenceStore) {
		this.lang = lang;
		this.name = name;
		this.occurrenceStore = occurrenceStore;
	}
	
	
	@Override
	public void addTerm(Term term) {
		Preconditions.checkArgument(
				!this.terms.containsKey(term.getGroupingKey()));

		this.terms.put(term.getGroupingKey(), term);
		for(TermWord tw:term.getWords()) {
			privateAddWord(tw.getWord(), false);
			if(!tw.isSwt())
				// try to set swt manually
				tw.setSwt(terms.containsKey(TermUtils.toGroupingKey(tw)));
		}
	}


	@Override
	public void addWord(Word word) {
		privateAddWord(word, true);
	}

	private void privateAddWord(Word word, boolean failIfAlredyPresent) {
		if(failIfAlredyPresent)
			Preconditions.checkArgument(
					!this.wordIndex.containsKey(word.getLemma()));
		this.wordIndex.put(word.getLemma(), word);
	}

	@Override
	public Map<String, Term> getTerms() {
		return this.terms;
	}

	@Override
	public Word getWord(String wordId) {
		return this.wordIndex.get(wordId);
	}

	@Override
	public Collection<Word> getWords() {
		return this.wordIndex.values();
	}
	
	@Override
	public void cleanOrphanWords() {
		Set<String> usedWordLemmas = Sets.newHashSet();
		for(Term t:getTerms().values()) {
			for(TermWord tw:t.getWords())
				usedWordLemmas.add(tw.getWord().getLemma());
		}
		Iterator<Entry<String, Word>> it = wordIndex.entrySet().iterator();
		Entry<String, Word> entry;
		while (it.hasNext()) {
			entry = it.next();
			if(!usedWordLemmas.contains(entry.getValue().getLemma()))
				it.remove();
		}
	}

	@Override
	public void removeTerm(Term t) {
		removeTermOnly(t);
		occurrenceStore.removeTerm(t);
	}

	private void removeTermOnly(Term t) {
		terms.remove(t.getGroupingKey());
		
		// remove from variants
		List<TermRelation> toRem = Lists.newLinkedList();
		toRem.addAll(this.outboundVariations.removeAll(t));
		toRem.addAll(this.inboundVariations.removeAll(t));
		toRem.forEach(this::removeRelation);
		
		/*
		 * Removes from context vectors.
		 * 
		 * We assumes that if this term has a context vector 
		 * then all others terms may have this term as co-term,
		 * thus they must be checked from removal.
		 * 
		 */
		if(t.getContext() != null) {
			for(Term o:terms.values()) {
				if(o.getContext() != null)
					o.getContext().removeCoTerm(t);
			}
		}
		
	}

	@Override
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
	
	@Override
	public Lang getLang() {
		return this.lang;
	}
	
	@Override
	public String getCorpusId() {
		return corpusId;
	}
	
	@Override
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}

	@Override
	public long getWordAnnotationsNum() {
		return this.nbWordAnnotations.longValue();
	}
	
	@Override
	public void setWordAnnotationsNum(long nbWordAnnos) {
		this.nbWordAnnotations = new AtomicLong(nbWordAnnos);
	}
	
	@Override
	public void incWordAnnotationsNum(int spottedTermsNum) {
		this.nbWordAnnotations.addAndGet(spottedTermsNum);
	}
	
	@Override
	public OccurrenceStore getOccurrenceStore() {
		return this.occurrenceStore;
	}


	@Override
	public Stream<TermRelation> getRelations(RelationType... types) {
		if(types.length == 0)
			return this.inboundVariations.entries().stream().map(e -> (TermRelation)e.getValue());
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			return this.inboundVariations.entries().stream().map(e -> (TermRelation)e.getValue())
					.filter(tv -> typeSet.contains(tv.getType()));
		}
	}

	@Override
	public Collection<TermRelation> getOutboundRelations(Term base, RelationType... types) {
		if(types.length == 0)
			return this.outboundVariations.get(base);
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			return this.outboundVariations.get(base).stream()
					.filter(tv -> typeSet.contains(tv.getType()))
					.collect(Collectors.toList());
		}
	}

	@Override
	public Collection<TermRelation> getInboundRelations(Term variant, RelationType... types) {
		if(types.length == 0)
			return this.inboundVariations.get(variant);
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			List<TermRelation> list = this.inboundVariations.get(variant).stream()
					.filter(tv -> typeSet.contains(tv.getType()))
					.collect(Collectors.toList());
			return list;
		}
	}
	
	@Override
	public void addRelation(TermRelation termVariation) {
		this.outboundVariations.put(termVariation.getFrom(), termVariation);
		this.inboundVariations.put(termVariation.getTo(), termVariation);
		
	}

	@Override
	public void removeRelation(TermRelation variation) {
		this.outboundVariations.remove(variation.getFrom(), variation);
		this.inboundVariations.remove(variation.getTo(), variation);
	}

	@Override
	public Stream<TermRelation> getRelations(Term from, Term to, RelationType... types) {
		Stream<TermRelation> stream = outboundVariations.get(from)
					.stream()
					.filter(relation -> relation.getTo().equals(to));
		
		if(types.length == 0)
			return stream;
		else if(types.length == 0)
			return stream.filter(relation -> relation.getType() == types[0]);
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			return stream.filter(relation -> typeSet.contains(relation.getType()));
		}
	}
	
	@Override
	public Multimap<Term, TermRelation> getOutboundRelations() {
		return outboundVariations;
	}

	@Override
	public Multimap<Term, TermRelation> getInboundRelations() {
		return inboundVariations;
	}

	@Override
	public long getSpottedTermsNum() {
		return this.nbSpottedTerms.get();
	}

	@Override
	public void setSpottedTermsNum(long spottedTermsNum) {
		this.nbSpottedTerms.set(spottedTermsNum);
	}

	@Override
	public void incSpottedTermsNum(int nbSpottedTerms) {
		this.nbSpottedTerms.addAndGet(nbSpottedTerms);
	}
}
