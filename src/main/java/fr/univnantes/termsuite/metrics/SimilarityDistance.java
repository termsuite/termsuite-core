
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
package fr.univnantes.termsuite.metrics;

import fr.univnantes.termsuite.model.ContextVector;

/**
 * 
 * An interface for similarity distance measure between 
 * two {@link ContextVector}s.
 * 
 * @author Damien Cram
 *
 */
public interface SimilarityDistance {

	/**
	 * Computes the value of the similarity distance between two {@link ContextVector}s.
	 * 
	 * @param first
	 * 			a context vector
	 * @param second
	 * 			a context vector
	 * @return
	 * 			the distance according to this similarity distance
	 */
	public double getValue(ContextVector first,ContextVector second);
	
	/**
	 * Computes the value of the similarity distance between two {@link ContextVector}s
	 * and produces an explanation of the distance value, i.e. a list of vectors' component
	 * contributing the most to the final distance value.
	 * 
	 * @see Explanation
	 * @see ExplainedValue
	 * @param first
	 * 			a context vector
	 * @param second
	 * 			a context vector
	 * @return
	 * 			an {@link ExplainedValue} object holding both the distance value and 
	 * 			the explanation.
	 */
	public ExplainedValue getExplainedValue(ContextVector first, ContextVector second);
	

	@SuppressWarnings("unchecked")
	public static Class<? extends SimilarityDistance>[] values() {
		return new Class[]{
			Cosine.class,
			Jaccard.class
		};
	}
	
	
	public static Class<? extends SimilarityDistance> forName(String name) {
		for(Class<? extends SimilarityDistance> cls:values()) {
			if(cls.getName().equals(name)
					|| cls.getCanonicalName().equals(name)
					|| cls.getSimpleName().equals(name))
				return cls;
		}
		throw new IllegalArgumentException("No such " + SimilarityDistance.class.getSimpleName() + ": " + name);
	}

}
