
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

import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;

public class Cosine extends AbstractSimilarityDistance {
	
	@Override
	protected double getValue(ContextVector source, ContextVector target, Explanation expl) {
		double sourceSum = getSquareSum(source);
		if(sourceSum == 0d)
			return 0d;
		
		double targetSum = getSquareSum(target);
		if(targetSum == 0d)
			return 0d;

		double sum = 0d;
		for (Term t : source.terms())  {
			double partial = source.getAssocRate(t) * target.getAssocRate(t);
			if(partial > 0)
				expl.addExplanation(t, partial);
			sum += partial;
		}
		
		return sum / Math.sqrt(sourceSum * targetSum);
	}

	private double getSquareSum(ContextVector vec) {
		double sum = 0.0;
		double val;
		for (Term t : vec.terms()) {
			val = vec.getAssocRate(t);
			sum += val * val;
		}
		return sum;
	}
	
}
