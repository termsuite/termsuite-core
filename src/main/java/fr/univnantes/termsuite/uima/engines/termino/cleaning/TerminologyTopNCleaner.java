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
package fr.univnantes.termsuite.uima.engines.termino.cleaning;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;


/**
 * Keeps only top n terms in a {@link Terminology} after having ranked 
 * them according to a parameter term property.
 */
public class TerminologyTopNCleaner extends AbstractTerminologyCleaner {


	public static final String TOP_N="TopN";
	@ConfigurationParameter(name=TOP_N, mandatory=true)
	private int topN;

	@Override
	protected void doCleaningPartition(Set<Term> keptTerms,
			Set<Term> removedTerms) {
		List<Term> terms = Lists.newArrayList(terminoResource.getTerminology().getTerms());
		Collections.sort(terms, property.getComparator(true));
		for(int index = 0; index < terms.size(); index++) {
			if(index < topN)
				keptTerms.add(terms.get(index));
			else
				removedTerms.add(terms.get(index));
		}
	}
	
	@Override
	public String toString() {
		return String.format("keeping top %d terms by %s", topN, property.getPropertyName());
	}

}
