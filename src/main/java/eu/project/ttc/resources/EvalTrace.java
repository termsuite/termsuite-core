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
package eu.project.ttc.resources;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.engines.EvalEngine;
import eu.project.ttc.resources.ReferenceTermList.RTLTerm;

/**
 * 
 * A list of evaluation {@link RecPoint} for precision/recall metrics against against 
 * a reference list. This resource is used and filled by {@link EvalEngine}.
 * 
 * Each {@link RecPoint}  holds the number of true positive (tp) extracted 
 * terms at a given index (where index is the top n extracted terms) aginst the
 * reference list.
 * 
 * @author Damien Cram
 *
 */
public class EvalTrace implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(EvalTrace.class);
	
	private static Map<String, EvalTrace> evalTraces = Maps.newConcurrentMap();
	
	/**
	 * 
	 * 
	 * 
	 * @author Damien Cram
	 *
	 */
	public class RecPoint {
		private static final String PRECISION = "precision";
		private static final String RECALL = "recall";
		List<RTLTerm> lcTermsFound;
		int rank;
		int tp;
		RecPoint(int rank, int tp, List<RTLTerm> lcTermsFound) {
			super();
			this.rank = rank;
			this.tp = tp;
			this.lcTermsFound = lcTermsFound;
		}
		public int getTp() {
			return tp;
		}
		public int getRank() {
			return this.rank;
		}
		public double getFScore(double beta) {
			if(getPrecision()*getRecall() == 0d)
				return 0d;
			else
				return ((1 + beta*beta)*getPrecision()*getRecall())/(beta*beta*(getPrecision()+getRecall()));
		}
		public double getPrecision() {
			return ((float)this.tp) / this.rank;
		}
		public double getRecall() {
			return ((float)this.tp) / EvalTrace.this.rtlSize;
		}
		public double getValue(String pName) {
			switch (pName) {
			case PRECISION:
				return getPrecision();
			case RECALL:
				return getRecall();
			default:
				throw  new IllegalArgumentException("Unkown property name for RecPoint: " + pName);
			}
		}

		public List<RTLTerm> getLcTermsFound() {
			return lcTermsFound;
		}
		
		@Override
		public String toString() {
			return String.format("%d: %d", rank, tp);
		}
	} /* End RecPoint */
	
	private int rtlSize;
	public void setRtlSize(int refRtlSize) {
		this.rtlSize = refRtlSize;
	}
	
	private SortedMap<Integer, RecPoint> points = Maps.newTreeMap();
	
	private String name;
	

	public void trace(int index, int tp, List<RTLTerm> foundTerms) {
		points.put(index, new RecPoint(index, tp, foundTerms));
	}
	
	public double getMaxRecall() {
		return points.isEmpty() ? 1d : getLast().getRecall();
	}
	
	public RecPoint getLast() {
		return points.get(points.lastKey());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(this.name)
				.add("rtlSize", this.rtlSize)
				.add("points", Joiner.on("|").join(this.points.values()))
				.toString();
	}
	/**
	 * Get the first {@link RecPoint} whose recall exceeds the parameter
	 * precisionThreshorld.
	 * 
	 * @param recallThreshorld
	 * @return
	 */
	public RecPoint getAtRecall(double recallThreshorld) {
		RecPoint last = null;
		for(java.util.Map.Entry<Integer, RecPoint> e:points.entrySet()) {
			if(e.getValue().getRecall() >= recallThreshorld)
				return e.getValue();
			last = e.getValue();
		}
		return last;
	}

	/**
	 * Get the {@link RecPoint} for rank number of extracted indexes.
	 * 
	 * @param rank
	 * @return
	 */
	public RecPoint getAtRank(int rank) {
		return points.get(rank);
	}

	public Collection<Integer> getIndexes() {
		return points.keySet();
	}

	public Collection<Double> getValues(String pName) {
		List<Double> vals = Lists.newArrayList();
		for(RecPoint p:points.values())
			vals.add(p.getValue(pName));
		return vals;
	}

	public Collection<RecPoint> getPoints() {
		return points.values();
	}
	public Collection<RecPoint> getChartAxisPoints(boolean takeLastPoint) {
		List<RecPoint> axisPoints = Lists.newArrayList();
		List<RTLTerm> accu = Lists.newArrayList();
		RecPoint last = null;
		for(RecPoint p:points.values()) {
			last = p;
			accu.addAll(p.getLcTermsFound());
			if(EvalEngine.CHART_AXIS_POINTS.contains(p.getRank())) {
				axisPoints.add(new RecPoint(p.getRank(), p.getTp(), accu));
				accu = Lists.newArrayList();
			}
		}
		if(takeLastPoint)
			axisPoints.add(new RecPoint(last.getRank(), last.getTp(), accu));
		return axisPoints;
	}

	
	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		URI uri = aData.getUri();
		String tName = uri.toString();
		LOGGER.debug("Registering trace {}", tName);
		if(tName == null)
			throw new ResourceInitializationException("Could not initialize an evaluation with a null name.", new Object[]{tName});
		else if(EvalTrace.evalTraces.containsKey(tName))
			throw new ResourceInitializationException("A trace named "+tName+" already exists", new Object[]{tName});
		else {
			this.name = tName;
			EvalTrace.evalTraces.put(tName, this);
		}
	}

	/**
	 * Remove all evaluation traces.
	 */
	public static void clear() {
		evalTraces.clear();
	}
	
	public static EvalTrace get(String traceName) {
		return evalTraces.get(traceName);
	}
	
	
	
	public String getName() {
		return name;
	}

	/**
	 * Creates a clone of this trace with only points that are 
	 * meant to be displayed on a chart (these points are {@link EvalEngine#CHART_AXIS_POINTS})
	 * 
	 * @return
	 */
	public EvalTrace toDisplayable() {
		EvalTrace displayable = new EvalTrace();
		displayable.rtlSize = this.rtlSize;
		displayable.name = this.name;
		for(RecPoint p:this.getChartAxisPoints(true)) 
			displayable.points.put(p.getRank(), p);
		return displayable;
	}
	
	/**
	 * Filters this eval trace and keeps only the parameter indexes.
	 */
	public void keepIndexes(Collection<Integer> indexes) {
		Iterator<Map.Entry<Integer, RecPoint>> it = points.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Integer, RecPoint> e = it.next();
			if(!indexes.contains(e.getKey()))
				it.remove();
		}
	}
}