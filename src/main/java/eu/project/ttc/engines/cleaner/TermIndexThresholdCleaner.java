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
package eu.project.ttc.engines.cleaner;

import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;


/**
 * Removes terms from the {@link TermIndex} according to certain predicates.
 */
public class TermIndexThresholdCleaner extends AbstractTermIndexCleaner {

	public static final String THRESHOLD="Threshold";
	@ConfigurationParameter(name=THRESHOLD, mandatory=true)
	private float threshold;
	
	protected boolean acceptTerm(Term term) {
		return property.getDoubleValue(term) >= this.threshold;
	}

	@Override
	protected void doCleaningPartition(Set<Term> keptTerms,
			Set<Term> removedTerms) {
		for(Term t:termIndexResource.getTermIndex().getTerms()) {
			if(acceptTerm(t))
				keptTerms.add(t);
			else
				removedTerms.add(t);
		}
	}
}