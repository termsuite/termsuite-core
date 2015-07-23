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
package eu.project.ttc.engines.compost;


public class SegmentScoreEntry {
	public static final SegmentScoreEntry SCORE_ZERO = new SegmentScoreEntry(null, 0, null);
	
	private String segmentString;
	private float score;
	private CompostIndexEntry dicoEntry;
	public SegmentScoreEntry(String segmentString, float score,
			CompostIndexEntry dicoEntry) {
		this.segmentString = segmentString;
		this.score = score;
		this.dicoEntry = dicoEntry;
	}
	public String getSegmentString() {
		return segmentString;
	}
	public float getScore() {
		return score;
	}
	public CompostIndexEntry getDicoEntry() {
		return dicoEntry;
	}
	@Override
	public int hashCode() {
		return segmentString.hashCode();
	}
	@Override
	public String toString() {
		return segmentString;
	}
}
