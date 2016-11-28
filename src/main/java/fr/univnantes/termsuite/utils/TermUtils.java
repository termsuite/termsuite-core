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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.model.termino.TermValueProviders;
import fr.univnantes.termsuite.uima.TermSuiteResource;
import fr.univnantes.termsuite.uima.TermSuiteResourceException;
import fr.univnantes.termsuite.uima.engines.termino.morpho.CompoundUtils;
import fr.univnantes.termsuite.uima.resources.termino.GeneralLanguageResource;

public class TermUtils {

	private static final String MSG_NOT_AN_EXTENSION = "Term '%s' is no extension of term '%s'";


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
	
	public static void showIndex(TermIndex index, PrintStream stream) {
		Optional<Pattern> watchExpression = Optional.empty();
		showIndex(index, stream, watchExpression);
	}
		
	public static void showIndex(TermIndex index, PrintStream stream, Optional<Pattern> watchExpression) {
		for(Term term:index.getTerms()) {
			if(!watchExpression.isPresent()
					|| (watchExpression.isPresent() && watchExpression.get().matcher(term.getGroupingKey()).find())
					) {
				stream.println(term);
//				for(Term t:term.getGraphicalVariants()) 
//					stream.format("\tgraphical: %s\n" , t.getGroupingKey());
				for(TermRelation variation:index.getOutboundRelations(term)) 
					stream.format("\tsyntactic: %s\n" , variation.getTo().getGroupingKey());
			}
		}
	}

	public static void showCompounds(TermIndex index, PrintStream out, int threshhold) {
		List<Term> terms = Lists.newArrayList();
		for(Term term:index.getTerms()) {
			if(term.isCompound() && term.getFrequency() >= threshhold)
				terms.add(term);
		}
		Collections.sort(terms, frequencyComparator);
		for(Term term:terms) 
			out.println(term);
	}
	
	/**
	 * 
	 * Finds in an input term all single-word terms it is made off. 
	 * If the input term has compounds, this method will iterate 
	 * over each compound and try to find a matching swt for each compound.
	 * 
	 * This method creates an index on TermIndex based on key
	 * {@link TermIndexes#SINGLE_WORD_LEMMA}.
	 * 
	 * @param termIndex
	 * 			The {@link TermIndex} in which single word terms must be found.
	 * @param term
	 * 			The input term.
	 * @return
	 * 			The list of single word terms.
	 * 
	 * @see Term#asComponentIterator(boolean)
	 */
	public static List<Term> getSingleWordTerms(TermIndex termIndex, Term term) {
		List<Term> terms = Lists.newArrayList();
		for(TermWord tw:term.getWords()) {
			Term swt = termIndex.getTermByGroupingKey(toGroupingKey(tw));
			if(swt != null)
				terms.add(swt);
		}
		
		return terms;
	}

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
	
	public static void showContextVector(ContextVector contextVector, int topN) {
		Set<ContextVector.Entry> entries = Sets.newTreeSet(contextVector.getEntries());
		int i = 0;
		for(ContextVector.Entry e:entries) {
			i++;
			if(i>topN)
				break;
			System.out.format("\t%-12s: %d\n", e.getCoTerm().getLemma(), e.getNbCooccs());
		}
	}

	/**
	 * Returns the strictness of t1 based on t2, i.e. the ratio of appearance
	 * in an occurrence that do not overlap with t2. 
	 * 
	 * @param t1
	 * 			the term to analyze
	 * @param t2
	 * 			the base term
	 * @return
	 * 			fstrict(t1) / f(t1)
	 */
	public static double getStrictness(OccurrenceStore store, Term t1, Term t2) {
		Collection<TermOccurrence> occ1 = Lists.newArrayList(store.getOccurrences(t1));
		TermOccurrenceUtils.removeOverlaps(store.getOccurrences(t2), occ1);
		double t1Strict = occ1.size();
		double t1F = t1.getFrequency();
		return t1Strict / t1F;
	}
	
	
	/**
	 * 
	 * Finds in a {@link TermIndex} the biggest extension affix term of a term depending 
	 * on a base term.
	 * 
	 * For example, the term "offshore wind turbine" is an extension of 
	 * "wind turbine". The extension affix is the term "offshore".
	 * 
	 * @param termIndex
	 * 			The term index that both terms belong to.
	 * @param base
	 * 			The base term
	 * @param extension
	 * 			The extension term
	 * @return
	 * 		the extension affix found in <code>termIndex</code>, <code>null</code> if none
	 * 		has been found.
	 * @throws IllegalArgumentException if <code>extension</code> id not an 
	 * 			extension of the term <code>base</code>.
	 */
	public static Term getExtensionAffix(TermIndex termIndex, Term base, Term extension) {
		int index = TermUtils.getPosition(base, extension);
		if(index == -1)
			throw new IllegalStateException(String.format(MSG_NOT_AN_EXTENSION, 
					extension,
					base)
				);

		/*
		 *  true if prefix, false if suffix
		 */
		boolean isPrefix = false;
		if(index == 0)
			isPrefix = true;
		else if(index + base.getWords().size() == extension.getWords().size())
			isPrefix = false; // suffix
		else {
			/*
			 * Happens sometimes. 
			 * 
			 * base = 		'nnnn: hd spring spring spring' 
			 * extension = 	'nn: spring spring'
			 * 
			 * Do nothing.
			 */
		}
		
		if(isPrefix) 
			return findBiggestSuffix(
					termIndex, 
					extension.getWords().subList(index + base.getWords().size(), extension.getWords().size())
				);
		else
			return findBiggestPrefix(
					termIndex, 
					extension.getWords().subList(0, index)
				);
	}

	/**
	 * Finds in a {@link TermIndex} the biggest prefix of a sequence of
	 * {@link TermWord}s that exists as a term.
	 * 
	 * @param termIndex
	 * 			the term index
	 * @param words
	 * 			the initial sequence of {@link TermWord}s
	 * @return
	 * 			A {@link Term} found in <code>termIndex</code> that makes the
	 * 			biggest possible prefix sequence for <code>words</code>.
	 */
	public static Term findBiggestPrefix(TermIndex termIndex, List<TermWord> words) {
		Term t;
		String gKey;
		for(int i = words.size(); i > 0 ; i--) {
			gKey = TermSuiteUtils.getGroupingKey(words.subList(0, i));
			t = termIndex.getTermByGroupingKey(gKey);
			if(t!=null)
				return t;
		}
		return null;
	}
	

	/**
	 * Finds in a {@link TermIndex} the biggest suffix of a sequence of
	 * {@link TermWord}s that exists as a term.
	 * 
	 * @param termIndex
	 * 			the term index
	 * @param words
	 * 			the initial sequence of {@link TermWord}s
	 * @return
	 * 			A {@link Term} found in <code>termIndex</code> that makes the
	 * 			biggest possible suffix sequence for <code>words</code>.

	 */
	public static Term findBiggestSuffix(TermIndex termIndex, List<TermWord> words) {
		Term t;
		String gKey;
		for(int i = 0; i < words.size() ; i++) {
			gKey = TermSuiteUtils.getGroupingKey(words.subList(i, words.size()));
			t = termIndex.getTermByGroupingKey(gKey);
			if(t!=null)
				return t;
		}
		return null;
	}
	
	public static boolean isIncludedIn(Term term, Term inTerm) {
		return getPosition(term, inTerm) != -1;
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

	/**
	 * 
	 * @param l
	 * @param t
	 * @return
	 */
	public static int getGeneralFrequency(Lang l, Term t) {
		String resName = TermSuiteResource.GENERAL_LANGUAGE.getPath(l);
		GeneralLanguageResource generalLanguage = new GeneralLanguageResource();
		try {
			generalLanguage.load(TermUtils.class.getClassLoader().getResourceAsStream(resName));
			return generalLanguage.getFrequency(t.getLemma(), t.getPattern());
		} catch (Exception e) {
			throw new TermSuiteResourceException("Could not read resource " + resName, e);
		}
	}

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
				sets.add(Sets.newHashSet(new Component(w.getLemma(), 0, w.getLemma().length())));
			}
		}
		return sets;
	}
	
	
	
	/**
	 * Return the term pair indexing key that is compliant with {@link TermValueProviders#WORD_LEMMA_LEMMA_PROVIDER}.
	 * 
	 * @see TermValueProviders#WORD_LEMMA_LEMMA_PROVIDER
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

	public static Collection<Term> getExtensions(TermIndex termIndex, Term term) {
		return termIndex.getOutboundRelations(term, RelationType.HAS_EXTENSION)
				.stream()
				.map(TermRelation::getTo)
				.collect(Collectors.toSet());
	}
		
	public static boolean isExtension(TermIndex termIndex, Term term, Term extension) {
		return termIndex.getOutboundRelations(term, RelationType.HAS_EXTENSION)
			.stream()
			.filter(tv -> tv.getTo().equals(extension))
			.findAny().isPresent();
	}
	
	
	public static Collection<TermRelation> getVariations(TermIndex termIndex, Term t) {
		return termIndex.getOutboundRelations(t,
				RelationType.SYNTACTICAL, 
				RelationType.MORPHOLOGICAL,
				RelationType.GRAPHICAL,
				RelationType.SYNONYMIC,
				RelationType.DERIVES_INTO,
				RelationType.IS_PREFIX_OF);
	}

	public static Collection<TermRelation> getBases(TermIndex termIndex, Term current) {
		return termIndex.getInboundRelations(current,
				RelationType.SYNTACTICAL, 
				RelationType.MORPHOLOGICAL,
				RelationType.GRAPHICAL,
				RelationType.SYNONYMIC,
				RelationType.DERIVES_INTO,
				RelationType.IS_PREFIX_OF);
	}
}
