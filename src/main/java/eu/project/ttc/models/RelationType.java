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
package eu.project.ttc.models;

public enum RelationType {
	MORPHOLOGICAL(1, "morph", true),
	SYNTACTICAL(3, "syn", true),
	GRAPHICAL(2, "graph", false),
	DERIVES_INTO(4, "deriv", true),
	IS_PREFIX_OF(5, "pref", true),
	HAS_EXTENSION(6, "hasext", true),
	;
	
	private int order;
	private String shortName;
	private boolean directional;
	
	private RelationType(int order, String shortName, boolean directional) {
		this.order = order;
		this.directional = directional;
		this.shortName = shortName;
	}
	
	public int getOrder() {
		return order;
	}
	
	public boolean isDirectional() {
		return directional;
	}

	public String getShortName() {
		return shortName;
	}
	
	public boolean isSymetric() {
		return !directional;
	}
	
	public static RelationType fromShortName(String shortName) {
		for(RelationType vt:values())
			if(vt.getShortName().equals(shortName))
				return vt;
		throw new IllegalArgumentException("No such variation type with name: " + shortName);
	}
}
