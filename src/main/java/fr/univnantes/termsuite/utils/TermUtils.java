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
package fr.univnantes.termsuite.utils;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.splitter.CompoundUtils;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;

public class TermUtils {


	/**
	 * Most frequent first
	 */
	public static Comparator<Term> frequencyComparator = new Comparator<Term>() {
		@Override
		public int compare(Term o1, Term o2) {
			return ComparisonChain.start()
					.compare(o2.getFrequency(), o1.getFrequency())
					.result();
		}
	};

	private static final String STEMMED_INSENSITIVE_GKEY_FORMAT = "%s: %s";
	/**
	 * e.g. a: Hydroélectrique -> a: hydroelectric
	 */
	public static String stemmedInsensitiveGroupingKey(TermWord termWord) {
		return StringUtils.replaceAccents(String.format(
				STEMMED_INSENSITIVE_GKEY_FORMAT, 
				termWord.getSyntacticLabel(), 
				termWord.getWord().getStem()).toLowerCase());
	}
	
	/**
	 * e.g. a: Hydroélectrique -> a: hydroelectrique
	 */
	public static String lemmatizedInsensitiveGroupingKey(TermWord termWord) {
		return StringUtils.replaceAccents(String.format(
				STEMMED_INSENSITIVE_GKEY_FORMAT, 
				termWord.getSyntacticLabel(), 
				termWord.getWord().getLemma()).toLowerCase());		
	}



	public static String collapseText(String coveredText) {
		char[] charArray = coveredText.toCharArray();
		if(charArray.length == 0)
			return "";
		char last = charArray[0];
		StringBuilder builder = new StringBuilder();
		builder.append(last);
		for(int i=1;i<charArray.length; i++) {
			char c = charArray[i];
			if(Character.isWhitespace(c)) {
				c = TermSuiteConstants.WHITESPACE;
				if(Character.isWhitespace(last))
					continue;
			}
			builder.append(c);
			last = c;
		}
		return builder.toString().trim();
	}
	
	public static boolean isIncludedIn(Term term, Term inTerm) {
		return term.getWords().size() < inTerm.getWords().size()
				&& getPosition(term, inTerm) != -1;
	}

	public static boolean isPrefixOf(Term term, Term ofTerm) {
		return getPosition(term, ofTerm) == 0;		
	}

	public static boolean isSuffixOf(Term term, Term ofTerm) {
		return getPosition(term, ofTerm) + term.getWords().size() == ofTerm.getWords().size();				
	}

	
	/**
	 * Finds the index of appearance of a term's sub-term.
	 * 
	 * 
	 * @param subTerm
	 * 			the inner term, must be included in <code>term</code>
	 * @param term
	 * 			the container term.
	 * @return
	 * 			the starting index of <code>subTerm</code> in <code>term</code>. -1 otherwise.
	 */
	public static int getPosition(Term subTerm, Term term) {
		int startingIndex = -1;
		int j = 0;
		for(int i=0; i<term.getWords().size(); i++) {
			if(term.getWords().get(i).equals(subTerm.getWords().get(j))) {
				j++;
				if(startingIndex == -1) 
					startingIndex = i;
			} else {
				startingIndex = -1;
				j = 0;
			}
			if(j == subTerm.getWords().size())
				return startingIndex;
		}
		return -1;
	}

//	/**
//	 * 
//	 * @param l
//	 * @param t
//	 * @return
//	 */
//	public static int getGeneralFrequency(Lang l, Term t) {
//		String resName = ResourceType.GENERAL_LANGUAGE.getPath(l);
//		GeneralLanguageResource generalLanguage = new GeneralLanguageResource();
//		try {
//			generalLanguage.load(TermUtils.class.getClassLoader().getResourceAsStream(resName));
//			return generalLanguage.getFrequency(t.getLemma(), t.getPattern());
//		} catch (Exception e) {
//			throw new TermSuiteResourceException("Could not read resource " + resName, e);
//		}
//	}

	public static double getExtensionGain(Term extension, Term extensionAffix) {
		return ((double)extension.getFrequency())/extensionAffix.getFrequency();
	}

	private static final String GROUPING_KEY_FORMAT = "%s: %s";
	public static String toGroupingKey(TermWord termWord) {
		return String.format(GROUPING_KEY_FORMAT, 
				termWord.getSyntacticLabel().toLowerCase(), 
				termWord.getWord().getLemma());
	}

	
	/**
	 * 
	 * Transforms a term into a list of component sets.
	 * 
	 * This
	 * 
	 * 
	 * 
	 * @param term
	 * @return
	 */
	public static List<Set<Component>> toComponentSets(Iterable<Word> words) {
		List<Set<Component>> sets = Lists.newArrayList();
		for(Word w:words) {
			if(w.isCompound())
				sets.add(Sets.newHashSet(CompoundUtils.allSizeComponents(w)));
			else {
				sets.add(Sets.newHashSet(new Component(0, w.getLemma().length(), w.getLemma())));
			}
		}
		return sets;
	}
	
	
	/**
	 * Return the term pair indexing key that is compliant with {@link TermValueProviders#ALLCOMP_PAIRS}.
	 * 
	 * @see TermValueProviders#ALLCOMP_PAIRS
	 * @param term1
	 * 				First term of the pair
	 * @param term2
	 * 				Second term of the pair
	 * @return
	 * 			The indexing key for the given pair
	 */
	public static String getLemmaLemmaKey(Term term1, Term term2) {
		List<String> lemmas = new ArrayList<>(2);
		lemmas.add(term1.getWords().get(0).getWord().getLemma());
		lemmas.add(term2.getWords().get(0).getWord().getLemma());
		Collections.sort(lemmas);
		return String.format("%s+%s", lemmas.get(0), lemmas.get(1));
	}

	public static boolean isCompound(Term actual) {
		return actual.getWords().size() == 1 
				&& actual.getWords().get(0).getWord().isCompound();
	}

	public static String getTermLemma(Term t) {
		return t.getWords().stream()
			.map(TermWord::getWord)
			.map(Word::getLemma)
			.collect(joining(TermSuiteConstants.WHITESPACE_STRING));
	}

}
