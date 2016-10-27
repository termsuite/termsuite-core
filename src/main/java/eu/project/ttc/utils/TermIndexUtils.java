
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

package eu.project.ttc.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermIndexes;

public class TermIndexUtils {

	public static  Set<TermRelation> selectTermVariations(TermIndex termIndex, RelationType type, String ruleName) {
		Set<TermRelation> selected = Sets.newHashSet();
		for(TermRelation tv:selectTermVariations(termIndex, type))
			if(Objects.equal(ruleName, tv.getInfo()))
				selected.add(tv);
		return selected;
	}
	
	public static  Set<TermRelation> selectTermVariations(TermIndex termIndex, RelationType... types) {
		Set<RelationType> typeSet = Sets.newHashSet(types);
		return termIndex.getRelations().filter(tv -> 
				typeSet.contains(tv.getType())
			).collect(Collectors.toSet());
	}


	public static Collection<Term> selectCompounds(TermIndex termIndex) {
		Set<Term> compounds = Sets.newHashSet();
		for(Term t:termIndex.getTerms())
			if(t.isSingleWord() && t.isCompound())
				compounds.add(t);
		return compounds;
	}


	public static Collection<TermRelation> selectTermVariationsByInfo(TermIndex termIndex, String ruleName) {
		return termIndex.getRelations().filter(tv -> 
				Objects.equal(ruleName, tv.getInfo())
			).collect(Collectors.toSet());
	}
	

	/**
	 * E.g. Given the compound [hydro|électricité] and the component [hydro], the method should return the 
	 * term [électricité]
	 * 
	 * 
	 * @param termIndex
	 * @param compound
	 * @param component
	 * @return
	 */
	public static Collection<Term> getMorphologicalExtensionsAsTerms(TermIndex termIndex, Term compound, Component component) {
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
		
		
		List<Term> extensionTerms = Lists.newArrayList();
		CustomTermIndex lemmaIndex = termIndex.getCustomIndex(TermIndexes.LEMMA_LOWER_CASE);
		for(String s:possibleExtensionLemmas)
			extensionTerms.addAll(lemmaIndex.getTerms(s.toLowerCase()));

		
		return extensionTerms;
	}

}
