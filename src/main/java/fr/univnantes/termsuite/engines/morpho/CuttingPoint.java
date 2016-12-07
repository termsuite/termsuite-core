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
package fr.univnantes.termsuite.engines.morpho;

public  class CuttingPoint implements Comparable<CuttingPoint> {
	private int index;
	private int offset;
	private boolean isHypen;
	CuttingPoint(int index, int offset, boolean isHypen) {
		super();
		this.index = index;
		this.offset = offset;
		this.isHypen = isHypen;
	}
	public int getIndex() {
		return index;
	}
	public int getOffset() {
		return offset;
	}
	
	@Override
	public int compareTo(CuttingPoint o) {
		return Integer.compare(index, o.index);
	}
	
	@Override
	public String toString() {
		return ""+this.index + (this.offset>0 ? '*' : "");
	}
	public boolean isHypen() {
		return this.isHypen;
	}
}