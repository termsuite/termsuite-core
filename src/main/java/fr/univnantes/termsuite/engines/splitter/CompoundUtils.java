
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

package fr.univnantes.termsuite.engines.splitter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.Pair;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

/**
 * 
 * A set of helper methods for compound words and for iteration
 * over word components (see {@link TermValueProviders}).
 * 
 * @author Damien Cram
 *
 */
public class CompoundUtils {

	private static final String ERR_MSG_CANNOT_MERGE_AN_EMPTY_SET = "Cannot merge an empty set of component";
	private static final String ERR_MSG_COMPONENTS_OVERLAP = "Cannot merge two components if they overlap. Got [%s,%s] followed by [%s,%s].";
	private static final String ERR_MSG_COMPONENT_OFFSET_ARE_TOO_BIG = "Component %s does not belong to word %s (length=%s), because offsets [%s,%s] are too big.";
	private static final String ERR_WMSG_WORD_LEMMA_NULL = "Word lemma needs to not be null";
	
	
	/**
	 * Returns all possible components for a compound word 
	 * by combining its atomic components.
	 * 
	 * E.g. ab|cd|ef returns
	 * 		abcdef,
	 * 		ab, cdef,
	 * 		abcd, ef,
	 * 		cd
	 * 
	 * 
	 * @param word the compound word
	 * @return
	 * 			the list of all possible component lemmas
	 */
	public static List<Component> allSizeComponents(Word word) {
		Set<Component> components = Sets.newHashSet();
		for(int nbComponents=word.getComponents().size();
				nbComponents > 0 ;
				nbComponents--) {
			
			for(int startIndex = 0;
					startIndex <= word.getComponents().size() - nbComponents;
					startIndex++) {
				List<Component> toMerge = Lists.newArrayListWithExpectedSize(nbComponents);
				
				for(int i = 0; i<nbComponents; i++) 
					toMerge.add(word.getComponents().get(startIndex + i));
				
				components.add(merge(word, toMerge));
			}
		}
		return Lists.newArrayList(components);
	}

	/**
	 * 
	 * Merges <code>n</code> consecutive components of a compound
	 * word into a single {@link Component} object. 
	 * 
	 * The <code>lemma</code> of the returned {@link Component} is
	 * the concatenation of the 1st to n-1-th param components' substring 
	 * and the last param component's <code>lemma</code>.
	 * 
	 * 
	 * @param word
	 * 			The compound word
	 * @param components
	 * 			The list of consecutive components of the word to merge
	 * @return
	 * 			The merged component
	 * 
	 * @throws IllegalArgumentException
	 * 				when the <code>components</code> param is empty
	 * @throws IllegalArgumentException
	 * 				when the <code>components</code> are not consecutive
	 * @throws IllegalArgumentException
	 * 				when the components offsets do not match with the <code>word</code> size.
	 */
	public static Component merge(Word word, Iterable<? extends Component> components) {
		Preconditions.checkNotNull(word.getLemma(), ERR_WMSG_WORD_LEMMA_NULL);
		 
		
		Iterator<? extends Component> it = components.iterator();
		Preconditions.checkArgument(it.hasNext(), ERR_MSG_CANNOT_MERGE_AN_EMPTY_SET);
		
		Component lastComponent = it.next();
		int begin = lastComponent.getBegin();
		StringBuilder lemmaBuilder = new StringBuilder();
		while (it.hasNext()) {
			Component cur = it.next();
			Preconditions.checkArgument(
					cur.getBegin() >= lastComponent.getEnd(),
					ERR_MSG_COMPONENTS_OVERLAP,
					lastComponent.getBegin(), lastComponent.getEnd(),
					cur.getBegin(), cur.getEnd()
				);
			
			
			Preconditions.checkArgument(
					cur.getEnd() <= word.getLemma().length(),
					ERR_MSG_COMPONENT_OFFSET_ARE_TOO_BIG,
					cur, word, word.getLemma().length(),
					cur.getBegin(),cur.getEnd()
					);
			lemmaBuilder.append(lastComponent.getSubstring());
			
			if(lastComponent.getEnd() < cur.getBegin())
				/*
				 * Fills the gap with the lemma substring
				 */
				lemmaBuilder.append(word.getLemma().substring(lastComponent.getEnd(), cur.getBegin()));
			
			lastComponent = cur;
		}
		lemmaBuilder.append(lastComponent.getLemma());
		String substring = word.getLemma().substring(begin, lastComponent.getEnd());
		return new Component(begin, lastComponent.getEnd(), substring, lemmaBuilder.toString());
	}
	
	
	/**
	 * @see #merge(Word, Iterable)
	 * 
	 * @param word
	 * @param begin
	 * @param end
	 * @return
	 */
	public static Component merge(Word word, int begin, int end) {
		return merge(word, word.getComponents().subList(begin, end+1));
	}


	
	/**
	 * 
	 * Produces the set of all pairs of non-overlapping components
	 * for a given word.
	 * 
	 * E.g. ab|cd|ef returns:
	 * 		ab+cd, ab+ef, cd+ef, ab+cdef, abcd+ef
	 * 			
	 * 
	 * @param word
	 * 			the compound word
	 * @return
	 * 			the exhaustive list of pairs.
	 */
	public static List<Pair<Component>> innerComponentPairs(Word word) {
		Set<Pair<Component>> pairs = Sets.newHashSet();
		List<Component> components = allSizeComponents(word);
		Component c1,c2;
		Pair<Component> pair;
		for(int i=0; i<components.size(); i++) {
			c1 = components.get(i);
			for(int j=i+1; j<components.size(); j++) {
				c2 = components.get(j);
				pair = new Pair<Component>(c1, c2);
				if(pair.getElement1().getEnd() <= pair.getElement2().getBegin())
					// no overlap
					pairs.add(pair);
			}
		}
		return Lists.newArrayList(pairs);
	}
	
	
	public static String toClassString(String s1, String s2) {
		boolean ordered = s1.compareTo(s2) <= 0;
		StringBuilder sb = new StringBuilder();
		sb.append(ordered ? s1 : s2);
		sb.append(TermSuiteConstants.PLUS);
		sb.append(ordered ? s2 : s1);
		return sb.toString();
	}
	
	
	public static Set<String> toIndexStrings(Pair<Component> pair) {
		Set<String> classes = new HashSet<>(4);
		
		// substring+substring
		classes.add(toClassString(
				pair.getElement1().getSubstring(), 
				pair.getElement2().getSubstring()));
		
		
		// lemma+lemma
		classes.add(toClassString(
			pair.getElement1().getLemma(), 
			pair.getElement2().getLemma()));

		// lemma+substring
		classes.add(toClassString(
			pair.getElement1().getLemma(), 
			pair.getElement2().getSubstring()));

		// substring+lemma
		classes.add(toClassString(
			pair.getElement1().getSubstring(), 
			pair.getElement2().getLemma()));
		
		
		return classes;
	}

	/**
	 * 
	 * <b>WARNING: This method does not behave as {@link #innerComponentPairs(Word)}.</b> 
	 * This method enforces that returned pairs cover the input word completely and 
	 * without any overlap.
	 *
	 * Example 1: with a word that is not a compound, it returns an empty list.
	 * 
	 * Example 2: with a word that is a size-2 compound, it returns the only pair of lemmas possible:
	 * 	
	 * <code>
	 * 	w = "ab|cd"
	 *  returnedPairs are [["ab","cd"]]
	 * </code>
	 * 
	 * Example 3: with a word that is a size-3 compound, it returns two pairs of lemmas:
	 * 	
	 * <code>
	 * 	w = "ab|cd|ef"
	 *  returnedPairs are [["ab","cded"], ["abcd","ef"]]
	 * </code>
	 * 
	 * Example 4: with a word that is a size-n compound, it returns n-1 pairs of lemmas:
	 * 
	 * <code>
	 * 	w = "comp1|comp2|...|compn"
	 *  returnedPairs are [
	 *  	["comp1","comp2comp3...compn"],
	 *  	["comp1comp2","comp3comp4...compn"], 
	 *  	..., 
	 *  	["comp1comp2...compn-1","compn"]
	 *  ]
	 * </code>
	 * 
	 * 
	 * @param word
	 * 			The input compound word
	 */
	public static Set<Pair<Component>> innerContiguousComponentPairs(Word word) {
		Set<Pair<Component>> pairs = new HashSet<>();
		if(word.isCompound()) {
			int n = word.getComponents().size();
			for(int i=0; i<n-1; i++) {
				pairs.add(new Pair<Component>(
						merge(word, word.getComponents().subList(0, i+1)), 
						merge(word, word.getComponents().subList(i+1, n))));
			}
		}
		return pairs;
	}
	
	
	/**
	 * 
	 * Gives all possible component of a compound word
	 * at given index.
	 * 
	 * Example with word ab|cd|ef|gg 
	 * 
	 * word[0] can be any of: ab, abcd, or abcdef 
	 * word[1] can be any of: cd, cdef, cdefgg, ef, efgg, or gg
	 * word[2] can be any of: ef, efgg, gg
	 * word[3] can only be: gg 
	 * 
	 * 
	 * Explanation: 
	 * 
	 * word[0] can be:
	 * 	- aa if word is considered to be composed as ab|cd|ef|gg, 
	 * 	- abcd if word is considered to be composed as abcd|ef|gg or abcd|efgg 
	 * 	- abcdef if word is considered to be composed as abcdef|gg 
	 * 
	 * idem with word[1], word[2] and word[3]
	 * 
	 * @param word
	 * @param i
	 * @return
	 */
	public static Set<Component> getPossibleComponentsAt(Word word, int i) {
		if(word.isCompound()) {
			switch (word.getComponents().size()) {
			case 0:
				return ImmutableSet.of();
			case 1:
				return i==0 ? ImmutableSet.of(word.getComponents().get(0)) : ImmutableSet.of();
			case 2:
				switch(i){
				case 0: return ImmutableSet.of(word.getComponents().get(0));
				case 1: return ImmutableSet.of(word.getComponents().get(1));
				default: return ImmutableSet.of();
				}
			case 3:
				return i < word.getComponents().size() ? 
						SIZE_3_POSSIBLE_BEGIN_END_AT_INDEX.get(i)
								.stream()
								.map(pair -> CompoundUtils.merge(word, pair.getElement1(), pair.getElement2()))
								.collect(Collectors.toSet()) : 
							ImmutableSet.of();
			case 4:
				return i < word.getComponents().size() ? 
						SIZE_4_POSSIBLE_BEGIN_END_AT_INDEX.get(i)
								.stream()
								.map(pair -> CompoundUtils.merge(word, pair.getElement1(), pair.getElement2()))
								.collect(Collectors.toSet()) : 
							ImmutableSet.of();
			default:
				return i < word.getComponents().size() ? 
						ImmutableSet.of(word.getComponents().get(i)) : 
							ImmutableSet.of();
			}
			
		} else
			return ImmutableSet.of();
	}

	private static Map<Integer, List<Pair<Integer>>> SIZE_3_POSSIBLE_BEGIN_END_AT_INDEX = ImmutableMap.of(
			0, ImmutableList.of(new Pair<Integer>(0, 0), new Pair<Integer>(0, 1)),
			1, ImmutableList.of(new Pair<Integer>(1, 1), new Pair<Integer>(1, 2), new Pair<Integer>(2, 2)),
			2, ImmutableList.of(new Pair<Integer>(2, 2))
		);

	private static Map<Integer, List<Pair<Integer>>> SIZE_4_POSSIBLE_BEGIN_END_AT_INDEX = ImmutableMap.of(
			0, ImmutableList.of(new Pair<Integer>(0, 0), new Pair<Integer>(0, 1), new Pair<Integer>(0, 2)),
			1, ImmutableList.of(new Pair<Integer>(1, 1), new Pair<Integer>(1, 2), new Pair<Integer>(1, 3), new Pair<Integer>(2, 2), new Pair<Integer>(2, 3), new Pair<Integer>(3, 3)),
			2, ImmutableList.of(new Pair<Integer>(2, 2), new Pair<Integer>(2, 3), new Pair<Integer>(3, 3)),
			3, ImmutableList.of(new Pair<Integer>(3, 3))
		);

}
