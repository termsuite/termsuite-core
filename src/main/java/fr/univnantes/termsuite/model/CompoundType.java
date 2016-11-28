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
package fr.univnantes.termsuite.model;

public enum CompoundType {
	NATIVE("nat"), NEOCLASSICAL("neo"), UNSET("unset");
	private String shortName;
	private CompoundType(String shortName) {
		this.shortName = shortName;
	}
	public String getShortName() {
		return shortName;
	}
	@Override
	public String toString() {
		return getShortName();
	}
	public static CompoundType fromName(String name) {
		for(CompoundType t:values())
			if(t.name().equals(name))
				return t;
		throw new IllegalArgumentException("Unknown compound type: " + name);
	}

	public static CompoundType fromShortName(String shortName) {
		for(CompoundType t:values())
			if(t.getShortName().equals(shortName))
				return t;
		
		throw new IllegalArgumentException("Unknown compound type: " + shortName);
	}
}
