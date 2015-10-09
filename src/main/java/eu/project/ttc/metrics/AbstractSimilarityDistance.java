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

package eu.project.ttc.metrics;

import eu.project.ttc.models.ContextVector;


/**
 * 
 * An abstract implementation of {@link SimilarityDistance} that manages the 
 * similarity explanation.
 * 
 * @see SimilarityDistance
 * @see Explanation
 * @author Damien Cram
 *
 */
public abstract class AbstractSimilarityDistance implements SimilarityDistance {

	private int nbExplanation = 10;

	public AbstractSimilarityDistance() {
		super();
	}
	
	@Override
	public ExplainedValue getExplainedValue(
			ContextVector v1, ContextVector v2) {
		Explanation expl = new Explanation(nbExplanation);
		double value = getValue(v1, v2, expl);
		return new ExplainedValue(value, expl);
	}

	@Override
	public double getValue(ContextVector v1, ContextVector v2) {
		return getValue(v1, v2, Explanation.emptyExplanation());
	}
	
	protected abstract double getValue(ContextVector source, ContextVector target, Explanation explainedValue);
}
