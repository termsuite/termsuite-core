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
package eu.project.ttc.models;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableDouble;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import eu.project.ttc.metrics.AssociationRate;

/**
 * 
 * An index (often called cross table) that helps to compute 
 * the association rate of two terms, i.e. their amount of co-occurrency
 * in the corpus.
 * 
 * @see CrossTable#getLastA();
 * @see CrossTable#getLastB();
 * @see CrossTable#getLastC();
 * @see CrossTable#getLastD();
 * 
 * @author Damien Cram
 *
 */
public class CrossTable {

	/**
	 * The term index of the corpus
	 */
    private TermIndex termIndex;

    private Map<Term, MutableDouble> aPlusB;
    private Map<Term, MutableDouble> aPlusC;
    private int totalCoOccurrences;
    private int totalFrequency;
    
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
     * @param termIndex
     * 			The term index of the corpus
     * @throws NullPointerException
     * 			when the context vectors of term index terms are not
     */
    public CrossTable(TermIndex termIndex) {
		super();
		this.termIndex = termIndex;
		init();
	}


	private void init() {
        this.aPlusB = Maps.newHashMap();
        this.aPlusC = Maps.newHashMap();
        this.totalCoOccurrences = 0;
        this.totalFrequency = 0;
        
        Term term;
        for (Iterator<Term> it1 = this.termIndex.getTerms().iterator(); it1.hasNext() ;) {
            term = it1.next();
            this.totalFrequency++;
        	ContextVector context = term.getContextVector();
            for (ContextVector.Entry entry : context.getEntries()) {
            	this.totalCoOccurrences += entry.getNbCooccs();
                getScoreFromMap(this.aPlusB, term).add(entry.getNbCooccs());
                getScoreFromMap(this.aPlusC, entry.getCoTerm()).add(term.getFrequency());
            }
        }
    }


    private MutableDouble getScoreFromMap(Map<Term, MutableDouble> aScoreMap, Term key) {
		if(aScoreMap.get(key) == null)
			aScoreMap.put(key, new MutableDouble(0));
		return aScoreMap.get(key);
	}

    /**
     * 
     * Computes coefficients a, b, c and d (available) and computes the association
     * rate based on these coefficients.
     * 
     * These coefficients are made available through the not-thread safe methods <code>#getLastX()</code>.
     * 
     * @see #getLastA()
     * @see #getLastB()
     * @see #getLastC()
     * @see #getLastD()
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
    	int a = (int)x.getContextVector().getNbCooccs(y);

    	// B = (x & not(y))
    	MutableDouble a_plus_b = this.aPlusB.get(x);
    	int b = a_plus_b == null ? 0 : a_plus_b.intValue() - a;
//        int b = x.getFrequency() - a;
    	
         // C = (not(x) & y)
    	MutableDouble a_plus_c = this.aPlusB.get(y);
    	int c = a_plus_c == null ? 0 : a_plus_c.intValue() - a;
//        int c = y.getFrequency() - a;
         
         // D = (not(x) & not(y))
    	int d = this.totalCoOccurrences -  a - b - c;
//    	int d = this.totalFrequency - a - b - c;
        
    	// for DEBUG purpose only
    	lastA = a;
    	lastB = b;
    	lastC = c;
    	lastD = d;
    	// END DEBUG
    	
    	
    	final double value = rate.getValue(a, b, c, d);
//    	if(dbgSource.contains(x.getGroupingKey()) && dbgTarget.contains(y.getGroupingKey())) {
//    		System.out.format("%-15s%-15s%10d%10d%10d%12d%10.2f\n", x, y, a, b, c, d, value);
//    	}
    	
		return value;
    }

    private Set<String> dbgSource = ImmutableSet.of("n: vent", "n: éolienne", "n: système", "n: production", "n: énergie", "a: éolien", "n: axe");
    private Set<String> dbgTarget = ImmutableSet.of("n: biomasse", "n: éolienne", "n: vent", "n: production", "n: énergie", "a: éolien", "n: axe", "n: horizontal");
    
	private int lastA;
    private int lastB;
    private int lastC;
    private int lastD;

    /**
     * The number of times <code>coTerm</code> id a co-occurrence of <code>term</code>
     * 
     * WARNING : not thread safe !
     * 
     * @return
     */
	public int getLastA() {
		return lastA;
	}

    /**
     * The number of times a co-occurrence of <code>term</code> is not <code>coTerm</code>
     * 
     * WARNING : not thread safe !
     *      
     * @return
     */
	public int getLastB() {
		return lastB;
	}

    /**
     * The number of times <code>coTerm</code> is a co-occurrence of another term 
     * than <code>term</code>.
     * 
     * WARNING : not thread safe !
     *
     * @return
     */
	public int getLastC() {
		return lastC;
	}

    /**
     * The number of times neither <code>term</code> nor <code>coTerm</code> is a co-occurrence of something else
     * 
     * WARNING : not thread safe !
     * 
     * @return
     */
	public int getLastD() {
		return lastD;
	}
}
    
