
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

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.utils.JsonConfigObject;

public class PostProcessorOptions  extends JsonConfigObject {
	
	@JsonProperty("enabled")
	private boolean enabled = true;
	
	@JsonProperty("affix-score-th")
	private double affixScoreTh = 0.25;
	
	@JsonProperty("variation-score-th")
	private double variationScoreTh = 0.20;
	
	@JsonProperty("orthographic-score-th")
	private double orthographicScoreTh = 0.55;
	
	@JsonProperty("term-independance-th")
	private double termIndependanceTh = 0.10;

	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PostProcessorOptions) {
			PostProcessorOptions o = (PostProcessorOptions)obj;
			return enabled == o.enabled
					&& affixScoreTh == o.affixScoreTh
					&& variationScoreTh == o.variationScoreTh
					&& orthographicScoreTh == o.orthographicScoreTh
					&& termIndependanceTh == o.termIndependanceTh
					;
		} else return false;
	}


	public PostProcessorOptions() {
	}
	
	public PostProcessorOptions noFiltering() {
		affixScoreTh = -Double.MAX_VALUE;
		variationScoreTh = -Double.MAX_VALUE;
		orthographicScoreTh = -Double.MAX_VALUE;
		termIndependanceTh = -Double.MAX_VALUE;
		return this;
	}

	public double getAffixScoreTh() {
		return affixScoreTh;
	}

	public PostProcessorOptions setAffixScoreTh(double affixScoreTh) {
		this.affixScoreTh = affixScoreTh;
		return this;
	}

	public double getVariationScoreTh() {
		return variationScoreTh;
	}

	public PostProcessorOptions setVariationScoreTh(double variationScoreTh) {
		this.variationScoreTh = variationScoreTh;
		return this;
	}

	public double getOrthographicScoreTh() {
		return orthographicScoreTh;
	}

	public PostProcessorOptions setOrthographicScoreTh(double orthographicScoreTh) {
		this.orthographicScoreTh = orthographicScoreTh;
		return this;
	}

	public double getTermIndependanceTh() {
		return termIndependanceTh;
	}

	public PostProcessorOptions setTermIndependanceTh(double termIndependanceTh) {
		this.termIndependanceTh = termIndependanceTh;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public PostProcessorOptions setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
}