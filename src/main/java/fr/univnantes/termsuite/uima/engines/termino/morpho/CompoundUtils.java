
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

package fr.univnantes.termsuite.uima.engines.termino.morpho;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.termino.TermValueProviders;
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
			lemmaBuilder.append(word.getLemma().substring(lastComponent.getBegin(), lastComponent.getEnd()));
			
			if(lastComponent.getEnd() < cur.getBegin())
				/*
				 * Fills the gap with the lemma substring
				 */
				lemmaBuilder.append(word.getLemma().substring(lastComponent.getEnd(), cur.getBegin()));
			
			lastComponent = cur;
		}
		lemmaBuilder.append(lastComponent.getLemma());
		return new Component(lemmaBuilder.toString(), begin, lastComponent.getEnd());
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
	
	public static String toIndexString(Pair<Component> pair) {
		boolean ordered = pair.getElement1().getLemma().compareTo(pair.getElement2().getLemma()) <= 0;
		StringBuilder sb = new StringBuilder();
		sb.append(ordered ? pair.getElement1().getLemma() : pair.getElement2().getLemma());
		sb.append(TermSuiteConstants.PLUS);
		sb.append(ordered ? pair.getElement2().getLemma() : pair.getElement1().getLemma());
		return sb.toString();
		
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
	public static List<Pair<String>> asLemmaPairs(Word word) {
		List<Pair<String>> pairs = Lists.newArrayList();
		if(word.isCompound()) {
			String lemma1, lemma2;
			int n = word.getComponents().size();
			for(int i=0; i<n-1; i++) {
				lemma1 = merge(word, word.getComponents().subList(0, i+1)).getLemma();
				lemma2 = merge(word, word.getComponents().subList(i+1, n)).getLemma();
				pairs.add(new Pair<String>(lemma1, lemma2));
			}
		}
		return pairs;
	}

}
