
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

package fr.univnantes.termsuite.resources;

public class PostProcConfig {
	private double affixScoreTh = 0.25;
	private double variationScoreTh = 0.20;
	private double orthographicScoreTh = 0.55;
	private double termIndependanceTh = 0.10;

	public PostProcConfig() {
	}
	
	public PostProcConfig noFiltering() {
		affixScoreTh = -Double.MAX_VALUE;
		variationScoreTh = -Double.MAX_VALUE;
		orthographicScoreTh = -Double.MAX_VALUE;
		termIndependanceTh = -Double.MAX_VALUE;
		return this;
	}

	public double getAffixScoreTh() {
		return affixScoreTh;
	}

	public PostProcConfig setAffixScoreTh(double affixScoreTh) {
		this.affixScoreTh = affixScoreTh;
		return this;
	}

	public double getVariationScoreTh() {
		return variationScoreTh;
	}

	public PostProcConfig setVariationScoreTh(double variationScoreTh) {
		this.variationScoreTh = variationScoreTh;
		return this;
	}

	public double getOrthographicScoreTh() {
		return orthographicScoreTh;
	}

	public PostProcConfig setOrthographicScoreTh(double orthographicScoreTh) {
		this.orthographicScoreTh = orthographicScoreTh;
		return this;
	}

	public double getTermIndependanceTh() {
		return termIndependanceTh;
	}

	public PostProcConfig setTermIndependanceTh(double termIndependanceTh) {
		this.termIndependanceTh = termIndependanceTh;
		return this;
	}

	public static PostProcConfig create() {
		return new PostProcConfig();
	}
}