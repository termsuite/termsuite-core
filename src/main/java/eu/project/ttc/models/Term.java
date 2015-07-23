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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.utils.TermSuiteConstants;
import eu.project.ttc.utils.TermSuiteUtils;


public class Term implements Iterable<TermOccurrence>, Comparable<Term> {
	
	private static final String NO_OCCURRENCE = "[No occurrence]";
	private List<TermOccurrence> occurrences = Lists.newArrayList();
	private Set<Document> documents = Sets.newHashSet();
	private Set<SyntacticVariation> syntacticVariants = Sets.newHashSet();
	private Set<SyntacticVariation> syntacticBases = Sets.newHashSet();
	private Set<Term> graphicalVariants = Sets.newHashSet();
	private Set<Term> semanticVariants = Sets.newHashSet();
	
	/*
	 * The identifier and display string of this term
	 */
	private String groupingKey;

	/*
	 * The numerical id in the term index
	 */
	private int id;
	
	/*
	 * The weirdness ratio of this term
	 */
	private float wr;
	
	/*
	 * The logarithm of weirdness ratio of this term
	 */
	private double wrLog;

	/*
	 * The z-score of wrLog
	 */
	private double wrLogZScore;

	/*
	 * The frequency of this term
	 */
	private int frequency = 0;
	
	/*
	 * The syntactic pattern of this term
	 */
	private String pattern;
	
	/*
	 * The spotting rule
	 */
	private String spottingRule;

	/*
	 * The morphological components of this term
	 */
	private List<TermWord> termWords = Lists.newArrayList();
	
	/*
	 * The context vector
	 */
	private Optional<ContextVector> contextVector = Optional.absent();
	
	/**
	 * The term class
	 * @see TermClass
	 */
	private TermClass termClass;
	
	Term(int id) {
		this.id = id;
	}
	Term(int id, String termId, List<TermWord> termWords, String spottingRule) {
		this(id);
		this.groupingKey = termId;
		this.spottingRule = spottingRule;
		this.termWords = termWords;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	public float getWR() {
		return wr;
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public void setWR(float wr) {
		this.wr = wr;
	}
	
	public void setWRLog(double wrLog) {
		this.wrLog = wrLog;
	}
	
	public void setWRLogZScore(double wrLogZScore) {
		this.wrLogZScore = wrLogZScore;
	}
	
	public double getWRLogZScore() {
		return wrLogZScore;
	}
	
	public Collection<SyntacticVariation> getSyntacticVariants() {
		return Collections.unmodifiableCollection(syntacticVariants);
	}

	public Collection<SyntacticVariation> getSyntacticBases() {
		return Collections.unmodifiableCollection(syntacticBases);
	}

	public Set<TermOccurrence> getAllOccurrences() {
		return getAllOccurrences(1);
	}
	
	/**
	 * Return all occurrences of this term and of its syntactic and graphical variants at depth 2.
	 * 
	 * 
	 * @param depth
	 * @return
	 */
	public Set<TermOccurrence> getAllOccurrences(int depth) {
		Set<TermOccurrence> occSet = Sets.newTreeSet();
		occSet.addAll(occurrences);
		if(depth > 0) {
			for(SyntacticVariation t:syntacticVariants)
				occSet.addAll(t.getTarget().getAllOccurrences(depth-1));
			for(Term t:semanticVariants)
				occSet.addAll(t.getAllOccurrences(depth-1));
			for(Term t:graphicalVariants)
				occSet.addAll(t.getAllOccurrences(depth-1));
		}
		return occSet;
	}


	public Collection<TermOccurrence> getOccurrences() {
		return Collections.unmodifiableCollection(occurrences);
	}
	
	@Override
	public Iterator<TermOccurrence> iterator() {
		return getOccurrences().iterator();
	}


	public boolean isEmpty() {
		return occurrences.isEmpty();
	}

	public boolean isSyntacticBaseTerm() {
		return this.syntacticBases.isEmpty();
	}

	/**
	 * Increments the frequency, updates the inner list of source documents of this term
	 * and optionnaly updates the inner list of occurrences of this term if param 
	 * <code>keepOccurrence</code> is set to true.
	 * 
	 * @param e
	 * 			the occurrence object to add
	 * @param keepOccurrence
	 * 			set this param to true if you need the occurrence to be stored within the object
	 * 
	 * @see #getOccurrences()
	 * @see #removeOccurrence(String, int, int)
	 */
	public void addOccurrence(TermOccurrence e, boolean keepOccurrence) {
		this.frequency++;
		this.documents.add(e.getSourceDocument());
		if(keepOccurrence)
			occurrences.add(e);
	}

	
	/**
	 * Adds the parameter occurrence and stores it to the inner occurrence list.
	 * 
	 * @see #addOccurrence(TermOccurrence, boolean)
	 * @param e
	 * 			the occurrence object
	 */
	public void addOccurrence(TermOccurrence e) {
		this.addOccurrence(e, true);
	}

	public boolean addAll(Collection<TermOccurrence> c) {
		return occurrences.addAll(c);
	}
	
	
	
	@Override
	public int compareTo(Term o) {
		return ComparisonChain.start()
				.compare(o.groupingKey.length(), this.groupingKey.length())
				.compare(o.groupingKey, this.groupingKey)
				.result();
	}
	
	@Override
	public int hashCode() {
		return groupingKey.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Term) 
			return this.groupingKey.equals(((Term) obj).groupingKey);
		else
			return false;
	}
	
	public String getGroupingKey() {
		return groupingKey;
	}
	
	@Override
	public String toString() {
//		ToStringHelper stringHelper = Objects.toStringHelper(this)
//				.addValue(this.groupingKey)
//				.add("f", this.occurrences.size())
//				.add("s", (int)this.specificity)
//				.add("gra.", graphicalVariants.size())
//				.add("syn.", syntacticVariants.size())
//				.add("synBases", syntacticBases.size());
//		for(TermWord w:termWords) {
//			List<String> compoundsStr = Lists.newArrayList();
//			if(w.getWord().isCompound())
//				compoundsStr.add(w.getWord().getCompoundType().getShortName() + ":" + Joiner.on('|').join(w.getWord().getComponents()));
//			if(!compoundsStr.isEmpty())
//				stringHelper.add("compounds", Joiner.on(' ').join(compoundsStr));
//		}
//		return stringHelper.toString();
		return this.groupingKey;
	}
	
	public boolean isSingleWord() {
		return termWords.size() == 1;
	}

	public boolean isMultiWord() {
		return termWords.size() > 1;
	}

	public void addGraphicalVariant(Term term) {
		this.graphicalVariants.add(term);
		term.graphicalVariants.add(this);
	}

	public Collection<Term> getGraphicalVariants() {
		return Collections.unmodifiableCollection(graphicalVariants);
	}
	
	
	/**
	 * Turns the term into a list {@link LemmaStemHolder} where each word of the term 
	 * is given as itself if not compound, or as a list of its components if compound.
	 * 
	 * @return
	 */
	public Iterator<LemmaStemHolder> asComponentIterator() {
		return new AbstractIterator<LemmaStemHolder>() {
			private final Iterator<TermWord> it = Term.this.termWords.iterator();
			private Iterator<Component> currentWordIt;
			
			@Override
			protected LemmaStemHolder computeNext() {
				if(currentWordIt != null && currentWordIt.hasNext()) {
					return currentWordIt.next();
				} else if(it.hasNext()) {
					TermWord w = it.next();
					if(w.getWord().isCompound()) {
						this.currentWordIt = w.getWord().getComponents().iterator();
						return this.currentWordIt.next();
					} else {
						currentWordIt = null;
						return w.getWord();
					}
				} else 
					return endOfData();
			}
		};
	}
	
	public String getPattern() {
		if(pattern == null) {
			List<String> labels = Lists.newArrayListWithCapacity(termWords.size());
			for(TermWord w:termWords) 
				labels.add(w.getSyntacticLabel());
			pattern = Joiner.on(' ').join(labels);
		}
		return pattern;
	}
	
	public List<TermWord> getWords() {
		return this.termWords;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public void removeOccurrence(String file, int begin, int end) {
		// TODO Operation requires a linked list and is still too long. HashMap ?
		Iterator<TermOccurrence> it = this.occurrences.iterator();
		while(it.hasNext()) {
			TermOccurrence occ = it.next();
			if(occ.getBegin() == begin && occ.getEnd() == end && file.equals(occ.getSourceDocument().getUrl())) {
				it.remove();
				break;
			}
		}
		this.frequency --;
	}
	
	public TermWord firstWord() {
		return this.termWords.get(0);
	}
	public boolean isCompound() {
		return isSingleWord() && firstWord().getWord().isCompound();
	}
	
	private GroovyTerm groovyTerm;
	public GroovyTerm asGroovyTerm() {
		if(this.groovyTerm == null)
			this. groovyTerm = new GroovyTerm(this);
		return this.groovyTerm;
	}
	
	public void addSyntacticVariant(Term target, String variationRule) {
		SyntacticVariation variation = new SyntacticVariation(this, target, variationRule);
		this.syntacticVariants.add(variation);
		target.syntacticBases.add(variation);
	}
	
	public int getId() {
		return id;
	}
	
	
	public String getSpottingRule() {
		return spottingRule;
	}
	
	public boolean isVariant() {
		return !syntacticBases.isEmpty();
	}
	
	public Set<String> getForms() {
		List<String> forms = Lists.newArrayList();
		for(TermOccurrence o:getOccurrences())
			forms.add(o.getForm());
		LinkedHashMap<String, Integer> counters = TermSuiteUtils.getCounters(forms);
		return counters.keySet();
	}
	
	public void removeSyntacticVariant(SyntacticVariation sv) {
		this.syntacticVariants.remove(sv);
	}
	
	public void removeSyntacticBase(SyntacticVariation sv) {
		this.syntacticBases.remove(sv);
	}
	
	public void removeGraphicalVariant(Term t) {
		this.graphicalVariants.remove(t);
	}
	public String getPilot() {
		Iterator<String> it = getForms().iterator();
		if(it.hasNext())
			return it.next();
		else
			return NO_OCCURRENCE;
	}
	
	
	/**
	 * Returns the concatenation of inner words' lemmas.
	 */
	public String getLemma() {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for(TermWord tw:this.getWords()) {
			if(i>0)
				builder.append(TermSuiteConstants.WHITESPACE);
			builder.append(tw.getWord().getLemma());
			i++;
		}
		return builder.toString();
	}
	
	
	
	/**
	 * 
	 * Get all variants of this term at depth-3 level.
	 * 
	 * @param depth
	 * 			the depth level of variant gathering
	 * @param graphicalVariantDirectionalComp
	 * 			The term comparator such that compare(t1,t2) <= 0 when t2 can be considered as 
	 * 			a variant of t1. E.g., supposing that graphicalVariantDirectionalComp is the 
	 * 			reverse frequency comparator, t1.freq = 5, t2.freq = 150. t2 will not be returned
	 * 			in the variant set.
	 * @return
	 * 		the gathered variants
	 */
	public Set<Term> getVariants(int depth, Comparator<Term> graphicalVariantDirectionalComp) {
		HashSet<Term> variants = new HashSet<Term>();
		accumulateVariants(variants, graphicalVariantDirectionalComp, this, depth);
		variants.remove(this);
		return variants;
	}
	
	private void accumulateVariants(Set<Term> variants, Comparator<Term> graphicalVariantDirectionalComp, Term baseTerm, int depth) {
		if(!variants.contains(this)) {
			variants.add(this);
			if(depth>0) {
				for(SyntacticVariation sv:getSyntacticVariants())
					if(graphicalVariantDirectionalComp.compare(baseTerm, sv.getTarget()) <= 0)
						// Accumulate variants iff gv is not more interesting (i.e. more frequent nor more specific)
						sv.getTarget().accumulateVariants(variants, graphicalVariantDirectionalComp, baseTerm, depth - 1);
				for(Term gv:getGraphicalVariants()) {
					if(graphicalVariantDirectionalComp.compare(baseTerm, gv) <= 0)
						// Accumulate variants iff gv is not more interesting (i.e. more frequent nor more specific)
						gv.accumulateVariants(variants, graphicalVariantDirectionalComp, baseTerm, depth - 1);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * Returns the context vector of this term.
	 * 
	 * The context vector must have been explicitly invoked by user using
	 * the <code>#computeContextVector(contextSize)</code> method.
	 * 
	 * @see #isContextVectorComputed()
	 * @return 
	 * 		the {@link ContextVector} of this term
	 * @throws IllegalStateException 
	 * 		if the inner context vector does not exist (invoke <code>#computeContextVector(contextSize)</code>)
	 * 		to generate this vector if missing.
	 */
	public ContextVector getContextVector() {
		if(this.contextVector.isPresent())
			return this.contextVector.get();
		else
			throw new IllegalStateException("Context vector not set on term " + this);
	}
	
	/**
	 * 
	 * True if the context vector of this term has been computed.
	 * float
	 * @see #getContextVector()
	 * @return
	 */
	public boolean isContextVectorComputed() {
		return this.contextVector.isPresent();
	}

	/**
	 * 
	 * Regenerate the single-word contextVector of this term and returns it.
	 * 
	 * @see #computeContextVector(int, boolean, boolean)
	 * @param coTermsType
	 * @param contextSize
	 * @param cooccFrequencyThreshhold
	 * @param useTermClasses
	 * @return
	 */
	public ContextVector computeContextVector(OccurrenceType coTermsType, int contextSize, 
			int cooccFrequencyThreshhold, boolean useTermClasses) {
		// 1- compute context vector
		ContextVector vector = new ContextVector(this, useTermClasses);
		vector.addAllCooccurrences(Iterators.concat(contextIterator(coTermsType, contextSize)));
		vector.removeCoTerm(this);
		this.contextVector = Optional.of(vector);
		
		// 2- filter entries that under the co-occurrence threshold
		if(cooccFrequencyThreshhold > 1) {
			for(ContextVector.Entry e:this.contextVector.get().getEntries()) {
				if(e.getNbCooccs()<cooccFrequencyThreshhold)
					this.contextVector.get().removeCoTerm(e.getCoTerm());
			}
		}
		
		return this.contextVector.get();
	}

	
	public Iterator<Iterator<TermOccurrence>> contextIterator(final OccurrenceType coTermsType, final int contextSize) {
		return new AbstractIterator<Iterator<TermOccurrence>>() {
			private Iterator<TermOccurrence> it = Term.this.occurrences.iterator();
			
			
			@Override
			protected Iterator<TermOccurrence> computeNext() {
				if(this.it.hasNext())
					return it.next().contextIterator(coTermsType, contextSize);
				else
					return endOfData();
			}
		};
	}
	
	public int getDocumentFrequency() {
		return this.documents.size();
	}
	
	public void normalize(CrossTable crossTable) {
		
	}
	
	public Number getValue() {
		return 0;
	}
	
	public void setContextVector(ContextVector vector) {
		this.contextVector = Optional.of(vector);
	}
	public void clearContext() {
		this.contextVector = Optional.absent();
	}
	
	public void setTermClass(TermClass termClass) {
		this.termClass = termClass;
	}
	
	public TermClass getTermClass() {
		return termClass;
	}
	public double getWRLog() {
		return wrLog;
	}
}
