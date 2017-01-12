
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
package fr.univnantes.termsuite.model;

import java.util.Map;

import org.apache.commons.lang3.mutable.MutableDouble;

import fr.univnantes.termsuite.metrics.AssociationRate;

/**
 * 
 * An index (often called cross table) that helps to compute 
 * the association rate of two terms, i.e. their amount of co-occurrency
 * in the corpus.
 * 
 * @author Damien Cram
 *
 */
public class CrossTable {

    private Map<Term, MutableDouble> aPlusB;
    private Map<Term, MutableDouble> aPlusC;
    private int totalCoOccurrences;
    
    /**
     * 
     * Constructs this cross table with a term index and inits the table 
     * with all co-occurrences of the term index's terms. This init process is
     * based on terms' context vectors, which must be computed before
     * invoking this constructor.
     * 
     * @see Term#getContextVector()
     * @throws NullPointerException
     * 			if terms' context vectors are not already computed
     * @throws NullPointerException
     * 			when the context vectors of term index terms are not
     */
	public CrossTable(Map<Term, MutableDouble> aPlusB, Map<Term, MutableDouble> aPlusC, int totalCoOccurrences) {
		super();
		this.aPlusB = aPlusB;
		this.aPlusC = aPlusC;
		this.totalCoOccurrences = totalCoOccurrences;
	}

    
    /**
     * 
     * Computes coefficients a, b, c and d (available) and computes the association
     * rate based on these coefficients.
     * 
     * These coefficients are made available through the not-thread safe methods <code>#getLastX()</code>.
     * 
     * @param rate
     * 			the association rate measure
     * @param x
     * 			the base term
     * @param y
     * 			the co term
     * @return
     * 			the association rate
     */
    public double computeRate(AssociationRate rate, Term x, Term y) {

    	// A = (x & y)
    	int a = (int)x.getContext().getNbCooccs(y);

    	// B = (x & not(y))
    	MutableDouble a_plus_b = this.aPlusB.get(x);
    	int b = a_plus_b == null ? 0 : a_plus_b.intValue() - a;
//        int b = x.getFrequency() - a;
    	
         // C = (not(x) & y)
    	MutableDouble a_plus_c = this.aPlusC.get(y);
    	int c = a_plus_c == null ? 0 : a_plus_c.intValue() - a;
//        int c = y.getFrequency() - a;
         
         // D = (not(x) & not(y))
    	int d = this.totalCoOccurrences -  a - b - c;
        
    	final double value = rate.getValue(a, b, c, d);
    	
		return value;
    }
}
    
