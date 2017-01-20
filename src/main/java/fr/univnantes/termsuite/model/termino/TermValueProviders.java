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
package fr.univnantes.termsuite.model.termino;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.splitter.CompoundUtils;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.CollectionUtils;
import fr.univnantes.termsuite.utils.Pair;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermSuiteConstants;
import fr.univnantes.termsuite.utils.TermUtils;

public class TermValueProviders {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermValueProviders.class);
	
	private static final Collection<String> EMPTY_COLLECTION = Lists.newArrayList();
	
	public static final TermValueProvider TERM_SINGLE_WORD_LEMMA_PROVIDER = new AbstractTermValueProvider(TermIndexes.SINGLE_WORD_LEMMA) {
		
		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			if(term.isSingleWord())
				return Lists.newArrayList(term.getWords().get(0).getWord().getLemma());
			return EMPTY_COLLECTION;
		}
	};

	public static final TermValueProvider TERM_LEMMA_LOWER_CASE_PROVIDER = new AbstractTermValueProvider(TermIndexes.LEMMA_LOWER_CASE) {
		public java.util.Collection<String> getClasses(Terminology termino, Term term) {
			return ImmutableList.of(term.getLemma().toLowerCase());
		};
	};

	public static final TermValueProvider TERM_NOCLASS_PROVIDER = new AbstractTermValueProvider(TermIndexes.TERM_NOCLASS) {
		private String value = "noclass";
		public java.util.Collection<String> getClasses(Terminology termino, Term term) {
			return ImmutableList.of(value);
		};
	};

	public static final TermValueProvider WORD_SWT_GROUPING_KEYS_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_LEMMA_IF_SWT) {
		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			List<String> swtGroupingKeys = Lists.newArrayListWithCapacity(term.getWords().size());
			for(TermWord tw:term.getWords()) {
				if(termino.getTerms().get(TermUtils.toGroupingKey(tw)) != null) 
					swtGroupingKeys.add(tw.toGroupingKey());
			}
			return swtGroupingKeys;
		}
	};
	
	public static final TermValueProvider WORD_LEMMA_STEM_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_COUPLE_LEMMA_STEM) {

		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			List<String> lemmas = Lists.newArrayListWithCapacity(term.getWords().size());
			
			Map<String, String> stems = new HashMap<String, String>();
			for(TermWord w:term.getWords()) {
				if (w.getWord().getLemma() == null || w.getWord().getLemma().isEmpty()) {
					LOGGER.warn("lemma is null or empty: " + w);
					continue;
				} else if(TermSuiteConstants.TERM_MATCHER_LABELS.contains(w.getSyntacticLabel())) {
						lemmas.add(w.getWord().getNormalizedLemma());
					if(w.getWord().getStem() == null || w.getWord().getStem().isEmpty()) {
						LOGGER.warn("stem is null or empty: " + w);
					} else 
						stems.put(w.getWord().getNormalizedLemma(), w.getWord().getNormalizedStem());
				}
			}	
			Collections.sort(lemmas);
			List<String> keys = Lists.newArrayListWithCapacity(lemmas.size());
			
			for (int i = 0 ; i < lemmas.size(); i++) {
				for (int j = i + 1; j < lemmas.size(); j++) {
					StringBuilder sb = new StringBuilder();
					sb.append(lemmas.get(i));
					sb.append(TermSuiteConstants.PLUS);
					sb.append(stems.get(lemmas.get(j)));
					keys.add(sb.toString());
				}
			}
			

			return keys;
		}
	};
		
	/**
	 * Provides all lemma-lemma pairs found in the term.
	 * 
	 * Ex1: offshore wind energy
	 * 			--> {offshore+wind, energy+wind, offshore+energy}
	 * 
	 * Performs two iterations when there are compounds
	 * 
	 * Ex2: horizontal-axis wind turbine
	 * 			it1 --> {horizontal-axis+turbine, horizontal-axis+turbine, wind+turbine}
	 * 			it2 --> {axis+horizontal, axis+turbine, axis+wind, horizontal+turbine, horizontal+wind, wind+turbine}
	 * 			total -->  it1 U it2
	 */
	public static final TermValueProvider ALLCOMP_PAIRS = new AbstractTermValueProvider(TermIndexes.ALLCOMP_PAIRS) {

		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			Set<Pair<Component>> componentPairs = Sets.newHashSet();
			Set<Word> significantWords = Sets.newHashSetWithExpectedSize(term.getWords().size());
			
			/*
			 * 1- select significant words
			 */
			for(TermWord w:term.getWords()) {
				if(w.isSwt())
					significantWords.add(w.getWord());
			}
			
			/*
			 * 2- Adds intra-compound component pairs (for compound words only)
			 */
			significantWords.stream()
				.filter(Word::isCompound)
				.forEach(w -> componentPairs.addAll(CompoundUtils.innerComponentPairs(w)));
			
			/*
			 * 3- Add inter-word component pairs
			 */
			componentPairs.addAll(CollectionUtils.combineAndProduct(TermUtils.toComponentSets(significantWords)));
			
			
			/*
			 * 4- transform each component pair to class
			 */
			Set<String> classes = new HashSet<>();
				
			componentPairs.stream()
				.forEach(pair -> classes.addAll(CompoundUtils.toIndexStrings(pair)));
			return classes;
		}
	};

	/**
	 * Provides all lemmas of the terms
	 */
	public static final TermValueProvider WORD_LEMMA_PROVIDER = new AbstractTermValueProvider(TermIndexes.ALLCOMP_PAIRS) {

		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			Set<String> classes = Sets.newHashSet();
			for(TermWord w:term.getWords()) 
				if(w.isSwt())
					classes.add(w.getWord().getLemma().toLowerCase());
			return classes;
			
		}
	};

	public static final TermValueProvider PREFIXATION_LEMMAS = new AbstractTermValueProvider(TermIndexes.PREFIXATION_LEMMAS) {
		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			return toRelationPairs(termino, term, RelationType.IS_PREFIX_OF);
		}

	};
	
	private static Collection<String> toRelationPairs(Terminology termino, Term term, RelationType relType) {
		Set<TermRelation> prefixations = new HashSet<>();
		for(TermWord tw:term.getWords()) {
			Term t =termino.getTerms().get(TermUtils.toGroupingKey(tw));
			if(t!=null) {
				prefixations.addAll(termino.getInboundRelations(t, relType));
				prefixations.addAll(termino.getOutboundRelations(t, relType));
			}
		}
		return prefixations.stream()
				.map(rel -> String.format(PAIR_FORMAT, rel.getFrom().getLemma(), rel.getTo().getLemma()))
				.collect(Collectors.toSet());
	}

	private static final String PAIR_FORMAT = "%s+%s";

	public static final TermValueProvider DERIVATION_LEMMAS = new AbstractTermValueProvider(TermIndexes.DERIVATION_LEMMAS) {
		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			return toRelationPairs(termino, term, RelationType.DERIVES_INTO);
		}
	};

	private static class FirstLettersProvider extends AbstractTermValueProvider {
		
		private int nbLetters;
		private Locale locale;

		
		public FirstLettersProvider(int nbLetters, Locale locale) {
			super(nbLetters + "-first-letters");
			this.nbLetters = nbLetters;
			this.locale = locale;
		}


		@Override
		public Collection<String> getClasses(Terminology termino, Term term) {
			if(term.getWords().size() == 1) {
//				return ImmutableList.of();
				Word word = term.getWords().get(0).getWord();
				if(word.getLemma().length() < 5)
					return ImmutableList.of();
				else {
					String substring = StringUtils.replaceAccents(word.getLemma().toLowerCase(locale).substring(0, 4));
					return ImmutableList.of(substring);
				}
			}
			StringBuilder builder = new StringBuilder();
			String normalizedStem;
			int i = 0;
			for(TermWord tw:term.getWords()) {
				if(i>0) {
					builder.append(TermSuiteConstants.COLONS);
				}
				normalizedStem = tw.getWord().getNormalizedStem();
				if(normalizedStem.length() > nbLetters)
					builder.append(normalizedStem.substring(0, nbLetters).toLowerCase(locale));
				else
					builder.append(normalizedStem.toLowerCase(locale));
				i++;
			}
			if(builder.length() >= nbLetters)
				return ImmutableList.of(builder.toString());
			else
				return ImmutableList.of();
		}
	};

	public static TermValueProvider get(String key, Locale locale) {
		switch(key) {
		case TermIndexes.FIRST_LETTERS_1:
			return new FirstLettersProvider(1, locale);
		case TermIndexes.FIRST_LETTERS_2:
			return new FirstLettersProvider(2, locale);
		case TermIndexes.FIRST_LETTERS_3:
			return new FirstLettersProvider(3, locale);
		case TermIndexes.FIRST_LETTERS_4:
			return new FirstLettersProvider(4, locale);
		default:
			return get(key);
		}
	}

	public static TermValueProvider get(String key) {
		switch(key) {
		case TermIndexes.SINGLE_WORD_LEMMA: return TERM_SINGLE_WORD_LEMMA_PROVIDER;
		case TermIndexes.TERM_NOCLASS: return TERM_NOCLASS_PROVIDER;
		case TermIndexes.SWT_GROUPING_KEYS: return WORD_SWT_GROUPING_KEYS_PROVIDER;
		case TermIndexes.WORD_LEMMA: return WORD_LEMMA_PROVIDER;
		case TermIndexes.LEMMA_LOWER_CASE: return TERM_LEMMA_LOWER_CASE_PROVIDER;
		case TermIndexes.WORD_COUPLE_LEMMA_STEM: return WORD_LEMMA_STEM_PROVIDER;
		case TermIndexes.ALLCOMP_PAIRS: return ALLCOMP_PAIRS;
		case TermIndexes.DERIVATION_LEMMAS: return DERIVATION_LEMMAS;
		case TermIndexes.PREFIXATION_LEMMAS: return PREFIXATION_LEMMAS;
		default:
			throw new IllegalStateException("Unknown index: " + key);
		}

	}

}
