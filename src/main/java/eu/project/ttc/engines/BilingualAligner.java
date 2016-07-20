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
package eu.project.ttc.engines;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import eu.project.ttc.engines.morpho.CompoundUtils;
import eu.project.ttc.metrics.ExplainedValue;
import eu.project.ttc.metrics.Explanation;
import eu.project.ttc.metrics.IExplanation;
import eu.project.ttc.metrics.SimilarityDistance;
import eu.project.ttc.metrics.TextExplanation;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermMeasure;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.resources.BilingualDictionary;
import eu.project.ttc.utils.AlignerUtils;
import eu.project.ttc.utils.IteratorUtils;
import eu.project.ttc.utils.Pair;
import eu.project.ttc.utils.TermSuiteConstants;
import eu.project.ttc.utils.TermUtils;
 
 
/** 
 * 
 * 
 * 
 * @author Damien Cram
 * 
 */
public class BilingualAligner {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BilingualAligner.class);
	private static final String MSG_NO_CONTEXT_VECTOR = "No context vector computed for term {} in target terminology";
	private static final String MSG_TERM_NOT_NULL = "Source term must not be null";
	private static final String MSG_REQUIRES_SIZE_2_LEMMAS = "The term %s must have exactly two single-word terms (single-word terms: %s)";
	private static final String MSG_SEVERAL_VECTORS_NOT_COMPUTED = "Several terms have no context vectors in target terminology (nb terms with vector: {}, nb terms without vector: {})";

	
	/**
	 * The bonus factor applied to dictionary candidates when they are
	 * merged with distributional candidates
	 */
	public static final double DICO_CANDIDATE_BONUS_FACTOR = 30;

	private BilingualDictionary dico;
	private TermIndex sourceTermino;
	private TermIndex targetTermino;

	private CustomTermIndex sourceTerminoLemmaIndex;
	private CustomTermIndex targetTerminoLemmaIndex;
	private CustomTermIndex targetTerminoLemmaLemmaIndex;

	private SimilarityDistance distance;
	
	public BilingualAligner(BilingualDictionary dico, TermIndex sourceTermino, TermIndex targetTermino, SimilarityDistance distance) {
		super();
		this.dico = dico;
		this.targetTermino = targetTermino;
		this.sourceTermino = sourceTermino;
		this.distance = distance;
		this.sourceTerminoLemmaIndex = sourceTermino.createCustomIndex("by_lemma", TermValueProviders.TERM_LEMMA_LOWER_CASE_PROVIDER);
		this.targetTerminoLemmaIndex = targetTermino.createCustomIndex("by_lemma", TermValueProviders.TERM_LEMMA_LOWER_CASE_PROVIDER);
		this.targetTerminoLemmaLemmaIndex = targetTermino.createCustomIndex("by_lemma_lemma", TermValueProviders.WORD_LEMMA_LEMMA_PROVIDER);
	}
	
	/**
	 * Overrides the default distance measure.
	 * 
	 * @param distance
	 * 			an object implementing the similarity distance
	 */
	public void setDistance(SimilarityDistance distance) {
		this.distance = distance;
	}

	
	/**
	 * 
	 * Translates the source term with the help of the dictionary
	 * and computes the list of <code>contextSize</code> closest candidate
	 * terms in the target terminology.
	 * 
	 * <code>sourceTerm</code>'s context vector must be computed and normalized,
	 * as well as all terms' context vectors in the target term index.
	 * 
	 * @param sourceTerm
	 * 			the term to align with target term index
	 * @param nbCandidates
	 * 			the number of {@link TranslationCandidate} to return in the returned list
	 * @param minCandidateFrequency
	 * 			the minimum frequency of a target candidate
	 * @return
	 * 			A sorted list of {@link TranslationCandidate} sorted by distance desc. Each
	 * 			{@link TranslationCandidate} is a container for a target term index's term 
	 * 			and its translation score.
	 * 			
	 */
	public List<TranslationCandidate> alignDistributional(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		checkNotNull(sourceTerm);

		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		/*
		 * 1- find direct translation of the term in the dictionary
		 */
		dicoCandidates.addAll(sortTruncateNormalize(targetTermino, nbCandidates, translateWithDico(sourceTerm, Integer.MAX_VALUE)));
		applySpecificityBonus(targetTermino, dicoCandidates);

		
		/*
		 * 2- align against all terms in the corpus
		 */
		Queue<TranslationCandidate> alignedCandidateQueue = MinMaxPriorityQueue.maximumSize(nbCandidates).create();
		ContextVector sourceVector = sourceTerm.getContextVector();
		ContextVector translatedSourceVector = AlignerUtils.translateVector(
				sourceVector,
				dico,
				AlignerUtils.TRANSLATION_STRATEGY_MOST_SPECIFIC,
				targetTermino);
		ExplainedValue v;
		int nbVectorsNotComputed = 0;
		int nbVectorsComputed = 0;
		for(Term targetTerm:IteratorUtils.toIterable(targetTermino.singleWordTermIterator())) {
			if(targetTerm.getFrequency() < minCandidateFrequency)
				continue;
			if(!targetTerm.isContextVectorComputed())  {
				if(nbVectorsNotComputed == 0)
					LOGGER.warn(MSG_NO_CONTEXT_VECTOR, targetTerm);
				nbVectorsNotComputed++;	
			} else {
				nbVectorsComputed++;
				v = distance.getExplainedValue(translatedSourceVector, targetTerm.getContextVector());
				alignedCandidateQueue.add(new TranslationCandidate(
						targetTerm, 
						AlignmentMethod.DISTRIBUTIONAL,
						v.getValue(), 
						v.getExplanation()));
			}
		}
		if(nbVectorsNotComputed > 0) {
			LOGGER.warn(MSG_SEVERAL_VECTORS_NOT_COMPUTED, nbVectorsComputed, nbVectorsNotComputed);	
		}
		
		// sort alignedCandidates
		List<TranslationCandidate> alignedCandidates = Lists.newArrayListWithCapacity(alignedCandidateQueue.size());
		alignedCandidates.addAll(alignedCandidateQueue);
		normalizeCandidateScores(alignedCandidates);
		
		
		/*
		 * 3- Merge candidates
		 */
		List<TranslationCandidate> mergedCandidates = dicoCandidates;
		mergedCandidates.addAll(alignedCandidateQueue);
		Collections.sort(mergedCandidates);
		

		return sortTruncateNormalize(targetTermino, nbCandidates, mergedCandidates);
	}
	
	
	private static final String ERR_MSG_BAD_SOURCE_LEMMA_SET_SIZE = "Unexpected size for a source lemma set: %s. Expected size: 2";
	/**
	 * 
	 * 
	 * @param sourceTerm
	 * @param nbCandidates
	 * @param minCandidateFrequency
	 * @return
	 */
	public List<TranslationCandidate> align(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		List<TranslationCandidate> mergedCandidates = Lists.newArrayList();
		for(List<Term> sourceLemmaSet:getSourceLemmaSets(sourceTerm)) {
			Preconditions.checkState(sourceLemmaSet.size() == 1 || sourceLemmaSet.size() == 2, 
					ERR_MSG_BAD_SOURCE_LEMMA_SET_SIZE, sourceLemmaSet);
			if(sourceLemmaSet.size() == 1) {
				mergedCandidates.addAll(alignDistributional(sourceTerm, nbCandidates, minCandidateFrequency));
			} else if(sourceLemmaSet.size() == 2) {
				List<TranslationCandidate> compositional = Lists.newArrayList();
				try {
					compositional.addAll(alignCompositionalSize2(sourceTerm, nbCandidates, minCandidateFrequency));
				} catch(RequiresSize2Exception e) {
					// Do nothing
				}
				mergedCandidates.addAll(compositional);
				List<TranslationCandidate> semiDist = Lists.newArrayList();
				try {
					semiDist = alignSemiDistributionalSize2Syntagmatic(sourceTerm, nbCandidates, minCandidateFrequency);
				} catch(RequiresSize2Exception e) {
					// Do nothing
				}
				mergedCandidates.addAll(semiDist);					
			} 
		}
		
		return sortTruncateNormalize(targetTermino, nbCandidates, mergedCandidates);
	}

	/**
	 * Returns all source lemma sets of given size
	 */
	private List<List<Term>> getSourceLemmaSets(Term sourceTerm, int sourceLemmaSetSize) {
		List<List<Term>> sourceLemmaSets = getSourceLemmaSets(sourceTerm);
		Iterator<List<Term>> it = sourceLemmaSets.iterator();
		while (it.hasNext()) 
			if(it.next().size() !=  sourceLemmaSetSize)
				it.remove();
		return sourceLemmaSets;
	}

	private List<List<Term>> getSourceLemmaSets(Term sourceTerm) {
		List<Term> swtTerms = TermUtils.getSingleWordTerms(sourceTermino, sourceTerm);
		List<List<Term>> sourceLemmaSets = Lists.newArrayList();
		if(swtTerms.size() == 1) {
			// sourceTerm is swtTerms.get(0);
			if(sourceTerm.isCompound()) {
				sourceLemmaSets.add(Lists.newArrayList(sourceTerm));
				for(Pair<String> pair:CompoundUtils.asLemmaPairs(sourceTerm.getWords().get(0).getWord())) {
					for(Term swt1:sourceTerminoLemmaIndex.getTerms(pair.getElement1())) {
						for(Term swt2:sourceTerminoLemmaIndex.getTerms(pair.getElement2())) {
							sourceLemmaSets.add(new Pair<Term>(swt1, swt2).toList());
							
						}
					}
				}
			} else {
				sourceLemmaSets.add(Lists.newArrayList(sourceTerm));
			}
		} else {
			if(swtTerms.size() == 2) {
				sourceLemmaSets.add(swtTerms);			
			} else 
				throw new RequiresSize2Exception(sourceTerm, swtTerms);
			
		}
		return sourceLemmaSets;
	}


	private List<TranslationCandidate> sortTruncateNormalize(TermIndex termIndex, int nbCandidates, Collection<TranslationCandidate> candidatesCandidates) {
		List<TranslationCandidate> list = Lists.newArrayList(candidatesCandidates);
		Collections.sort(list);
		List<TranslationCandidate> finalCandidates = list.subList(0, Ints.min(nbCandidates, candidatesCandidates.size()));
		normalizeCandidateScores(finalCandidates);
		return finalCandidates;
	}

	/*
	 * Filter candidates by specificity
	 */
	private void applySpecificityBonus(TermIndex termIndex, List<TranslationCandidate> list) {
		Iterator<TranslationCandidate> it = list.iterator();
		TranslationCandidate c;
		while (it.hasNext()) {
			c = (TranslationCandidate) it.next();
			double wr = termIndex.getWRMeasure().getValue(c.getTerm());
			c.setScore(c.getScore()*getSpecificityBonusFactor(wr));
		}
	}

	private double getSpecificityBonusFactor(double wr) {
		if(wr <= 1)
			return 0.5;
		else if(wr <= 2)
			return 1;
		else if(wr <= 10)
			return 1.5;
		else if(wr <= 100)
			return 2;
		else
			return 5;
	}

	public List<TranslationCandidate> translateWithDico(Term sourceTerm, int nbCandidates) {
		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		Collection<String> translations = dico.getTranslations(sourceTerm.getLemma());
		
		ContextVector translatedSourceVector = AlignerUtils.translateVector(
				sourceTerm.getContextVector(),
				dico,
				AlignerUtils.TRANSLATION_STRATEGY_MOST_SPECIFIC,
				targetTermino);

		
		for(String candidateLemma:translations) {
			for(Term candidateTerm:targetTerminoLemmaIndex.getTerms(candidateLemma)) {
				dicoCandidates.add(
						new TranslationCandidate(
								candidateTerm, 
								AlignmentMethod.DICTIONARY,
								distance.getValue(translatedSourceVector, candidateTerm.getContextVector()))
						);
			}
		}
		

		return dicoCandidates;
	}

	
	public List<TranslationCandidate> alignCompositionalSize2(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		checkNotNull(sourceTerm);
		List<List<Term>> swtTerms = getSourceLemmaSets(sourceTerm, 2);
		Collection<TranslationCandidate> candidates = Lists.newArrayList();
		
		for(List<Term> pair:swtTerms) {
			checkSize2(sourceTerm, pair);
			Term swt1 = sourceTermino.getTermByGroupingKey(pair.get(0).getGroupingKey());
			Term swt2 = sourceTermino.getTermByGroupingKey(pair.get(1).getGroupingKey());
			
			if(swt1 == null || swt2 == null)
				return Lists.newArrayList();
			
			List<TranslationCandidate> dicoCandidates1 = translateWithDico(swt1, Integer.MAX_VALUE);
			List<TranslationCandidate> dicoCandidates2 = translateWithDico(swt2, Integer.MAX_VALUE);
			
			candidates.addAll(combineCandidates(dicoCandidates1, dicoCandidates2, AlignmentMethod.COMPOSITIONAL));
		}
		return sortTruncateNormalize(targetTermino, nbCandidates, candidates);
	}

	public static class RequiresSize2Exception extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private Term term;
		private List<Term> swtTerms;
		
		public RequiresSize2Exception(Term term, List<Term> swtTerms) {
			super();
			this.term = term;
			this.swtTerms = swtTerms;
		}

		@Override
		public String getMessage() {
			return String.format(MSG_REQUIRES_SIZE_2_LEMMAS, 
				term, 
				Joiner.on(TermSuiteConstants.COMMA).join(swtTerms)
				);
		}
	}
	private void checkSize2(Term sourceTerm, List<Term> swtTerms) {
		if(swtTerms.size() != 2)
			throw new RequiresSize2Exception(sourceTerm, swtTerms);
	}

	/**
	 * Join to lists of swt candidates and use the specificities (wrLog)
	 * of the combine terms as the candidate scores.
	 * 
	 * FIXME Bad way of scoring candidates. They should be scored by similarity of context vectors with the source context vector
	 * 
	 * @param candidates1
	 * @param candidates2
	 * @return
	 */
	private Collection<TranslationCandidate> combineCandidates(Collection<TranslationCandidate> candidates1,
			Collection<TranslationCandidate> candidates2, AlignmentMethod method) {
		Collection<TranslationCandidate> combination = Sets.newHashSet();
		TermMeasure wrLog = targetTermino.getWRLogMeasure();
		wrLog.compute();
		for(TranslationCandidate candidate1:candidates1) {
			for(TranslationCandidate candidate2:candidates2) {
				/*
				 * 1- create candidate combine terms
				 */
				List<Term> candidateCombinedTerms = targetTerminoLemmaLemmaIndex.getTerms(candidate1.getTerm().getLemma() + "+" + candidate2.getTerm().getLemma());
				candidateCombinedTerms.addAll(targetTerminoLemmaLemmaIndex.getTerms(candidate2.getTerm().getLemma() + "+" + candidate1.getTerm().getLemma()));
				if(candidateCombinedTerms.isEmpty())
					continue;
				
				/*
				 * 2- Avoids retrieving too long terms by keeping the ones that have 
				 * the lowest number of lemma+lemma keys.
				 */
				final Map<Term, Collection<String>> termLemmaLemmaKeys = Maps.newHashMap();
				for(Term t:candidateCombinedTerms)
					termLemmaLemmaKeys.put(t, TermValueProviders.WORD_LEMMA_LEMMA_PROVIDER.getClasses(targetTermino, t));
				Collections.sort(candidateCombinedTerms, new Comparator<Term>() { 
					@Override
					public int compare(Term o1, Term o2) {
						return Integer.compare(termLemmaLemmaKeys.get(o1).size(), termLemmaLemmaKeys.get(o2).size());
					}
				});
				List<Term> filteredTerms = Lists.newArrayList();
				int minimumNbClasses = termLemmaLemmaKeys.get(candidateCombinedTerms.get(0)).size();
				for(Term t:candidateCombinedTerms) {
					if(termLemmaLemmaKeys.get(t).size() == minimumNbClasses)
						filteredTerms.add(t);
					else 
						break;
				}
				
				/*
				 * 3- Create candidates from filtered terms
				 */
				for(Term t:filteredTerms) {
					combination.add(new TranslationCandidate(
							t, 
							method,
							wrLog.getValue(t), 
							new TextExplanation(String.format("Spécificité: %.1f", wrLog.getValue(t)))));
				}
			}
		}
		return combination;
	}

	private void checkNotNull(Term sourceTerm) {
		Preconditions.checkNotNull(sourceTerm, MSG_TERM_NOT_NULL);
	}

	public List<TranslationCandidate> alignSemiDistributionalSize2Syntagmatic(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		checkNotNull(sourceTerm);
		List<List<Term>> swtTerms = getSourceLemmaSets(sourceTerm, 2);
		
		List<TranslationCandidate> candidates = Lists.newArrayList();
		for(List<Term> pair:swtTerms) {
			checkSize2(sourceTerm, pair);
			
			Term swt1 = pair.get(0);
			Term swt2 = pair.get(1);
			
			Collection<? extends TranslationCandidate> t1 = semiDistributional(swt1, swt2);
			candidates.addAll(t1);
			Collection<? extends TranslationCandidate> t2 = semiDistributional(swt2, swt1);
			candidates.addAll(t2);
		}

		removeDuplicatesOnTerm(candidates);
		return sortTruncateNormalize(targetTermino, nbCandidates, candidates);
	}

	private void removeDuplicatesOnTerm(List<TranslationCandidate> candidates) {
		Set<Term> set = Sets.newHashSet();
		Iterator<TranslationCandidate> it = candidates.iterator();
		while(it.hasNext())
			if(!set.add(it.next().getTerm()))
				it.remove();
	}

	private Collection<? extends TranslationCandidate> semiDistributional(Term dicoTerm, Term vectorTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();
		List<TranslationCandidate> dicoCandidates = translateWithDico(dicoTerm, Integer.MAX_VALUE);
		
		if(dicoCandidates.isEmpty())
			// Optimisation: no need to align since there is no possible comination
			return candidates;
		else {
			List<TranslationCandidate> vectorCandidates = alignDistributional(vectorTerm, Integer.MAX_VALUE, 1);
			return combineCandidates(dicoCandidates, vectorCandidates, AlignmentMethod.SEMI_DISTRIBUTIONAL);
		}
	}

	private void normalizeCandidateScores(List<TranslationCandidate> candidates) {
		double sum = 0;
		for(TranslationCandidate cand:candidates)
			sum+= cand.getScore();
		
		if(sum > 0d) 
			for(TranslationCandidate cand:candidates)
				cand.setScore(cand.getScore()/sum);
			
	}



	public static enum AlignmentMethod {
		DICTIONARY,
		DISTRIBUTIONAL,
		COMPOSITIONAL,
		SEMI_DISTRIBUTIONAL;
	}

	
	public static class TranslationCandidate implements Comparable<TranslationCandidate> {
		private IExplanation explanation;
		private AlignmentMethod method;
		private Term term;
		private double score;
		
		private TranslationCandidate(Term term, AlignmentMethod method, double score) {
			this(term, method, score, Explanation.emptyExplanation());
		}

			
		public void setScore(double score) {
			this.score = score;
		}


		private TranslationCandidate(Term term, AlignmentMethod method, double score, IExplanation explanation) {
			super();
			this.term = term;
			this.score = score;
			this.method = method;
			this.explanation = explanation;
		}

		@Override
		public int compareTo(TranslationCandidate o) {
			return ComparisonChain.start()
					.compare(o.score, score)
					.compare(term, o.term)
					.result();
		}
		
		public AlignmentMethod getMethod() {
			return method;
		}
		
		public double getScore() {
			return score;
		}
		
		public Term getTerm() {
			return term;
		}
		
		@Override
		public boolean equals(Object obj) {
			if( obj instanceof TranslationCandidate)
				return Objects.equal(((TranslationCandidate)obj).score, this.score) 
						&& Objects.equal(((TranslationCandidate)obj).term, this.term);
			else
				return false;
		}
		
		public IExplanation getExplanation() {
			return explanation;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(term, score);
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.addValue(this.term.getGroupingKey())
					.addValue(this.method.toString())
					.add("s",String.format("%.2f", this.score))
					.toString();
		}
	}


	public BilingualDictionary getDico() {
		return this.dico;
	}
}
