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
package eu.project.ttc.align;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import eu.project.ttc.metrics.ExplainedValue;
import eu.project.ttc.metrics.Levenshtein;
import eu.project.ttc.metrics.SimilarityDistance;
import eu.project.ttc.metrics.TextExplanation;
import eu.project.ttc.models.Component;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.resources.BilingualDictionary;
import eu.project.ttc.utils.AlignerUtils;
import eu.project.ttc.utils.IteratorUtils;
import eu.project.ttc.utils.StringUtils;
import eu.project.ttc.utils.TermIndexUtils;
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
	private static final String MSG_TERM_NOT_NULL = "Source term must not be null";
	static final String MSG_REQUIRES_SIZE_2_LEMMAS = "The term %s must have exactly two single-word terms (single-word terms: %s)";
	private static final String MSG_SEVERAL_VECTORS_NOT_COMPUTED = "Several terms have no context vectors in target terminology (nb terms with vector: {}, nb terms without vector: {})";
	private static final String ERR_VECTOR_NOT_SET = "Cannot align on term %s. Cause: context vector no set.";

	
	/**
	 * The bonus factor applied to dictionary candidates when they are
	 * merged with distributional candidates
	 */
	public static final double DICO_CANDIDATE_BONUS_FACTOR = 30;

	private BilingualDictionary dico;
	private TermIndex sourceTermino;
	private TermIndex targetTermino;

	private SimilarityDistance distance;
	
	public BilingualAligner(BilingualDictionary dico, TermIndex sourceTermino, TermIndex targetTermino, SimilarityDistance distance) {
		super();
		this.dico = dico;
		this.targetTermino = targetTermino;
		this.sourceTermino = sourceTermino;
		this.distance = distance;
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
	public List<TranslationCandidate> alignDicoThenDistributional(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		checkNotNull(sourceTerm);
		Preconditions.checkArgument(sourceTerm.isContextVectorComputed(), ERR_VECTOR_NOT_SET, sourceTerm.getGroupingKey());

		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		/*
		 * 1- find direct translation of the term in the dictionary
		 */
		dicoCandidates.addAll(sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, alignDico(sourceTerm, Integer.MAX_VALUE)));
		applySpecificityBonus(targetTermino, dicoCandidates);

		
		/*
		 * 2- align against all terms in the corpus
		 */
		List<TranslationCandidate> alignedCandidateQueue = alignDistributional(sourceTerm, nbCandidates,
				minCandidateFrequency);
		
		
		/*
		 * 3- Merge candidates
		 */
		List<TranslationCandidate> mergedCandidates = dicoCandidates;
		mergedCandidates.addAll(alignedCandidateQueue);
		Collections.sort(mergedCandidates);
		

		/*
		 * 4- Sort, truncate, and normalize
		 */
		List<TranslationCandidate> sortedTruncateedNormalized = sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, mergedCandidates);
		return sortedTruncateedNormalized;
	}

	public boolean canAlignNeoclassical(Term sourceTerm) {
		return sourceTerm.isCompound() 
				&& sourceTerm.getWords().get(0).getWord().getCompoundType() == CompoundType.NEOCLASSICAL;
	}

	public List<TranslationCandidate> alignNeoclassical(Term sourceTerm, int nbCandidates,
			int minCandidateFrequency) {
		
		if(!canAlignNeoclassical(sourceTerm))
			return Lists.newArrayList();
		
		Component neoclassicalAffix = sourceTerm.getWords().get(0).getWord().getNeoclassicalAffix();

		
		/*
		 * Index target candidates by morphological extensions when the extension 
		 * is a valid swt in the target termino.
		 * 
		 * E.g. électricité -> hydroélectricité
		 */
		Map<Term, Term> targetCandidatesBySWTExtension = Maps.newHashMap();
		Set<Term> targetCandidatesHavingSameAffix = Sets.newHashSet();
		for(Term targetCandidate:targetTermino.getTerms()) {
			Word targetCompound = targetCandidate.getWords().get(0).getWord();
			if(targetCandidate.isCompound() && targetCompound.getCompoundType() == CompoundType.NEOCLASSICAL) {
				String targetLemma = targetCompound.getNeoclassicalAffix().getLemma();
				String sourceLemma = neoclassicalAffix.getLemma();
				if(StringUtils.replaceAccents(targetLemma).equals(StringUtils.replaceAccents(sourceLemma))) {
					targetCandidatesHavingSameAffix.add(targetCandidate);
					
					Collection<Term> targetExtensions = TermIndexUtils.getMorphologicalExtensionsAsTerms(
							targetTermino, 
							targetCandidate, 
							targetCompound.getNeoclassicalAffix());
					
					for(Term morphologicalExtensin: targetExtensions) 
						targetCandidatesBySWTExtension.put(morphologicalExtensin, targetCandidate);
				}
				
			} 
		}
		
		Collection<Term> possibleSourceExtensions = TermIndexUtils.getMorphologicalExtensionsAsTerms(
				sourceTermino, 
				sourceTerm, 
				neoclassicalAffix);
		
		/*
		 * 6a- try to align by extension translation with dico
		 */	
		List<TranslationCandidate> candidates = Lists.newArrayList();
		for(Term sourceExtension:possibleSourceExtensions) {
			
			// recursive alignment on extension
			List<TranslationCandidate> recursiveCandidates = align(sourceExtension, nbCandidates, minCandidateFrequency);
			
			for(TranslationCandidate extensionTranslationCandidate:recursiveCandidates) {
				if(targetCandidatesBySWTExtension.containsKey(extensionTranslationCandidate.getTerm()))
					candidates.add(new TranslationCandidate(
						AlignmentMethod.NEOCLASSICAL, 
						targetCandidatesBySWTExtension.get(extensionTranslationCandidate.getTerm()), 
						extensionTranslationCandidate.getScore(), 
						sourceTerm, 
						extensionTranslationCandidate));
			
			}
		}
		
		// graphical alignment on extension if no candidate
		if(candidates.isEmpty())
			candidates.addAll(alignGraphically(AlignmentMethod.NEOCLASSICAL, sourceTerm, nbCandidates, targetCandidatesHavingSameAffix));

		
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, candidates);
	}

	private static final Levenshtein LEVENSHTEIN = new Levenshtein();

	public List<TranslationCandidate> alignGraphically(AlignmentMethod method, Term sourceTerm, int nbCandidates, Collection<Term> targetTerms) {
		Preconditions.checkArgument(sourceTerm.isSingleWord());
		for(Term targetTerm:targetTerms)
			Preconditions.checkArgument(targetTerm.isSingleWord());
		
		Word sourceWord = sourceTerm.getWords().get(0).getWord();
		return targetTerms.stream().map(targetTerm -> 
			{
				double dist;
				Word targetWord = targetTerm.getWords().get(0).getWord();
				if(sourceWord.getStem() != null 
						&& targetWord.getStem() != null)
					dist = LEVENSHTEIN.computeNormalized(
							TermUtils.stemmedInsensitiveGroupingKey(sourceTerm.getWords().get(0)), 
							TermUtils.stemmedInsensitiveGroupingKey(targetTerm.getWords().get(0)));
				else
					dist = LEVENSHTEIN.computeNormalized(
							TermUtils.lemmatizedInsensitiveGroupingKey(sourceTerm.getWords().get(0)), 
							TermUtils.lemmatizedInsensitiveGroupingKey(targetTerm.getWords().get(0)));
				return new TranslationCandidate(
						method, 
						targetTerm, 
						dist, 
						sourceTerm,
						new TextExplanation(String.format("Graphical distance(Levenshtein) is %.3f", dist)));
			}
				).collect(Collectors.toList());
	}

	
	public List<TranslationCandidate> alignDistributional(Term sourceTerm, int nbCandidates,
			int minCandidateFrequency) {
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
			if(targetTerm.isContextVectorComputed()) {
				nbVectorsComputed++;
				v = distance.getExplainedValue(translatedSourceVector, targetTerm.getContextVector());
				TranslationCandidate candidate = new TranslationCandidate(
						AlignmentMethod.DISTRIBUTIONAL,
						targetTerm, 
						v.getValue(), 
						sourceTerm,
						v.getExplanation());
				alignedCandidateQueue.add(candidate);
			}
		}
		if(nbVectorsNotComputed > 0) {
			LOGGER.warn(MSG_SEVERAL_VECTORS_NOT_COMPUTED, nbVectorsComputed, nbVectorsNotComputed);	
		}
		
		// sort alignedCandidates
		List<TranslationCandidate> alignedCandidates = Lists.newArrayListWithCapacity(alignedCandidateQueue.size());
		alignedCandidates.addAll(alignedCandidateQueue);
		normalizeCandidateScores(alignedCandidates);
		return Lists.newArrayList(alignedCandidateQueue);
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
		Preconditions.checkNotNull(sourceTerm);
		List<TranslationCandidate> mergedCandidates = Lists.newArrayList();
		List<List<Term>> sourceLemmaSets = AlignerUtils.getSingleLemmaTerms(sourceTermino, sourceTerm);
		for(List<Term> sourceLemmaSet:sourceLemmaSets) {
			Preconditions.checkState(sourceLemmaSet.size() == 1 || sourceLemmaSet.size() == 2, 
					ERR_MSG_BAD_SOURCE_LEMMA_SET_SIZE, sourceLemmaSet);
			if(sourceLemmaSet.size() == 1) {
				mergedCandidates.addAll(alignDicoThenDistributional(sourceLemmaSet.get(0), 3*nbCandidates, minCandidateFrequency));
			} else if(sourceLemmaSet.size() == 2) {
				List<TranslationCandidate> compositional = Lists.newArrayList();
				try {
					compositional.addAll(alignCompositionalSize2(sourceLemmaSet.get(0), sourceLemmaSet.get(1), nbCandidates, minCandidateFrequency, sourceTerm));
				} catch(RequiresSize2Exception e) {
					// Do nothing
				}
				mergedCandidates.addAll(compositional);
				if(mergedCandidates.isEmpty()) {
					List<TranslationCandidate> semiDist = Lists.newArrayList();
					try {
						semiDist = alignSemiDistributionalSize2Syntagmatic(
										sourceLemmaSet.get(0), 
										sourceLemmaSet.get(1), 
										nbCandidates, 
										minCandidateFrequency, 
										sourceTerm);
					} catch(RequiresSize2Exception e) {
						// Do nothing
					}
					mergedCandidates.addAll(semiDist);
				}
			} 
		}
		
		removeDuplicatesOnTerm(mergedCandidates);
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, mergedCandidates);
	}

	private List<TranslationCandidate> sortTruncateNormalizeAndMerge(TermIndex termIndex, int nbCandidates, Collection<TranslationCandidate> candidatesCandidates) {
		List<TranslationCandidate> list = Lists.newArrayList();
		
		/*
		 * 1. Merge
		 */
		Multimap<Term, TranslationCandidate> multimap = HashMultimap.create();
		candidatesCandidates.stream().forEach(tc -> multimap.put(tc.getTerm(), tc));
		multimap.keySet().stream().forEach(uniqueTerm -> {
			if(multimap.get(uniqueTerm).size() >= 2) {
				List<TranslationCandidate> termCandidates = Lists.newArrayList(multimap.get(uniqueTerm));
				Collections.sort(termCandidates);
				list.add(termCandidates.get(0));
			} else {
				list.add(multimap.get(uniqueTerm).iterator().next());				
			}
		});
		
		
		Collections.sort(list);
		// set rank
		for(int i = 0; i < list.size(); i++)
			list.get(i).setRank(i+1);
		List<TranslationCandidate> finalCandidates = list.subList(0, Ints.min(nbCandidates, list.size()));
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
			double wr = c.getTerm().getSpecificity();
			c.setScore(c.getScore()*getSpecificityBonusFactor(wr));
		}
	}

	private double getSpecificityBonusFactor(double specificity) {
		return specificity;
	}

	public List<TranslationCandidate> alignDico(Term sourceTerm, int nbCandidates) {
		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		Collection<String> translations = dico.getTranslations(sourceTerm.getLemma());
		
		ContextVector translatedSourceVector = AlignerUtils.translateVector(
				sourceTerm.getContextVector(),
				dico,
				AlignerUtils.TRANSLATION_STRATEGY_MOST_SPECIFIC,
				targetTermino);

		
		for(String candidateLemma:translations) {
			List<Term> terms = targetTermino.getCustomIndex(TermIndexes.LEMMA_LOWER_CASE).getTerms(candidateLemma);
			for (Term candidateTerm : terms) {
				if (candidateTerm.isContextVectorComputed()) {
					TranslationCandidate candidate = new TranslationCandidate(
							AlignmentMethod.DICTIONARY,
							candidateTerm,
							distance.getValue(translatedSourceVector, candidateTerm.getContextVector()),
							sourceTerm);
					dicoCandidates.add(candidate);
				}
			}
		}
		

		return dicoCandidates;
	}

	
	public boolean canAlignCompositional(Term sourceTerm) {
		return AlignerUtils.getSingleLemmaTerms(sourceTermino, sourceTerm)
					.stream()
					.anyMatch(slTerms -> slTerms.size() == 2);
	}

	public List<TranslationCandidate> alignCompositional(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		Preconditions.checkArgument(canAlignCompositional(sourceTerm), "Cannot align <%s> with compositional method", sourceTerm);
		
		List<List<Term>> singleLemmaTermSets = AlignerUtils.getSingleLemmaTerms(sourceTermino, sourceTerm);
		
		List<TranslationCandidate> candidates = Lists.newArrayList();
		
		for(List<Term> singleLemmaTerms:singleLemmaTermSets) {
			if(singleLemmaTerms.size() == 2) {
				candidates.addAll(alignCompositionalSize2(
						singleLemmaTerms.get(0), 
						singleLemmaTerms.get(1), 
						nbCandidates, 
						minCandidateFrequency,
						sourceTerm));
			}
		}
		
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, candidates);
	}

	public boolean canAlignSemiDistributional(Term sourceTerm) {
		return AlignerUtils.getSingleLemmaTerms(sourceTermino, sourceTerm)
				.stream()
				.anyMatch(slTerms -> slTerms.size() == 2);
	}
	
	public List<TranslationCandidate> alignSemiDistributional(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		Preconditions.checkArgument(canAlignCompositional(sourceTerm), "Cannot align <%s> with compositional method", sourceTerm);
		
		List<List<Term>> singleLemmaTermSets = AlignerUtils.getSingleLemmaTerms(sourceTermino, sourceTerm);
		
		List<TranslationCandidate> candidates = Lists.newArrayList();
		
		for(List<Term> singleLemmaTerms:singleLemmaTermSets) {
			if(singleLemmaTerms.size() == 2) {
				candidates.addAll(alignSemiDistributionalSize2Syntagmatic(
						singleLemmaTerms.get(0), 
						singleLemmaTerms.get(1), 
						nbCandidates, 
						minCandidateFrequency,
						sourceTerm));
			}
		}
		
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, candidates);

	}

	
	public List<TranslationCandidate> alignCompositionalSize2(Term lemmaTerm1, Term lemmaTerm2, int nbCandidates, int minCandidateFrequency, Term sourceTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();	
		List<TranslationCandidate> dicoCandidates1 = alignDico(lemmaTerm1, Integer.MAX_VALUE);
		List<TranslationCandidate> dicoCandidates2 = alignDico(lemmaTerm2, Integer.MAX_VALUE);
			
		candidates.addAll(combineCandidates(dicoCandidates1, dicoCandidates2, AlignmentMethod.COMPOSITIONAL, sourceTerm));
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, candidates);
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
			Collection<TranslationCandidate> candidates2, AlignmentMethod method, Term sourceTerm) {
		Collection<TranslationCandidate> combination = Sets.newHashSet();
		for(TranslationCandidate candidate1:candidates1) {
			for(TranslationCandidate candidate2:candidates2) {
				/*
				 * 1- create candidate combine terms
				 */
				CustomTermIndex index = targetTermino.getCustomIndex(TermIndexes.WORD_COUPLE_LEMMA_LEMMA);
				List<Term> candidateCombinedTerms = index.getTerms(candidate1.getTerm().getLemma() + "+" + candidate2.getTerm().getLemma());
				candidateCombinedTerms.addAll(index.getTerms(candidate2.getTerm().getLemma() + "+" + candidate1.getTerm().getLemma()));
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
					TranslationCandidate combinedCandidate = new TranslationCandidate(
							method,
							t, 
							t.getSpecificity(), // TODO Not by specificity, by distribution !!
							sourceTerm, 
							candidate1, candidate2
							);
					combinedCandidate.setExplanation(new TextExplanation(String.format("Spécificité: %.1f", t.getSpecificity())));
					combination.add(combinedCandidate);
				}
			}
		}
		return combination;
	}

	private void checkNotNull(Term sourceTerm) {
		Preconditions.checkNotNull(sourceTerm, MSG_TERM_NOT_NULL);
	}

	
		
	public List<TranslationCandidate> alignSemiDistributionalSize2Syntagmatic(Term lemmaTerm1, Term lemmaTerm2, int nbCandidates, int minCandidateFrequency, Term sourceTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();
			
		Collection<? extends TranslationCandidate> t1 = semiDistributional(lemmaTerm1, lemmaTerm2, sourceTerm);
		candidates.addAll(t1);
		Collection<? extends TranslationCandidate> t2 = semiDistributional(lemmaTerm2, lemmaTerm1, sourceTerm);
		candidates.addAll(t2);

		removeDuplicatesOnTerm(candidates);
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, candidates);
	}

	private void removeDuplicatesOnTerm(List<TranslationCandidate> candidates) {
		Set<Term> set = Sets.newHashSet();
		Iterator<TranslationCandidate> it = candidates.iterator();
		while(it.hasNext())
			if(!set.add(it.next().getTerm()))
				it.remove();
	}

	private Collection<? extends TranslationCandidate> semiDistributional(Term dicoTerm, Term vectorTerm, Term sourceTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();
		List<TranslationCandidate> dicoCandidates = alignDico(dicoTerm, Integer.MAX_VALUE);
		
		if(dicoCandidates.isEmpty())
			// Optimisation: no need to align since there is no possible combination
			return candidates;
		else {
			List<TranslationCandidate> vectorCandidates = alignDicoThenDistributional(vectorTerm, Integer.MAX_VALUE, 1);
			return combineCandidates(dicoCandidates, vectorCandidates, AlignmentMethod.SEMI_DISTRIBUTIONAL, sourceTerm);
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



	public BilingualDictionary getDico() {
		return this.dico;
	}
}
