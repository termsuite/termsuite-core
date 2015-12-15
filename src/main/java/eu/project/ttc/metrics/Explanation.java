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

import java.util.Collections;
import java.util.List;
import java.util.Queue;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;

import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;

/**
 * 
 * An object storing information about what can explains the value returned by 
 * a {@link SimilarityDistance} object for two {@link ContextVector}s.
 * 
 * The explanation comes in the form of a list of {@link ExplanationFeature}
 * objects where each {@link ExplanationFeature} gives a value to a co-term 
 * that appeared in both {@link ContextVector} compared. The value of an {@link ExplainedValue}
 * does not represent any tangible reality but the bigger is that value, the most
 * influent the associated co-term is for the Similarrity measure.
 * 
 * 
 * @see SimilarityDistance
 * @see ContextVector
 * @see ExplanationFeature
 * @author Damien Cram
 *
 */
public class Explanation implements IExplanation {
	public class ExplanationFeature implements Comparable<ExplanationFeature>{
		private Term coTerm;
		private double value;
		public ExplanationFeature(Term coTerm, double value) {
			super();
			this.coTerm = coTerm;
			this.value = value;
		}
		@Override
		public int compareTo(ExplanationFeature e) {
			return Double.compare(e.value,  this.value);
		}
		public Term getCoTerm() {
			return coTerm;
		}
		public double getValue() {
			return value;
		}
		@Override
		public String toString() {
			return String.format("%s: %.2f", coTerm.getLemma(), 10000*value);
		}
	}
	
	private Queue<ExplanationFeature> entries;
	private boolean isActivated = true;

	/**
	 * Builds an explanation object.
	 * 
	 * @param topN
	 */
	public Explanation(int topN) {
		super();
		this.entries = MinMaxPriorityQueue.maximumSize(topN).create();
	}
	
	/**
	 * 
	 * Gives the top n important entries for this alignment process.
	 * 
	 * @return
	 */
	public List<ExplanationFeature> getTopNFeatures() {
		List<ExplanationFeature> ret = Lists.newArrayList(this.entries);
		Collections.sort(ret);
		return ret;
	}

	public void addExplanation(Term coTerm, double value) {
		if(isActivated)
			this.entries.add(new ExplanationFeature(coTerm, value));
	}
	 
	private static Explanation emptyExplanation;
	
	static {
		emptyExplanation = new Explanation(1);
		emptyExplanation.isActivated  = false;
	}
	/**
	 * An empty explanation.
	 * 
	 * @return
	 */
	public static Explanation emptyExplanation() {
		return emptyExplanation;
	}
	
	@Override
	public String toString() {
		return getText();
	}

	@Override
	public String getText() {
		return String.format("{%s}",Joiner.on(",").join(getTopNFeatures()));
	}
}
