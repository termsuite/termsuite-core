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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import eu.project.ttc.utils.IteratorUtils;
import eu.project.ttc.utils.TermSuiteConstants;
import eu.project.ttc.utils.TermSuiteUtils;
import eu.project.ttc.utils.TermUtils;


public class Term implements Iterable<TermOccurrence>, Comparable<Term> {
	
	private static final String NO_OCCURRENCE = "[No occurrence]";
//	private List<TermOccurrence> occurrences = Lists.newArrayList();
	private OccurrenceStore occurrenceStore;
	private Set<Document> documents = Sets.newHashSet();

	private Set<TermVariation> variations = Sets.newHashSet();
	private Set<TermVariation> bases = Sets.newHashSet();
	
	private Set<Term> extensions = Sets.newHashSet();
	private Set<Term> extensionBases = Sets.newHashSet();

	/*
	 * The identifier and display string of this term
	 */
	private String groupingKey;

	/*
	 * The numerical id in the term index
	 */
	private int id;
	
	private double normalizedTermFrequency;
	
	private double normalizedGeneralTermFrequency;
	
	/*
	 * The weirdness ratio of this term
	 */
//	private float wr;
	
	/*
	 * The logarithm of weirdness ratio of this term
	 */
//	private double wrLog;

	/*
	 * The z-score of wrLog
	 */
//	private double wrLogZScore;

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
	
	Term(OccurrenceStore occurrenceStore, int id) {
		this.occurrenceStore = occurrenceStore;
		this.id = id;
	}
	Term(OccurrenceStore occurrenceStore, int id, String termId, List<TermWord> termWords, String spottingRule) {
		this(occurrenceStore, id);
		this.groupingKey = termId;
		this.spottingRule = spottingRule;
		this.termWords = termWords;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
//	public double getWR() {
//		return getNormalizedTermFrequency() / getNormalizedGeneralTermFrequency();
//	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public Collection<TermOccurrence> getOccurrences() {
		return Collections.unmodifiableCollection(occurrenceStore.getOccurrences(this));
	}
	
	@Override
	public Iterator<TermOccurrence> iterator() {
		return getOccurrences().iterator();
	}


//	public boolean isEmpty() {
//		return occurrences.isEmpty();
//	}

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
	 */
	public void addOccurrence(TermOccurrence e, boolean keepOccurrence) {
		this.frequency++;
		this.documents.add(e.getSourceDocument());
		if(keepOccurrence)
			occurrenceStore.addOccurrence(this,e);
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

	public void addAll(Collection<TermOccurrence> c) {
		occurrenceStore.addAllOccurrences(this, c);
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
		return this.groupingKey;
	}
	
	public boolean isSingleWord() {
		return termWords.size() == 1;
	}

	public boolean isMultiWord() {
		return termWords.size() > 1;
	}

	/**
	 * Builds a {@link TermVariation} object and add it to {@link #variations} and
	 * variant{@link #bases}.
	 * 
	 * @param variant
	 * @param type
	 * @param info
	 */
	public void addTermVariation(Term variant, VariationType type, Object info) {
		TermVariation variation = new TermVariation(type, this, variant, info);
		this.variations.add(variation);
		variant.bases.add(variation);
	}
	
	/**
	 * Removes the param variation from this{@link #variations} and
	 * from variant's {@link #bases}.
	 * @param variation
	 */
	public void removeTermVariation(TermVariation variation) {
		this.variations.remove(variation);
		variation.getVariant().bases.remove(variation);
	}
	
	/**
	 * 
	 * Calls {@link #asComponentIterator(boolean)} with param <code>true</code>.
	 * 
	 * @return
	 */
	public Iterator<LemmaStemHolder> asComponentIterator() {
		return asComponentIterator(true);
	}


	/**
	 * Turns the term into a list {@link LemmaStemHolder} where each word of the term 
	 * is given as itself if not compound, or as a list of its components if compound.
	 * 
	 * @param compoundLevel
	 * 			set to <code>true</code> if this method should iterate over
	 * 			components when words are compound, set it to <code>false</code>
	 * 			if this method should iterate over plain words even though
	 * 			they are compounds.
	 * 
	 * @return
	 * 		The list of words and/or (depending on <code>compoundLevel</code>) compounds.
	 */
	public Iterator<LemmaStemHolder> asComponentIterator(final boolean compoundLevel) {
		return new AbstractIterator<LemmaStemHolder>() {
			private final Iterator<TermWord> it = Term.this.termWords.iterator();
			private Iterator<Component> currentWordIt;
			
			@Override
			protected LemmaStemHolder computeNext() {
				if(currentWordIt != null && currentWordIt.hasNext()) {
					return currentWordIt.next();
				} else if(it.hasNext()) {
					TermWord w = it.next();
					if(compoundLevel && w.getWord().isCompound()) {
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
	
//	public void removeOccurrence(String file, int begin, int end) {
//		// TODO Operation requires a linked list and is still too long. HashMap ?
//		Iterator<TermOccurrence> it = this.occurrenceStore.occurrenceIterator(this);
//		while(it.hasNext()) {
//			TermOccurrence occ = it.next();
//			if(occ.getBegin() == begin && occ.getEnd() == end && file.equals(occ.getSourceDocument().getUrl())) {
//				it.remove();
//				break;
//			}
//		}
//		this.frequency --;
//	}
	
	public TermWord firstWord() {
		return this.termWords.get(0);
	}
	public boolean isCompound() {
		return isSingleWord() && firstWord().getWord().isCompound();
	}
	
	public int getId() {
		return id;
	}
	
	
	public String getSpottingRule() {
		return spottingRule;
	}
	
	public boolean isVariant() {
		return !bases.isEmpty();
	}
	
	/**
	 * Use {@link TermUtils#formGetter(TermIndex, boolean)} instead.
	 * @return
	 */
	@Deprecated
	public Set<String> getForms() {
		List<String> forms = Lists.newArrayList();
		for(TermOccurrence o:getOccurrences())
			forms.add(o.getForm());
		LinkedHashMap<String, Integer> counters = TermSuiteUtils.getCounters(forms);
		return counters.keySet();
	}
	
	/**
	 * Use {@link TermUtils#formGetter(TermIndex, boolean)} instead.
	 * 
	 * @return
	 */
	@Deprecated
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
	
	public List<VariationPath> getVariationPaths(int depth) {
		ArrayList<VariationPath> accu = Lists.newArrayList();
		accumulateVariations(
				this,
				new ArrayList<TermVariation>(),
				depth,
				accu
				);
		return accu;
	}

	private void accumulateVariations(Term baseTerm, List<TermVariation> currentPath, int depth, List<VariationPath> accu) {
		if(depth == 0 
				|| (!currentPath.isEmpty() && this.equals(baseTerm)) // cycle prevention
			)
			return;
			
		for(TermVariation tv:this.variations) {
			currentPath.add(tv);
			accu.add(new VariationPath(currentPath));
			tv.getVariant().accumulateVariations(baseTerm, currentPath, depth-1, accu);
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
	 * @param coTermsType
	 * @param contextSize
	 * @param cooccFrequencyThreshhold
	 * @param useTermClasses
	 * @return
	 * 		The computed {@link ContextVector} object
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
			private Iterator<TermOccurrence> it = Term.this.occurrenceStore.occurrenceIterator(Term.this);
			
			
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
//	public double getWRLog() {
//		return Math.log10(1 + getWR());
//	}
	
	public Set<TermVariation> getVariations() {
		return Collections.unmodifiableSet(variations);
	}

	public Set<TermVariation> getBases() {
		return Collections.unmodifiableSet(bases);
	}


	private Iterator<TermVariation> getTermVariationsIterator(final Iterable<TermVariation> iterable, final VariationType... variantTypes) {
		return new AbstractIterator<TermVariation>() {
			private Iterator<TermVariation> it = iterable.iterator();
			private TermVariation current = null;
			private Set<VariationType> types = Sets.newHashSet(variantTypes);

			
			@Override
			protected TermVariation computeNext() {
				while(it.hasNext()) {
					this.current = it.next();
					if(this.types.contains(this.current.getVariationType()))
						return this.current;
				}
				return endOfData();
			}
		};
	}
	
	/**
	 * Get all variations of given {@link VariationType}s
	 * 
	 * @param variantTypes
	 * @return
	 */
	public Iterable<TermVariation> getVariations(final VariationType... variantTypes) {
		return IteratorUtils.toIterable(getTermVariationsIterator(this.variations, variantTypes));
	}
	
	/**
	 * Get all bases of given {@link VariationType}s
	 * 
	 * @param variantTypes
	 * @return
	 */
	public Iterable<TermVariation> getBases(final VariationType... variantTypes) {
		return IteratorUtils.toIterable(getTermVariationsIterator(this.bases, variantTypes));
	}
	
	public void setFrequencyNorm(double normalizedTermFrequency) {
		this.normalizedTermFrequency = normalizedTermFrequency;
	}
	
	public void setGeneralFrequencyNorm(double normalizedGeneralTermFrequency) {
		this.normalizedGeneralTermFrequency = normalizedGeneralTermFrequency;
	}
	
	/**
	 * The average number of occurrences of this term in the 
	 * general language corpus for each slice of 1000 words.
	 * 
	 * @return
	 */
	public double getGeneralFrequencyNorm() {
		return normalizedGeneralTermFrequency;
	}
	
	/**
	 * The average number of occurrences of this term in the 
	 * corpus for each slice of 1000 words.
	 * 
	 * @return
	 */
	public double getFrequencyNorm() {
		return normalizedTermFrequency;
	}
	
	public void addExtension(Term t) {
		this.extensions.add(t);
		t.extensionBases.add(this);
	}

	public Set<Term> getExtensions() {
		return Collections.unmodifiableSet(this.extensions);
	}

	public Set<Term> getExtensionBases() {
		return Collections.unmodifiableSet(this.extensionBases);
	}
}
