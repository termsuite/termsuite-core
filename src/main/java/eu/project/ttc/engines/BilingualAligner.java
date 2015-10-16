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
package eu.project.ttc.engines;

import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.primitives.Ints;

import eu.project.ttc.metrics.ExplainedValue;
import eu.project.ttc.metrics.Explanation;
import eu.project.ttc.metrics.SimilarityDistance;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.resources.BilingualDictionary;
import eu.project.ttc.utils.AlignerUtils;
import eu.project.ttc.utils.IteratorUtils;
 
 
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
	
	private BilingualDictionary dico;
	private TermIndex targetTermino;

	private CustomTermIndex targetTerminoLemmaIndex;
	
	private SimilarityDistance distance;
	
	public BilingualAligner(BilingualDictionary dico, TermIndex targetTermino, SimilarityDistance distance) {
		super();
		this.dico = dico;
		this.targetTermino = targetTermino;
		this.distance = distance;
		this.targetTerminoLemmaIndex = targetTermino.createCustomIndex("by_lemma", TermValueProviders.TERM_LEMMA_LOWER_CASE_PROVIDER);
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
	public List<TranslationCandidate> align(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		/*
		 * 1- find direct translation of the term in the dictionary
		 */
		for(String candidateLemma:dico.getTranslations(sourceTerm.getLemma())) {
			for(Term candidateTerm:targetTerminoLemmaIndex.getTerms(candidateLemma)) {
				dicoCandidates.add(new TranslationCandidate(candidateTerm, targetTermino.getWRMeasure().getValue(candidateTerm)));
			}
		}
		normalizeCandidateScores(dicoCandidates);
		
		
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
		for(Term targetTerm:IteratorUtils.toIterable(targetTermino.singleWordTermIterator())) {
			if(targetTerm.getFrequency() < minCandidateFrequency)
				continue;
			if(!targetTerm.isContextVectorComputed()) 
				LOGGER.warn(MSG_NO_CONTEXT_VECTOR, targetTerm);
			else {
				v = distance.getExplainedValue(translatedSourceVector, targetTerm.getContextVector());
				alignedCandidateQueue.add(new TranslationCandidate(targetTerm, v.getValue(), v.getExplanation()));
			}
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

		/*
		 * 4- Keep only nbCandidates and normalize again
		 */
		List<TranslationCandidate> finalCandidates = mergedCandidates.subList(0, Ints.min(nbCandidates, mergedCandidates.size()));
		normalizeCandidateScores(finalCandidates);

		return finalCandidates;
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
		private Explanation explanation;
		private Term term;
		private double score;
		
		private TranslationCandidate(Term term, double score) {
			this(term, score, Explanation.emptyExplanation());
		}

			
		public void setScore(double score) {
			this.score = score;
		}


		private TranslationCandidate(Term term, double score, Explanation explanation) {
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
		
		public Explanation getExplanation() {
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
