
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
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class TermIndexUtils {

	public static Set<TermVariation> getVariations(TermIndex termIndex) {
		Set<TermVariation> variations = Sets.newHashSet();
		for(Term t:termIndex.getTerms()) {
			for(TermVariation tv:t.getVariations())
				variations.add(tv);
			for(TermVariation tv:t.getBases())
				variations.add(tv);
		}
		return variations;
	}


	public static  Set<TermVariation> selectTermVariations(TermIndex termIndex, VariationType type, String ruleName) {
		Set<TermVariation> selected = Sets.newHashSet();
		for(TermVariation tv:selectTermVariations(termIndex, type))
			if(Objects.equal(ruleName, tv.getInfo()))
				selected.add(tv);
		return selected;
	}
	
	public static  Set<TermVariation> selectTermVariations(TermIndex termIndex, VariationType... types) {
		Set<TermVariation> selected = Sets.newHashSet();
		for(TermVariation tv:getVariations(termIndex))
			for(VariationType type:types)
				if(tv.getVariationType() == type)
					selected.add(tv);
		return selected;
	}


	public static Collection<Term> selectCompounds(TermIndex termIndex) {
		Set<Term> compounds = Sets.newHashSet();
		for(Term t:termIndex.getTerms())
			if(t.isSingleWord() && t.isCompound())
				compounds.add(t);
		return compounds;
	}


	public static Collection<TermVariation> selectTermVariationsByInfo(TermIndex termIndex, String ruleName) {
		Set<TermVariation> selected = Sets.newHashSet();
		for(TermVariation tv:getVariations(termIndex))
			if(Objects.equal(ruleName, tv.getInfo()))
				selected.add(tv);
		return selected;
	}
}
