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
package eu.project.ttc.models.index;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.LemmaStemHolder;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.selectors.HasSingleWordVariationSelector;
import eu.project.ttc.models.index.selectors.TermSelector;
import eu.project.ttc.utils.TermSuiteConstants;

public class TermValueProviders {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermValueProviders.class);
	
	public static final TermValueProvider TERM_SINGLE_WORD_LEMMA_PROVIDER = new AbstractTermValueProvider(TermIndexes.SINGLE_WORD_LEMMA) {
		
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			if(term.isSingleWord())
				return Lists.newArrayList(term.getWords().get(0).getWord().getLemma());
			return null;
		}
	};

	public static final TermValueProvider TERM_LEMMA_LOWER_CASE_PROVIDER = new AbstractTermValueProvider(TermIndexes.LEMMA_LOWER_CASE) {
		public java.util.Collection<String> getClasses(TermIndex termIndex, Term term) {
			return ImmutableList.of(term.getLemma().toLowerCase());
		};
	};

	public static final TermValueProvider TERM_NOCLASS_PROVIDER = new AbstractTermValueProvider(TermIndexes.TERM_NOCLASS) {
		private String value = "noclass";
		public java.util.Collection<String> getClasses(TermIndex termIndex, Term term) {
			return ImmutableList.of(value);
		};
	};

	public static final TermValueProvider WORD_LEMMA_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_LEMMA) {
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			List<String> lemmas = Lists.newArrayListWithCapacity(term.getWords().size());
			Iterator<LemmaStemHolder> it = term.asComponentIterator();
			LemmaStemHolder c;
			while(it.hasNext()) {
				c = it.next();
				if(c.getLemma() == null) {
					LOGGER.warn("Lemma is null for " + c);
				} else {
					if(c.isLemmaSet())
						lemmas.add(c.getLemma());
					else
						lemmas.add(NO_LEMMA_SET);
				}
				
			}
			return lemmas;
		}
	};

	
	public static final TermValueProvider WORD_LEMMA_STEM_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_COUPLE_LEMMA_STEM) {

		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
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
	protected static final String NO_LEMMA_SET = "__no_lemma_set__";
		
	public static final TermValueProvider WORD_LEMMA_LEMMA_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_COUPLE_LEMMA_LEMMA) {

		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			List<String> lemmas = Lists.newArrayListWithCapacity(term.getWords().size());
			for(TermWord w:term.getWords()) {
				if (w.getWord().getLemma() == null || w.getWord().getLemma().isEmpty()) {
					LOGGER.warn("lemma is null or empty: " + w);
					continue;
				} else if(TermSuiteConstants.TERM_MATCHER_LABELS.contains(w.getSyntacticLabel())) {
					lemmas.add(w.getWord().getLemma());
					if(w.getWord().isCompound()) {
						for(Component c:w.getWord().getComponents()) {
							if(c.isLemmaSet())
								lemmas.add(c.getLemma());
							else 
								lemmas.add(NO_LEMMA_SET);
								
						}
					}
				}
			}	
			Collections.sort(lemmas);
			List<String> keys = Lists.newArrayListWithCapacity((lemmas.size()*(lemmas.size()-1))/2);

			for (int i = 0 ; i < lemmas.size(); i++) {
				for (int j = i + 1; j < lemmas.size(); j++) {
					StringBuilder sb = new StringBuilder();
					sb.append(lemmas.get(i));
					sb.append(TermSuiteConstants.PLUS);
					sb.append(lemmas.get(j));
					keys.add(sb.toString());
				}
			}
			
			return keys;
			
		}
	};
	
	public static final TermSingleValueProvider WORD_LEMMA_LOWER_CASE = new TermSingleValueProvider(TermIndexes.WORD_LEMMA_LOWER_CASE) {
		@Override
		public String getClass(TermIndex termIndex, Term term) {
			return Character.toString(Character.toLowerCase(term.getGroupingKey().charAt(0)));
		}
	};
	
	private static final Map<String, TermValueProvider> valueProviders = Maps.newHashMap();

	static {
		valueProviders.put(TermIndexes.SINGLE_WORD_LEMMA, TERM_SINGLE_WORD_LEMMA_PROVIDER);
		valueProviders.put(TermIndexes.TERM_NOCLASS, TERM_NOCLASS_PROVIDER);
		valueProviders.put(TermIndexes.LEMMA_LOWER_CASE, TERM_LEMMA_LOWER_CASE_PROVIDER);
		valueProviders.put(TermIndexes.WORD_LEMMA_LOWER_CASE, WORD_LEMMA_LOWER_CASE);
		valueProviders.put(TermIndexes.WORD_LEMMA, WORD_LEMMA_PROVIDER);
		valueProviders.put(TermIndexes.WORD_COUPLE_LEMMA_STEM, WORD_LEMMA_STEM_PROVIDER);
		valueProviders.put(TermIndexes.WORD_COUPLE_LEMMA_LEMMA, WORD_LEMMA_LEMMA_PROVIDER);
	}

	public static TermValueProvider get(String key) {
		return valueProviders.get(key);
	}

	/*
	 * TODO Bad design, unify model with all indexes even though
	 * 		that do not need TermIndex
	 */
	public static TermValueProvider get(String indexName, TermIndex termIndex) {
		switch(indexName) {
		case TermIndexes.TERM_HAS_PREFIX_LEMMA:
			return new SelectorTermValueProvider(
						TermIndexes.WORD_LEMMA,
						new HasSingleWordVariationSelector(VariationType.IS_PREFIX_OF), 
						termIndex
					);
		case TermIndexes.TERM_HAS_DERIVATES_LEMMA:
			return new SelectorTermValueProvider(
						TermIndexes.WORD_LEMMA,
						new HasSingleWordVariationSelector(VariationType.DERIVES_INTO), 
						termIndex
					);
		default:
			return get(indexName);
		}
	}
	
	
	static class SelectorTermValueProvider extends AbstractTermValueProvider {
		private static final String ERR_MSG_NO_SUCH_VALUE_PROVIDER = "No such TermValueProvider: %s";

		TermSelector selector;
		TermValueProvider termValueProvider;
		TermIndex termIndex;
		
		public SelectorTermValueProvider(String name, TermSelector selector, TermIndex termIndex) {
			super(name);
			this.termValueProvider = TermValueProviders.get(this.getName());
			Preconditions.checkNotNull(
					this.termValueProvider,
					ERR_MSG_NO_SUCH_VALUE_PROVIDER,
					this.getName());
			this.selector = selector;
			this.termIndex = termIndex;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			if(selector.select(termIndex, term)) {
				return termValueProvider.getClasses(termIndex, term);
			} else 
				return Collections.EMPTY_SET;
		}
	}
}
