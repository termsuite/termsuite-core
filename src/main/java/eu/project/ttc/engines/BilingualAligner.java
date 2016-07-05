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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

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


	private BilingualDictionary dico;
	private TermIndex sourceTermino;
	private TermIndex targetTermino;

	private CustomTermIndex targetTerminoLemmaIndex;
	private CustomTermIndex targetTerminoLemmaLemmaIndex;

	private SimilarityDistance distance;
	
	public BilingualAligner(BilingualDictionary dico, TermIndex sourceTermino, TermIndex targetTermino, SimilarityDistance distance) {
		super();
		this.dico = dico;
		this.targetTermino = targetTermino;
		this.sourceTermino = sourceTermino;
		this.distance = distance;
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
		dicoCandidates.addAll(translateWithDico(sourceTerm, Integer.MAX_VALUE));
		
		
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
				alignedCandidateQueue.add(new TranslationCandidate(targetTerm, v.getValue(), v.getExplanation()));
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


		return sortTruncateNormalize(nbCandidates, mergedCandidates);
	}

	private List<TranslationCandidate> sortTruncateNormalize(int nbCandidates, Collection<TranslationCandidate> candidatesCandidates) {
		ArrayList<TranslationCandidate> list = Lists.newArrayList(candidatesCandidates);
		Collections.sort(list);
		List<TranslationCandidate> finalCandidates = list.subList(0, Ints.min(nbCandidates, candidatesCandidates.size()));
		normalizeCandidateScores(finalCandidates);
		return finalCandidates;
	}

	public List<TranslationCandidate> translateWithDico(Term sourceTerm, int nbCandidates) {
		checkNotNull(sourceTerm);

		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		Collection<String> translations = dico.getTranslations(sourceTerm.getLemma());
		for(String candidateLemma:translations) {
			for(Term candidateTerm:targetTerminoLemmaIndex.getTerms(candidateLemma)) {
				dicoCandidates.add(
						new TranslationCandidate(
								candidateTerm, 
								targetTermino.getWRMeasure().getValue(candidateTerm))
						);
			}
		}
		

		return sortTruncateNormalize(nbCandidates, dicoCandidates);
	}

	
	public List<TranslationCandidate> alignCompositionalSize2(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		checkNotNull(sourceTerm);
		List<Term> swtTerms = asSize2swtColl(sourceTermino, sourceTerm);
		checkSize2(sourceTerm, swtTerms);
		Collection<TranslationCandidate> candidates = Lists.newArrayList();
		
		Term swt1 = sourceTermino.getTermByGroupingKey(swtTerms.get(0).getGroupingKey());
		Term swt2 = sourceTermino.getTermByGroupingKey(swtTerms.get(1).getGroupingKey());
		
		if(swt1 == null || swt2 == null)
			return Lists.newArrayList();
		
		List<TranslationCandidate> dicoCandidates1 = translateWithDico(swt1, Integer.MAX_VALUE);
		List<TranslationCandidate> dicoCandidates2 = translateWithDico(swt2, Integer.MAX_VALUE);
		
		candidates = combineCandidates(dicoCandidates1, dicoCandidates2);
		return sortTruncateNormalize(nbCandidates, candidates);
	}

	private void checkSize2(Term sourceTerm, List<Term> swtTerms) {
		if(swtTerms.size() != 2)
			throw new IllegalArgumentException(String.format(MSG_REQUIRES_SIZE_2_LEMMAS, 
				sourceTerm, 
				Joiner.on(TermSuiteConstants.COMMA).join(swtTerms)
				));
	}

	/**
	 * Join to lists of swt candidates and use the specificities (wrLog)
	 * of the combine terms as the candidate scores.
	 * 
	 * @param candidates1
	 * @param candidates2
	 * @return
	 */
	private Collection<TranslationCandidate> combineCandidates(Collection<TranslationCandidate> candidates1,
			Collection<TranslationCandidate> candidates2) {
		Collection<TranslationCandidate> combination = Sets.newHashSet();
		TermMeasure wrLog = targetTermino.getWRLogMeasure();
		wrLog.compute();
		for(TranslationCandidate candidate1:candidates1) {
			for(TranslationCandidate candidate2:candidates2) {
				List<Term> terms = targetTerminoLemmaLemmaIndex.getTerms(candidate1.getTerm().getLemma() + "+" + candidate2.getTerm().getLemma());
				terms.addAll(targetTerminoLemmaLemmaIndex.getTerms(candidate2.getTerm().getLemma() + "+" + candidate1.getTerm().getLemma()));
				for(Term t:terms) {
					combination.add(new TranslationCandidate(t, wrLog.getValue(t), new TextExplanation(String.format("Spécificité: %.1f", wrLog.getValue(t)))));
				}
			}
		}
		return combination;
	}

	private void checkNotNull(Term sourceTerm) {
		Preconditions.checkNotNull(sourceTerm, MSG_TERM_NOT_NULL);
	}

	private List<Term> asSize2swtColl(TermIndex termIndex, Term term) {
		List<Term> coll = TermUtils.getSingleWordTerms(termIndex, term, true);
		int sizeComp = coll.size();
		if(sizeComp == 2)
			return coll;
		else
			return TermUtils.getSingleWordTerms(termIndex, term, false);
	}
		
	
	public List<TranslationCandidate> alignSemiDistributionalSize2Syntagmatic(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		checkNotNull(sourceTerm);
		List<Term> swtTerms = asSize2swtColl(sourceTermino, sourceTerm);
		checkSize2(sourceTerm, swtTerms);

		List<TranslationCandidate> candidates = Lists.newArrayList();
		
		Term swt1 = swtTerms.get(0);
		Term swt2 = swtTerms.get(1);
		
		Collection<? extends TranslationCandidate> t1 = semiDistributional(swt1, swt2);
		candidates.addAll(t1);
		Collection<? extends TranslationCandidate> t2 = semiDistributional(swt2, swt1);
		candidates.addAll(t2);

		return sortTruncateNormalize(nbCandidates, candidates);
	}

	private Collection<? extends TranslationCandidate> semiDistributional(Term dicoTerm, Term vectorTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();
		List<TranslationCandidate> dicoCandidates = translateWithDico(dicoTerm, Integer.MAX_VALUE);
		
		if(dicoCandidates.isEmpty())
			// Optimisation: no need to align since there is no possible comination
			return candidates;
		else {
			List<TranslationCandidate> vectorCandidates = alignDistributional(vectorTerm, Integer.MAX_VALUE, 1);
			return combineCandidates(dicoCandidates, vectorCandidates);
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



	
	public class TranslationCandidate implements Comparable<TranslationCandidate> {
		private IExplanation explanation;
		private Term term;
		private double score;
		
		private TranslationCandidate(Term term, double score) {
			this(term, score, Explanation.emptyExplanation());
		}

			
		public void setScore(double score) {
			this.score = score;
		}


		private TranslationCandidate(Term term, double score, IExplanation explanation) {
			super();
			this.term = term;
			this.score = score;
			this.explanation = explanation;
		}

		@Override
		public int compareTo(TranslationCandidate o) {
			return ComparisonChain.start()
					.compare(o.score, score)
					.compare(term, o.term)
					.result();
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
					.add("s",String.format("%.2f", this.score))
					.toString();
		}
	}


	public BilingualDictionary getDico() {
		return this.dico;
	}
}
