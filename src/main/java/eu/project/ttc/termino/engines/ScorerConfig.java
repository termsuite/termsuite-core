
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

package eu.project.ttc.termino.engines;

public class ScorerConfig {
	private double extensionSpecTh = 0.1;
	private double extensionGainTh = 0.1;
	private double variantIndependanceTh = 0.5;
	private double variationScoreTh = 0.25;
	private double orthographicScoreTh = 0.55;
	private double termIndependanceTh = 0.10;

	private ScorerConfig() {
	}

	public double getExtensionSpecTh() {
		return extensionSpecTh;
	}

	public void setExtensionSpecTh(double extensionSpecTh) {
		this.extensionSpecTh = extensionSpecTh;
	}
	
	public ScorerConfig noFiltering() {
		extensionSpecTh = 0;
		extensionGainTh = 0;
		variantIndependanceTh = 0;
		variationScoreTh = 0;
		orthographicScoreTh = 0;
		termIndependanceTh = 0;
		return this;
	}

	public double getExtensionGainTh() {
		return extensionGainTh;
	}

	public void setExtensionGainTh(double extensionGainTh) {
		this.extensionGainTh = extensionGainTh;
	}

	public double getVariantIndependanceTh() {
		return variantIndependanceTh;
	}

	public void setVariantIndependanceTh(double variantIndependanceTh) {
		this.variantIndependanceTh = variantIndependanceTh;
	}

	public double getVariationScoreTh() {
		return variationScoreTh;
	}

	public void setVariationScoreTh(double variationScoreTh) {
		this.variationScoreTh = variationScoreTh;
	}

	public double getOrthographicScoreTh() {
		return orthographicScoreTh;
	}

	public void setOrthographicScoreTh(double orthographicScoreTh) {
		this.orthographicScoreTh = orthographicScoreTh;
	}

	public double getTermIndependanceTh() {
		return termIndependanceTh;
	}

	public void setTermIndependanceTh(double termIndependanceTh) {
		this.termIndependanceTh = termIndependanceTh;
	}

	public static ScorerConfig create(double variantIndependenceScoreThreshold, 
			double variantExtGainThreshold, 
			double variantExtSpecThreshold, 
			double variantScoreThreshold) {
		ScorerConfig scorerConfig = create();
		scorerConfig.setExtensionGainTh(variantExtGainThreshold);
		scorerConfig.setExtensionSpecTh(variantExtSpecThreshold);
		scorerConfig.setVariantIndependanceTh(variantIndependenceScoreThreshold);
		scorerConfig.setVariationScoreTh(variantScoreThreshold);
		return scorerConfig;
	}
	public static ScorerConfig create() {
		return new ScorerConfig();
	}

}