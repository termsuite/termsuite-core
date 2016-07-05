
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.metrics;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;

import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;

public class Jaccard extends AbstractSimilarityDistance {
	
	@Override
	protected double getValue(ContextVector source, ContextVector target, Explanation expl) {
		double infSum = 0;
		double supSum = 0;
		Set<Term> terms = Sets.newHashSet();
		terms.addAll(source.terms());
		terms.addAll(target.terms());
		double sourceValue;
		double targetValue;
		double partialInf;
		double partialSup;
		for (Term term : terms) {
			sourceValue = source.getAssocRate(term);
			targetValue = target.getAssocRate(term);
			partialInf = Doubles.min(sourceValue, targetValue);
			partialSup = Doubles.max(sourceValue, targetValue);
			infSum += partialInf;
			supSum += partialSup;
			if(partialInf > 0)
				expl.addExplanation(term, partialInf);
		}
		return supSum == 0 ? 0 : infSum / supSum;
	}

}
