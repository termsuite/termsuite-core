
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

package eu.project.ttc.models.scored;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;

public class ScoredModel {
	
	private List<ScoredTerm> terms;
	private Map<Term, ScoredTerm> adapters = Maps.newHashMap();
	private TermIndex termIndex;
	
	public void importTermIndex(TermIndex termIndex) {
		this.terms = Lists.newLinkedList();
		for(Term t:termIndex.getTerms()) {
			List<ScoredVariation> scoredVariations  = Lists.newArrayListWithExpectedSize(termIndex.getOutboundTermVariations(t).size());
			ScoredTerm st = getAdapter(t);
			for(TermVariation tv:termIndex.getOutboundTermVariations(t)) {
				ScoredVariation stv = new ScoredVariation(this, tv);
				scoredVariations.add(stv);
			}
			st.setVariations(scoredVariations);
			this.terms.add(st);
		}
		this.termIndex = termIndex;
	}

	public Collection<ScoredTerm> getTerms() {
		return Collections.unmodifiableCollection(this.terms);
	}

	public void sort(Comparator<ScoredTerm> compoarator) {
		Collections.sort(this.terms, compoarator);
	}

	public ScoredTerm getAdapter(Term t) {
		if(!adapters.containsKey(t))
			adapters.put(t, new ScoredTerm(this,  t));
		return adapters.get(t);
	}

	public TermIndex getTermIndex() {
		return termIndex;
	}

	public void removeTerms(Set<ScoredTerm> rem) {
		Iterator<ScoredTerm> it = this.terms.iterator();
		ScoredTerm cur;
		while(it.hasNext()) {
			cur = it.next();
			if(rem.contains(cur))
				it.remove();
			else {
				Iterator<ScoredVariation> vIt = cur.getVariations().iterator();
				while(vIt.hasNext()) {
					if(rem.contains(vIt.next().getVariant()))
						vIt.remove();
				}
			}
		}
		for(ScoredTerm st:rem)
			adapters.remove(st.getTerm());
	}
}
