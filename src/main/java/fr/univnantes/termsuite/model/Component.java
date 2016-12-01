
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


import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;


public class Component implements Comparable<Component> {

	private String lemma;
	private String substring;
	private int begin;
	private int end;
	
	public boolean neoclassicalAffix = false;
	
	public Component(int begin, int end, String substring, String lemma) {
		this(begin, end, substring);
		this.lemma = lemma;
	}
	
	public Component(int begin, int end, String substring) {
		this.begin = begin;
		this.end = end;
		this.substring = substring;
		this.lemma = substring;
	}
	
	public int getBegin() {
		return begin;
	}
	
	public int getEnd() {
		return end;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(lemma, begin, end);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Component) {
			Component o = (Component) obj;
			return Objects.equal(this.lemma, o.lemma)
					&& Objects.equal(this.begin, o.begin)
					&& Objects.equal(this.end, o.end);
		} else
			return false;
	}
	
	@Override
	public int compareTo(Component o) {
		return ComparisonChain.start()
				.compare(begin, o.begin)
				.compare(end, o.end)
				.result();
	}
	
	@Override
	public String toString() {
		return this.substring;
	}

	public void setNeoclassical() {
		this.neoclassicalAffix = true;
	}

	public boolean isNeoclassicalAffix() {
		return neoclassicalAffix;
	}
	
	public String getLemma() {
		return lemma;
	}
	
	public String getSubstring() {
		return substring;
	}
}
