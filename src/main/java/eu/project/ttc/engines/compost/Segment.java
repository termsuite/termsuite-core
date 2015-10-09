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

import com.google.common.base.MoreObjects;

public  class Segment implements Comparable<Segment> {
	private int begin;
	private int end;
	
	/* cached substring of the parent compound */
	private String _substring;
	private String lemma;
	private boolean neoclassical;
	
	@Override
	public int compareTo(Segment o) {
		return Integer.compare(begin, o.begin);
	}

	Segment(int begin, int end, String string) {
		super();
		this.begin = begin;
		this.end = end;
		this._substring = string.substring(begin, end);
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}
	
	public String getSubstring() {
		return _substring;
	}
	
	public void setSubstring(String substring) {
		this._substring = substring;
	}

	public boolean isNeoclassical() {
		return neoclassical;
	}

	public void setNeoclassical(boolean neoclassical) {
		this.neoclassical = neoclassical;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("substring", _substring)
				.add("lemma", lemma)
				.toString();
	}

	public String getLemma() {
		return lemma;
	}
	
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
}