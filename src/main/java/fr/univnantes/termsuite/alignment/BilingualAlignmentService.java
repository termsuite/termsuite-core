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
package fr.univnantes.termsuite.alignment;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.google.inject.Inject;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.splitter.CompoundUtils;
import fr.univnantes.termsuite.framework.SourceLanguage;
import fr.univnantes.termsuite.framework.TargetLanguage;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.index.providers.AllComponentPairsProvider;
import fr.univnantes.termsuite.metrics.Cosine;
import fr.univnantes.termsuite.metrics.ExplainedValue;
import fr.univnantes.termsuite.metrics.Levenshtein;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.metrics.TextExplanation;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.resources.BilingualDictionary;
import fr.univnantes.termsuite.utils.Pair;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermUtils;
import fr.univnantes.termsuite.utils.WordUtils;
 
/** 
 * 
 * 
 * 
 * @author Damien Cram
 * 
 */
public class BilingualAlignmentService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BilingualAlignmentService.class);
	private static final String MSG_TERM_NOT_NULL = "Source term must not be null";
	static final String MSG_REQUIRES_SIZE_2_LEMMAS = "The term %s must have exactly two single-word terms (single-word terms: %s)";
	private static final String MSG_SEVERAL_VECTORS_NOT_COMPUTED = "Several terms have no context vectors in target terminology (nb terms with vector: {}, nb terms without vector: {})";
	
	@Inject
	private BilingualDictionary dico;
	
	@Inject
	@SourceLanguage
	private TerminologyService sourceTermino;

	@Inject
	@TargetLanguage
	private TerminologyService targetTermino;

	@Inject
	@SourceLanguage
	private IndexService sourceIndexes;

	@Inject
	@TargetLanguage
	private IndexService targetIndexes;


	@Inject(optional=true)
	private SimilarityDistance distance = new Cosine();
	
	@Inject(optional=true)
	private Map<Term, Term> manualDico = new HashMap<>();
	

	public BilingualAlignmentService addTranslation(TermService sourceTerm, TermService targetTerm) {
		return addTranslation(sourceTerm.getTerm(), targetTerm.getTerm());
	}

	public BilingualAlignmentService addTranslation(Term sourceTerm, Term targetTerm) {
		Preconditions.checkNotNull(sourceTerm);
		Preconditions.checkNotNull(targetTerm);
		manualDico.put(sourceTerm, targetTerm);
		return this;
	}

	
	/**
	 * 
	 * @param sourceLemma
	 * @param targetLemmas
	 * @return
	 */
	public BilingualAlignmentService addTranslation(String sourceLemma, String targetLemma) {
		if(sourceIndexes.getIndex(TermIndexType.WORD_LEMMAS).getTerms(sourceLemma).isEmpty()) 
			throw new TermSuiteException("No term found in source termino with lemma: " + sourceLemma);
		else if(targetIndexes.getIndex(TermIndexType.WORD_LEMMAS).getTerms(targetLemma).isEmpty()) 
			throw new TermSuiteException("No term found in target termino with lemma: " + targetLemma);
		else {
			for(Term sourceTerm:sourceIndexes.getIndex(TermIndexType.WORD_LEMMAS).getTerms(sourceLemma))
				for(Term targetTerm:targetIndexes.getIndex(TermIndexType.WORD_LEMMAS).getTerms(targetLemma))
					manualDico.put(sourceTerm, targetTerm);
		}
		return this;
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
	public List<TranslationCandidate> alignDicoThenDistributional(TermService sourceTerm, int nbCandidates, int minCandidateFrequency) {
		checkNotNull(sourceTerm);

		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		/*
		 * 1- find direct translation of the term in the dictionary
		 */
		dicoCandidates.addAll(sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, alignDico(sourceTerm, Integer.MAX_VALUE)));
		applySpecificityBonus(dicoCandidates);

		
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

	public boolean canAlignNeoclassical(TermService sourceTerm) {
		return sourceTerm.isCompound() 
				&& sourceTerm.getWords().get(0).getWord().getCompoundType() == CompoundType.NEOCLASSICAL;
	}

	public List<TranslationCandidate> alignNeoclassical(Term sourceTerm, int nbCandidates,
			int minCandidateFrequency) {
		return alignNeoclassical(sourceTermino.asTermService(sourceTerm), nbCandidates, minCandidateFrequency);
	}


	
	/**
	 * 
	 * Align a term using the TermSuite's neoclassical alignment method.
	 * 
	 * This method behaves as follows: 
	 * 	
	 * 
	 * @param sourceTerm
	 * 			the source term to align
	 * @param nbCandidates
	 * 			the maximum number of {@link TranslationCandidate} returned 
	 * @param minCandidateFrequency
	 * 			the minimum frequency of returned translation candidates
	 * @return
	 * 		the sorted list {@link TranslationCandidate} produced by this method 
	 * 		or an empty list if the term could not be aligned using the neoclassical method.
	 * 
	 * @see #canAlignNeoclassical(Term)
	 * @see CompoundType#NEOCLASSICAL
	 * 		
	 */
	public List<TranslationCandidate> alignNeoclassical(TermService sourceTerm, int nbCandidates,
			int minCandidateFrequency) {
		
		if(!canAlignNeoclassical(sourceTerm))
			return Lists.newArrayList();

		Word sourceWord = sourceTerm.getWords().get(0).getWord();
		Component sourceNeoclassicalAffix = sourceWord.getNeoclassicalAffix();
		String sourceNeoclassicalAffixString = WordUtils.getComponentSubstring(sourceWord, sourceNeoclassicalAffix);
		
		/*
		 * 1. try to translate the neoclassical affix 
		 * 
		 * E.g. aéro (fr) -> aero (en)
		 */
		Set<String> targetNeoclassicalAffixes = Sets.newHashSet();
		// 1a. find translation in dico
		targetNeoclassicalAffixes.addAll(dico.getTranslations(sourceNeoclassicalAffixString));
		// some dicos also appends the hyphen to affixes
		targetNeoclassicalAffixes.addAll(dico.getTranslations(sourceNeoclassicalAffixString+"-"));
		// clean hyphens returned by dicos
		targetNeoclassicalAffixes = targetNeoclassicalAffixes.stream()
			.map(affix-> affix.replaceAll("^-", "").replaceAll("-$", ""))
			.collect(Collectors.toSet());

		
		
		/*
		 * 2. Index target candidates by morphological extensions when the extension 
		 * is a valid swt in the target termino.
		 * 
		 * E.g. électricité -> hydroélectricité
		 */
		Map<TermService, TermService> targetCandidatesBySWTExtension = Maps.newHashMap();
		Set<TermService> targetCandidatesHavingSameAffix = Sets.newHashSet();
		for(TermService targetCandidate:targetTermino.getTerms()) {
			Word targetCompound = targetCandidate.getWords().get(0).getWord();
			if(targetCandidate.isCompound() && targetCompound.getCompoundType() == CompoundType.NEOCLASSICAL) {
				String targetNeoclassicalAffixString = WordUtils.getComponentSubstring(targetCompound, targetCompound.getNeoclassicalAffix());
				
				boolean isValidTargetCandidate = false;
				// Case1: we have translations from dico for neoclassical affix
				if(!targetNeoclassicalAffixes.isEmpty()) 
					isValidTargetCandidate = targetNeoclassicalAffixes.contains(targetNeoclassicalAffixString);
				// Case2: we don't, then we have to test validity on graphical pure graphical equality
				else
					isValidTargetCandidate = StringUtils
						.replaceAccents(targetNeoclassicalAffixString).toLowerCase()
						.equals(StringUtils.replaceAccents(sourceNeoclassicalAffixString).toLowerCase());
					
				if (isValidTargetCandidate) {
					targetCandidatesHavingSameAffix.add(targetCandidate);

					Collection<TermService> targetExtensions = getMorphologicalExtensionsAsTerms(
							targetTermino,
							targetIndexes.getIndex(TermIndexType.LEMMA_LOWER_CASE),
							targetCandidate, 
							targetCompound.getNeoclassicalAffix());

					for (TermService morphologicalExtensin : targetExtensions)
						targetCandidatesBySWTExtension.put(morphologicalExtensin, targetCandidate);
				}
			}
		}
		
		/*
		 * 3. try recursive alignment on neoclassical extensions
		 */
		Set<TermService> possibleSourceExtensions = Sets.newHashSet(getMorphologicalExtensionsAsTerms(
				sourceTermino,
				sourceIndexes.getIndex(TermIndexType.LEMMA_LOWER_CASE), 
				sourceTerm, 
				sourceNeoclassicalAffix));
		List<TranslationCandidate> candidates = Lists.newArrayList();
		for(TermService sourceExtension:possibleSourceExtensions) {
			// recursive alignment on extension
			List<TranslationCandidate> recursiveCandidates = alignSize2(sourceExtension, nbCandidates, minCandidateFrequency);
			
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

	/**
	 * E.g. Given the compound [hydro|électricité] and the component [hydro], the method should return the 
	 * term [électricité]
	 * 
	 * 
	 * @param termino
	 * @param compound
	 * @param component
	 * @return
	 */
	public Collection<TermService> getMorphologicalExtensionsAsTerms(
			TerminologyService terminoService, 
			TermIndex lemmaLowerCaseIndex, 
			TermService compound, 
			Component component) {
		Preconditions.checkArgument(compound.isSingleWord());
		Preconditions.checkArgument(compound.isCompound());
		Preconditions.checkArgument(compound.getWords().get(0).getWord().getComponents().contains(component));
		
		Word compoundWord = compound.getWords().get(0).getWord();
		LinkedList<Component> extensionComponents = Lists.newLinkedList(compoundWord.getComponents());
		extensionComponents.remove(component);
		
		if(!(component.getBegin() == 0 || component.getEnd() == compound.getLemma().length()))
			return Lists.newArrayList();

		
		Set<String> possibleExtensionLemmas = Sets.newHashSet();
		possibleExtensionLemmas.add(compound.getLemma().substring(
				extensionComponents.getFirst().getBegin(), 
				extensionComponents.getLast().getEnd()));
			
		if(extensionComponents.size() > 1) {
			LinkedList<Component> allButLast = Lists.newLinkedList(extensionComponents);
			Component last = allButLast.removeLast();
			String lemma = compound.getLemma().substring(allButLast.getFirst().getBegin(), last.getBegin())
						+ last.getLemma();
			possibleExtensionLemmas.add(lemma);
		}
		
		List<TermService> extensionTerms = Lists.newArrayList();
		for(String s:possibleExtensionLemmas) 
			for(Term term:lemmaLowerCaseIndex.getTerms(s.toLowerCase()))
				extensionTerms.add(terminoService.asTermService(term));
		
		return extensionTerms;
	}

	
	private static final Levenshtein LEVENSHTEIN = new Levenshtein();

	public List<TranslationCandidate> alignGraphically(AlignmentMethod method, TermService sourceTerm, int nbCandidates, Collection<TermService> targetTerms) {
		Preconditions.checkArgument(sourceTerm.isSingleWord());
		for(TermService targetTerm:targetTerms)
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

	
	public List<TranslationCandidate> alignDistributional(TermService sourceTerm, int nbCandidates,
			int minCandidateFrequency) {
		Queue<TranslationCandidate> alignedCandidateQueue = MinMaxPriorityQueue.maximumSize(nbCandidates).create();
		ContextVector sourceVector = sourceTerm.getContext();
		if(sourceVector == null)
			return new ArrayList<>();
		ContextVector translatedSourceVector = translateVector(
				sourceVector,
				dico,
				TRANSLATION_STRATEGY_MOST_SPECIFIC,
				targetTermino);
		ExplainedValue v;
		int nbVectorsNotComputed = 0;
		int nbVectorsComputed = 0;
		for(TermService targetTerm:targetTermino.terms().filter(TermService::isSingleWord).collect(Collectors.toList())) {
			if(targetTerm.getFrequency() < minCandidateFrequency)
				continue;
			if(targetTerm.getContext() != null) {
				nbVectorsComputed++;
				v = distance.getExplainedValue(translatedSourceVector, targetTerm.getContext());
				TranslationCandidate candidate = new TranslationCandidate(
						AlignmentMethod.DISTRIBUTIONAL,
						targetTerm, 
						v.getValue(), 
						sourceTerm,
						v.getExplanation());
				alignedCandidateQueue.add(candidate);
			}
		};
		if(nbVectorsNotComputed > 0) {
			LOGGER.warn(MSG_SEVERAL_VECTORS_NOT_COMPUTED, nbVectorsComputed, nbVectorsNotComputed);	
		}
		
		// sort alignedCandidates
		List<TranslationCandidate> alignedCandidates = Lists.newArrayListWithCapacity(alignedCandidateQueue.size());
		alignedCandidates.addAll(alignedCandidateQueue);
		normalizeCandidateScores(alignedCandidates);
		return Lists.newArrayList(alignedCandidateQueue);
	}
	
	
	/**
	 * Runs bilingual alignment on a source term.
	 * 
	 * 
	 * @param sourceTerm
	 * 				The source term to align
	 * @param nbCandidates
	 * 				The maximum number of target translation candidates
	 * @param minCandidateFrequency
	 * 				The minimum frequency allowed for target translation candidates
	 * @return
	 * 			A ranked list of {@link TranslationCandidate}
	 */
	public List<TranslationCandidate> align(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		return alignSwtTermList(
				sourceTermino.asTermService(sourceTerm).getSwts().collect(toList()), 
				nbCandidates, 
				minCandidateFrequency, 
				true);
	}

	private List<TranslationCandidate> alignSwtTermList(List<TermService> terms, int nbCandidates, int minCandidateFrequency, boolean allowDistributionalAlignment) {
		Preconditions.checkArgument(!terms.isEmpty());
		
		if(terms.size() == 1) {
			return alignSize2(terms.get(0), nbCandidates, minCandidateFrequency, allowDistributionalAlignment);			
		} else if(terms.size() == 2) {
			String indexingKey = TermUtils.getLemmaLemmaKey(terms.get(0).getTerm(), terms.get(1).getTerm());
					
			Optional<Term> recursiveTerm = sourceIndexes.getIndex(TermIndexType.ALLCOMP_PAIRS)
					.getTerms(indexingKey)
					.stream()
					.filter(t -> t.getSwtSize() == 2)
					.max(TermProperty.FREQUENCY.getComparator(false));
			
			if(recursiveTerm.isPresent())
				return alignSize2(
						sourceTermino.asTermService(recursiveTerm.get()), 
						nbCandidates, 
						minCandidateFrequency, 
						allowDistributionalAlignment);
			else
				return Lists.newArrayList();
		} else {
			
			Collection<TranslationCandidate> combinedCandidates = Lists.newArrayList();
			
			/*
			 * Cut the swt list in two lists
			 */
			for(int i=1; i<=terms.size()-1;i++) {
				// cut at index i
				List<TermService> swtTermList1 = terms.subList(0, i);
				List<TermService> swtTermList2 = terms.subList(i, terms.size());
				
				List<TranslationCandidate> candidates1 = alignSwtTermList(swtTermList1, nbCandidates, minCandidateFrequency, allowDistributionalAlignment);
				if(!candidates1.isEmpty()) {
					
					/*
					 *  do not allow distributional again if it has been used for candidates one already.
					 */
					boolean candidates1Distributional = candidates1.get(0).getMethod() == AlignmentMethod.DISTRIBUTIONAL 
							|| candidates1.get(0).getMethod() == AlignmentMethod.SEMI_DISTRIBUTIONAL;
					
					List<TranslationCandidate> candidates2 = alignSwtTermList(
							swtTermList2, 
							nbCandidates,
							minCandidateFrequency, 
							allowDistributionalAlignment && !candidates1Distributional
							);
					
					combinedCandidates.addAll(combineMWTCandidates(candidates1, candidates2, terms));
				}
			}
			return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, combinedCandidates);
		}
	}


	public List<TranslationCandidate> alignSize2(Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		return alignSize2(sourceTermino.asTermService(sourceTerm), nbCandidates, minCandidateFrequency);
	}

	/**
	 * alias for {@link #align(Term, int, int, <code>true</code>)}
	 * 
	 * @param sourceTerm
	 * @param nbCandidates
	 * @param minCandidateFrequency
	 * @return
	 */
	public List<TranslationCandidate> alignSize2(TermService sourceTerm, int nbCandidates, int minCandidateFrequency) {
		return alignSize2(sourceTerm, nbCandidates, minCandidateFrequency, true);
	}

	private static final String ERR_MSG_BAD_SOURCE_LEMMA_SET_SIZE = "Unexpected size for a source lemma set: %s. Expected size: 2";
	/**
	 * 
	 * 
	 * @param sourceTerm
	 * @param nbCandidates
	 * @param minCandidateFrequency
	 * @param allowDistributionalAlignment 
	 * @return
	 */
	public List<TranslationCandidate> alignSize2(TermService sourceTerm, int nbCandidates, int minCandidateFrequency, boolean allowDistributionalAlignment) {
		Preconditions.checkNotNull(sourceTerm);
		List<TranslationCandidate> mergedCandidates = Lists.newArrayList();
		List<List<TermService>> sourceLemmaSets = getSourceSingleLemmaTerms(sourceTerm);
		for(List<TermService> sourceLemmaSet:sourceLemmaSets) {
			Preconditions.checkState(sourceLemmaSet.size() == 1 || sourceLemmaSet.size() == 2, 
					ERR_MSG_BAD_SOURCE_LEMMA_SET_SIZE, sourceLemmaSet);
			if(sourceLemmaSet.size() == 1) {
				if(allowDistributionalAlignment)
					mergedCandidates.addAll(alignDicoThenDistributional(sourceLemmaSet.get(0), 3*nbCandidates, minCandidateFrequency));
				else
					mergedCandidates.addAll(alignDico(sourceLemmaSet.get(0), 3*nbCandidates));
			} else if(sourceLemmaSet.size() == 2) {
				List<TranslationCandidate> compositional = Lists.newArrayList();
				try {
					compositional.addAll(alignCompositionalSize2(sourceLemmaSet.get(0), sourceLemmaSet.get(1), nbCandidates, minCandidateFrequency, sourceTerm));
				} catch(RequiresSize2Exception e) {
					// Do nothing
				}
				mergedCandidates.addAll(compositional);
				if(mergedCandidates.isEmpty() && allowDistributionalAlignment) {
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

	private List<TranslationCandidate> sortTruncateNormalizeAndMerge(TerminologyService termino, int nbCandidates, Collection<TranslationCandidate> candidatesCandidates) {
		List<TranslationCandidate> list = Lists.newArrayList();
		
		/*
		 * 1. Merge
		 */
		Multimap<TermService, TranslationCandidate> multimap = HashMultimap.create();
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
	private void applySpecificityBonus(List<TranslationCandidate> list) {
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

	public List<TranslationCandidate> alignDico(TermService sourceTerm, int nbCandidates) {
		List<TranslationCandidate> dicoCandidates = Lists.newArrayList();
		
		if(manualDico.containsKey(sourceTerm)) {
			return Lists.newArrayList((
						new TranslationCandidate(
							AlignmentMethod.DICTIONARY,
							targetTermino.asTermService(manualDico.get(sourceTerm)),
							1,
							sourceTerm)
						));
		} else {
			if(sourceTerm.getContext() != null) {
				
				ContextVector translatedSourceVector = translateVector(
						sourceTerm.getContext(),
						dico,
						TRANSLATION_STRATEGY_MOST_SPECIFIC,
						targetTermino);
		
				for(String candidateLemma:dico.getTranslations(sourceTerm.getLemma())) {
					List<Term> terms = targetIndexes.getIndex(TermIndexType.LEMMA_LOWER_CASE).getTerms(candidateLemma);
					for (Term candidateTerm : terms) {
						if (candidateTerm.getContext() != null) {
							TranslationCandidate candidate = new TranslationCandidate(
									AlignmentMethod.DICTIONARY,
									targetTermino.asTermService(candidateTerm),
									distance.getValue(translatedSourceVector, candidateTerm.getContext()),
									sourceTerm);
							dicoCandidates.add(candidate);
						}
					}
				}
			
			}
			return dicoCandidates;
		}
	}

	
	public boolean canAlignCompositional(TermService sourceTerm) {
		return getSourceSingleLemmaTerms(sourceTerm)
					.stream()
					.anyMatch(slTerms -> slTerms.size() == 2);
	}

	public List<TranslationCandidate> alignCompositional(TermService sourceTerm, int nbCandidates, int minCandidateFrequency) {
		Preconditions.checkArgument(canAlignCompositional(sourceTerm), "Cannot align <%s> with compositional method", sourceTerm);
		
		List<List<TermService>> singleLemmaTermSets = getSourceSingleLemmaTerms(sourceTerm);
		
		List<TranslationCandidate> candidates = Lists.newArrayList();
		
		for(List<TermService> singleLemmaTerms:singleLemmaTermSets) {
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

	public boolean canAlignSemiDistributional(TermService sourceTerm) {
		return getSourceSingleLemmaTerms(sourceTerm)
				.stream()
				.anyMatch(slTerms -> slTerms.size() == 2);
	}
	
	public List<TranslationCandidate> alignSemiDistributional(TermService sourceTerm, int nbCandidates, int minCandidateFrequency) {
		Preconditions.checkArgument(canAlignCompositional(sourceTerm), "Cannot align <%s> with compositional method", sourceTerm);
		
		List<List<TermService>> singleLemmaTermSets = getSourceSingleLemmaTerms(sourceTerm);
		
		List<TranslationCandidate> candidates = Lists.newArrayList();
		
		for(List<TermService> singleLemmaTerms:singleLemmaTermSets) {
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

	
	public List<TranslationCandidate> alignCompositionalSize2(TermService lemmaTerm1, TermService lemmaTerm2, int nbCandidates, int minCandidateFrequency, TermService sourceTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();	
		List<TranslationCandidate> dicoCandidates1 = alignDico(lemmaTerm1, Integer.MAX_VALUE);
		List<TranslationCandidate> dicoCandidates2 = alignDico(lemmaTerm2, Integer.MAX_VALUE);
			
		candidates.addAll(combineSWTCandidates(dicoCandidates1, dicoCandidates2, sourceTerm));
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, candidates);
	}

	
	
	private AllComponentPairsProvider allComponentPairsProvider = new AllComponentPairsProvider();
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
	private Collection<TranslationCandidate> combineSWTCandidates(Collection<TranslationCandidate> candidates1,
			Collection<TranslationCandidate> candidates2, Object sourceTerm) {
		Collection<TranslationCandidate> combination = Sets.newHashSet();
		for(TranslationCandidate candidate1:candidates1) {
			for(TranslationCandidate candidate2:candidates2) {
				/*
				 * 1- create candidate combine terms
				 */
				String key1 = candidate1.getTerm().getLemma() + "+" + candidate2.getTerm().getLemma();
				List<Term> candidateCombinedTerms = targetIndexes.getIndex(TermIndexType.ALLCOMP_PAIRS).getTerms(key1);
				String key2 = candidate2.getTerm().getLemma() + "+" + candidate1.getTerm().getLemma();
				candidateCombinedTerms.addAll(targetIndexes.getIndex(TermIndexType.ALLCOMP_PAIRS).getTerms(key2));
				if(candidateCombinedTerms.isEmpty())
					continue;
				
				/*
				 * 2- Avoids retrieving too long terms by keeping the ones that have 
				 * the lowest number of lemma+lemma keys.
				 */
				final Map<Term, Collection<String>> termLemmaLemmaKeys = Maps.newHashMap();
				for(Term t:candidateCombinedTerms)
					termLemmaLemmaKeys.put(t, allComponentPairsProvider.getClasses(t));
				Collections.sort(candidateCombinedTerms, new Comparator<Term>() { 
					@Override
					public int compare(Term o1, Term o2) {
						return Integer.compare(
								termLemmaLemmaKeys.get(o1).size(), 
								termLemmaLemmaKeys.get(o2).size());
					}
				});
				List<TermService> filteredTerms = Lists.newArrayList();
				int minimumNbClasses = termLemmaLemmaKeys.get(candidateCombinedTerms.get(0)).size();
				for(Term t:candidateCombinedTerms) {
					if(termLemmaLemmaKeys.get(t).size() == minimumNbClasses)
						filteredTerms.add(targetTermino.asTermService(t));
					else 
						break;
				}
				
				/*
				 * 3- Create candidates from filtered terms
				 */
				for(TermService t:filteredTerms) {
					TranslationCandidate combinedCandidate = new TranslationCandidate(
							getCombinedMethod(candidate1, candidate2),
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
	
	
	private Collection<TranslationCandidate> combineMWTCandidates(Collection<TranslationCandidate> candidates1,
			Collection<TranslationCandidate> candidates2, Object sourceTerm) {
		ensureHasExtensionRelationsComputred(targetTermino);
		
		Collection<TranslationCandidate> combinations = Sets.newHashSet();
		for(TranslationCandidate candidate1:candidates1) {
			Collection<TermService> extensions1 = targetTermino.extensions(candidate1.getTerm())
					.map(RelationService::getTo)
					.collect(toSet());
			for(TranslationCandidate candidate2:candidates2) {
				Set<TermService> commonExtensions = targetTermino.extensions(candidate2.getTerm())
						.map(RelationService::getTo)
						.filter(ext-> extensions1.contains(ext))
						.collect(Collectors.toSet());
				Optional<Integer> minSize = commonExtensions.stream().map(t->t.getWords().size()).sorted().findFirst();
				if(minSize.isPresent()) {
					commonExtensions.stream().filter(t->t.getWords().size() == minSize.get()).forEach(targetTerm-> {
						combinations.add(new TranslationCandidate(
								AlignmentMethod.COMPOSITIONAL, 
								targetTerm, 
								candidate1.getScore()*candidate2.getScore(), 
								sourceTerm, 
								candidate1, candidate2
								));
					});
					
				}
			}
		}
		return combinations;
	}


	private boolean ensuredExtensionsAreComputed = false;
	private void ensureHasExtensionRelationsComputred(TerminologyService termino) {
		if(!ensuredExtensionsAreComputed) {
			if(!termino.extensions().findAny().isPresent()) 
				throw new IllegalStateException(String.format("No %s relation found in termino %s", RelationType.HAS_EXTENSION, termino));
			ensuredExtensionsAreComputed = true;
		}
	}

	private AlignmentMethod getCombinedMethod(TranslationCandidate candidate1, TranslationCandidate candidate2) {
		if(candidate1.getMethod() == AlignmentMethod.DISTRIBUTIONAL || candidate1.getMethod() == AlignmentMethod.SEMI_DISTRIBUTIONAL)
			return AlignmentMethod.SEMI_DISTRIBUTIONAL;
		else if(candidate2.getMethod() == AlignmentMethod.DISTRIBUTIONAL || candidate2.getMethod() == AlignmentMethod.SEMI_DISTRIBUTIONAL)
			return AlignmentMethod.SEMI_DISTRIBUTIONAL;
		else
			return AlignmentMethod.COMPOSITIONAL;
	}

	private void checkNotNull(TermService sourceTerm) {
		Preconditions.checkNotNull(sourceTerm, MSG_TERM_NOT_NULL);
	}

	
		
	public List<TranslationCandidate> alignSemiDistributionalSize2Syntagmatic(TermService lemmaTerm1, TermService lemmaTerm2, int nbCandidates, int minCandidateFrequency, TermService sourceTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();
			
		Collection<? extends TranslationCandidate> t1 = semiDistributional(lemmaTerm1, lemmaTerm2, sourceTerm);
		candidates.addAll(t1);
		Collection<? extends TranslationCandidate> t2 = semiDistributional(lemmaTerm2, lemmaTerm1, sourceTerm);
		candidates.addAll(t2);

		removeDuplicatesOnTerm(candidates);
		return sortTruncateNormalizeAndMerge(targetTermino, nbCandidates, candidates);
	}

	private void removeDuplicatesOnTerm(List<TranslationCandidate> candidates) {
		Set<TermService> set = Sets.newHashSet();
		Iterator<TranslationCandidate> it = candidates.iterator();
		while(it.hasNext())
			if(!set.add(it.next().getTerm()))
				it.remove();
	}

	private Collection<? extends TranslationCandidate> semiDistributional(TermService dicoTerm, TermService vectorTerm, TermService sourceTerm) {
		List<TranslationCandidate> candidates = Lists.newArrayList();
		List<TranslationCandidate> dicoCandidates = alignDico(dicoTerm, Integer.MAX_VALUE);
		
		if(dicoCandidates.isEmpty())
			// Optimisation: no need to align since there is no possible combination
			return candidates;
		else {
			List<TranslationCandidate> vectorCandidates = alignDicoThenDistributional(vectorTerm, Integer.MAX_VALUE, 1);
			return combineSWTCandidates(dicoCandidates, vectorCandidates, sourceTerm);
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
	
	
	
	
	public static final int TRANSLATION_STRATEGY_PRORATA = 1;
	public static final int TRANSLATION_STRATEGY_MOST_FREQUENT = 2;
	public static final int TRANSLATION_STRATEGY_MOST_SPECIFIC = 3;
	private static final int TRANSLATION_STRATEGY_EQUI_REPARTITION = 4;
	
	
	/**
	 *
	 * Translates all {@link ContextVector} components (i.e. its coTerms) into
	 * the target language of this aligner by the mean of one of the available 
	 * strategy :
	 *  - {@link TRANSLATION_STRATEGY_MOST_FREQUENT}
	 *  - {@link TRANSLATION_STRATEGY_PRORATA}
	 *  - {@link TRANSLATION_STRATEGY_EQUI_REPARTITION} 
	 *  - {@link TRANSLATION_STRATEGY_MOST_SPECIFIC} 
	 *
	 * @see BilingualDictionary
	 * @param sourceVector
	 * 			The source context vector object to be translated into target language
	 * @param dictionary
	 * 			The dico used in the translation process
	 * @param translationStrategy
	 * 			The translation strategy of the <code>sourceVector</code>. 
	 * 			Two possible values: {@link TRANSLATION_STRATEGY_MOST_FREQUENT}
	 * 							     {@link TRANSLATION_STRATEGY_PRORATA} 
	 * 							     {@link TRANSLATION_STRATEGY_EQUI_REPARTITION} 
	 * 							     {@link TRANSLATION_STRATEGY_MOST_SPECIFIC} 
	 * @return
	 * 			The translated context vector
	 */
	public ContextVector translateVector(ContextVector sourceVector, 
			BilingualDictionary dictionary, int translationStrategy, TerminologyService targetTermino) {
		
		
		ContextVector targetVector = new ContextVector();
		
		for(ContextVector.Entry entry:sourceVector.getEntries()) {
			Set<Term> translations = Sets.newHashSet();
			for(String targetLemma:dictionary.getTranslations(entry.getCoTerm().getLemma())) {
				Collection<Term> translatedTerms = targetIndexes.getIndex(TermIndexType.SWT_LEMMAS_SWT_TERMS_ONLY).getTerms(targetLemma);
				if(!translatedTerms.isEmpty()) 
					translations.add(translatedTerms.iterator().next());
			}
			switch (translationStrategy) {
			case TRANSLATION_STRATEGY_PRORATA:
				fillTargetVectorSProrata(targetVector, entry, translations);
				break;
			case TRANSLATION_STRATEGY_MOST_FREQUENT:
				fillTargetVectorSMost(targetVector, entry, translations, TermProperty.FREQUENCY);
				break;
			case TRANSLATION_STRATEGY_MOST_SPECIFIC:
				fillTargetVectorSMost(targetVector, entry, translations, TermProperty.SPECIFICITY);
				break;
			case TRANSLATION_STRATEGY_EQUI_REPARTITION:
				fillTargetVectorSEquiRepartition(targetVector, entry, translations);
				break;
			default:
				throw new IllegalArgumentException("Invalid translation strategy: " + translationStrategy);
			}
		}
		return targetVector;
	}
	

	/**
	 * This method implements the strategy {@link #TRANSLATION_STRATEGY_PRORATA} 
	 * for context vector translation.
	 * 
	 * Explanation of strategy:
	 * 
	 * Example of source term in french : chat <noir: 10, chien: 3>
	 * 
	 * Example of candidate translations for "noir" from dico: black, dark
	 * Example of candidate translations for "chien" from dico: dog
	 * 
	 * Suppose that frequencies in target term index are : 
	 *   - black : 35
	 *   - dark : 15
	 *   - dog : 7
	 *   
	 * The translated vector would be : <black: 7, dark: 3, dog: 3>
	 * 
	 * because :
	 *   - total frequency in target term index for term "noir" is 35 + 15 = 50,
	 *     and 7 = ( 35 / 50 ) * 10 for "black"
	 *     and 3 = ( 15 / 50 ) * 10 for "dark"
	 *   - total frequency in target term index for term "dog" is 7,
	 *     and 3 = ( 7 / 7 ) * 3
	 *     
	 * 
	 * @param translatedVector
	 * 			the target vector to be fill 
	 * @param sourceTermEntry
	 * 			the source vector's component to translated and add to target vector
	 * @param candidateTranslations
	 * 			the candidate translations of the <code>sourceTermEntry</code> given by the
	 * 			bilingual dictionary.
	 */
	private static void fillTargetVectorSProrata(ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry, Set<Term> candidateTranslations) {
		/*
		 * Do the cross product of translation frequencies
		 */
		int totalFreqInTargetTermino = 0;
		for(Term tt : candidateTranslations) 
			totalFreqInTargetTermino += tt.getFrequency();
		
		for(Term targetTerm:candidateTranslations) {
			int prorataCooccs = targetTerm.getFrequency() * sourceTermEntry.getNbCooccs() / totalFreqInTargetTermino;
			translatedVector.addEntry(targetTerm, prorataCooccs, sourceTermEntry.getAssocRate());
		}
	}
	
	/**
	 * This method implements the {@value #TRANSLATION_STRATEGY_MOST_FREQUENT} 
	 * strategy for context vector translation.
	 * 
	 * 
	 * Explanation of strategy:
	 * 
	 * Example of source term in french : chat <noir: 10, chien: 3>
	 * 
	 * Example of candidate translations for "noir" from dico: black, dark
	 * Example of candidate translations for "chien" from dico: dog
	 * 
	 * Suppose that frequencies in target term index are : 
	 *   - black : 35
	 *   - dark : 15
	 *   - dog : 7
	 *   
	 * The translated vector would be : <black: 10, dog: 3>
	 * 
	 * @param translatedVector
	 * 			the target vector to be fill 
	 * @param sourceTermEntry
	 * 			the source vector's component to translated and add to target vector
	 * @param candidateTranslations
	 * 			the candidate translations of the <code>sourceTermEntry</code> given by the
	 * 			bilingual dictionary.
	 * @param termMeasure 
	 * 
	 */
	private static void fillTargetVectorSMost(ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry, Set<Term> candidateTranslations, TermProperty termProperty) {
		fillTargetVectorWithMostProperty(translatedVector, sourceTermEntry,
				candidateTranslations, termProperty);
	}
	
	
	/**
	 * 
	 * Explanation of strategy:
	 * 
	 * Example of source term in french : chat <noir: 10, chien: 3>
	 * 
	 * Example of candidate translations for "noir" from dico: black, dark
	 * Example of candidate translations for "chien" from dico: dog
	 * 
	 *   
	 * The translated vector would be : <black: 5,  dark: 5, dog: 3>
	 * 
	 * @param translatedVector
	 * @param sourceTermEntry
	 * @param candidateTranslations
	 */
	private static  void fillTargetVectorSEquiRepartition(ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry, Set<Term> candidateTranslations) {
		/*
		 * Do the cross product of translation frequencies
		 */
		for(Term targetTerm:candidateTranslations) {
			int nbCooccs = sourceTermEntry.getNbCooccs()/candidateTranslations.size();
			translatedVector.addEntry(
					targetTerm, 
					nbCooccs, 
					sourceTermEntry.getAssocRate()/candidateTranslations.size());
		}
	}

	private static void fillTargetVectorWithMostProperty(
			ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry,
			Set<Term> candidateTranslations, final TermProperty termProperty) {
		Preconditions.checkArgument(termProperty.isNumeric());
		
		Term mostFrequent = null;
		double maxValue = -1d;
		
		for(Term t:candidateTranslations) {
			if(t.isNumericValueGT(termProperty, maxValue)) {
				maxValue = t.getFrequency();
				mostFrequent = t;
			}
		}
		
		if(mostFrequent != null) 
			/*
			 * mostFrequent would be null if candidateTranslations is empty
			 */
			translatedVector.addEntry(mostFrequent, sourceTermEntry.getNbCooccs(), sourceTermEntry.getAssocRate());
	}


	/**
	 * 
	 * Gives the list of all possible single lemma terms decompositino for a complex term.
	 * 
	 * 
	 * @param termino
	 * @param term
	 * @return
	 */
	public List<List<TermService>> getSourceSingleLemmaTerms(TermService term) {
		List<TermService> swtTerms = term.getSwts().collect(toList());
		List<List<TermService>> lemmaSets = Lists.newArrayList();
		if(swtTerms.size() == 1) {
			
			if(term.getWords().size() > 1) {
				LOGGER.warn("Could not apply single lemma term decomposition for term {}. Expected at least two inner swt terms, but got {}", term, swtTerms);
				return Lists.newArrayList();
			}
			
			// sourceTerm is swtTerms.get(0);
			if(term.isCompound()) {
				lemmaSets.add(Lists.newArrayList(term));
				for(Pair<Component> pair:CompoundUtils.innerContiguousComponentPairs(term.getWords().get(0).getWord())) {
					for(Term swt1:getSwtSetFromComponent(sourceIndexes.getIndex(TermIndexType.LEMMA_LOWER_CASE), pair.getElement1())) {
						for(Term swt2:getSwtSetFromComponent(sourceIndexes.getIndex(TermIndexType.LEMMA_LOWER_CASE), pair.getElement2())) {
							Pair<Term> pair2 = new Pair<Term>(swt1, swt2);
							lemmaSets.add(pair2.toList().stream()
									.map(t->sourceTermino.asTermService(t))
									.collect(toList()));
							
						}
					}
				}
			} else {
				lemmaSets.add(Lists.newArrayList(term));
			}
		} else {
			if(swtTerms.size() == 2) {
				lemmaSets.add(swtTerms);			
			} else 
				throw new RequiresSize2Exception(term.getTerm(), swtTerms.stream().map(TermService::getTerm).collect(toList()));
			
		}
		return lemmaSets;
	}


	public static Set<Term> getSwtSetFromComponent(TermIndex lemmaLowerCaseIndex, Component c) {
		Set<Term> terms = new HashSet<>();
		terms.addAll(lemmaLowerCaseIndex.getTerms(c.getLemma()));
		terms.addAll(lemmaLowerCaseIndex.getTerms(c.getSubstring()));
		return terms;
	}

}
