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
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.LemmaStemHolder;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.utils.TermSuiteConstants;

public class TermClassProviders {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermClassProviders.class);
	public static final String KEY_TERM_LEMMA = "term-lemma";
	public static final String KEY_TERM_LEMMA_LOWER_CASE = "term-lemma-lower-case";
	public static final String KEY_WORD_LEMMA = "word-lemma";
	public static final String KEY_WORD_COUPLE_LEMMA_LEMMA = "word-lemma-lemma";
	public static final String KEY_WORD_COUPLE_LEMMA_STEM = "word-lemma-stem";
	public static final String KEY_WORD_LEMMA_LOWER_CASE = "word-lemma-lower-case";
	public static final String KEY_3RD_FIRST_LETTERS = "3rd-first-letters";
	private static final char JOIN_CHAR = ':';
	

	public static TermClassProvider getNFirstLettersNormalizedClassProvider(final int n, final Locale locale) {
		return new AbstractTermClassProvider(KEY_3RD_FIRST_LETTERS) {
			@Override
			public Collection<String> getClasses(Term term) {
				if(term.getWords().size() == 1)
					// do not gather sw term with that method
					return ImmutableList.of();
				StringBuilder builder = new StringBuilder();
				String normalizedStem;
				int i = 0;
				for(TermWord tw:term.getWords()) {
					if(i>0) {
						builder.append(JOIN_CHAR);
					}
					normalizedStem = tw.getWord().getNormalizedStem();
					if(normalizedStem.length() > n)
						builder.append(normalizedStem.substring(0, n).toLowerCase(locale));
					else
						builder.append(normalizedStem.toLowerCase(locale));
					i++;
				}
				if(builder.length() >= n)
					return ImmutableList.of(builder.toString());
				else
					return ImmutableList.of();
			}
		};
	}

	public static final TermClassProvider TERM_LEMMA_LOWER_CASE_PROVIDER = new AbstractTermClassProvider(KEY_TERM_LEMMA_LOWER_CASE) {
		public java.util.Collection<String> getClasses(Term term) {
			return ImmutableList.of(term.getLemma().toLowerCase());
		};
	};
	
	public static final TermClassProvider WORD_LEMMA_PROVIDER = new AbstractTermClassProvider(KEY_WORD_LEMMA) {
		@Override
		public Collection<String> getClasses(Term term) {
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

	
	public static final TermClassProvider WORD_LEMMA_STEM_PROVIDER = new AbstractTermClassProvider(KEY_WORD_COUPLE_LEMMA_STEM) {

		@Override
		public Collection<String> getClasses(Term term) {
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
		
	
	public static final TermClassProvider WORD_LEMMA_LEMMA_PROVIDER = new AbstractTermClassProvider(KEY_WORD_COUPLE_LEMMA_LEMMA) {

		@Override
		public Collection<String> getClasses(Term term) {
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
	
	public static final TermSingleClassProvider WORD_LEMMA_LOWER_CASE = new TermSingleClassProvider(KEY_WORD_LEMMA_LOWER_CASE) {
		@Override
		public String getClass(Term term) {
			return Character.toString(Character.toLowerCase(term.getGroupingKey().charAt(0)));
		}
	};
	
	
	public static final Map<String, TermClassProvider> classProviders = ImmutableMap.of(
			KEY_TERM_LEMMA_LOWER_CASE, TERM_LEMMA_LOWER_CASE_PROVIDER,
			KEY_WORD_LEMMA_LOWER_CASE, WORD_LEMMA_LOWER_CASE,
			KEY_WORD_LEMMA, WORD_LEMMA_PROVIDER,
			KEY_WORD_COUPLE_LEMMA_STEM, WORD_LEMMA_STEM_PROVIDER,
			KEY_WORD_COUPLE_LEMMA_LEMMA, WORD_LEMMA_LEMMA_PROVIDER
		);
}
