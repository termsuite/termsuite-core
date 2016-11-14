
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import eu.project.ttc.models.TermWord;
import eu.project.ttc.utils.StringUtils;

public class SuffixDerivation {
	private static String TYPE_FORMAT = "%s %s";
	private static final String MSG_CANNOT_APPLY_DERIVATION = "Cannot operate the derivation %s on string %s";

	private String derivateFormPattern;
	private String regularFormPattern;
	private String derivateFormSuffix;
	private String regularFormSuffix;
	private String type;

	
	public SuffixDerivation(String derivateFormPattern, String regularFormPattern, String derivateFormSuffix, String regularFormSuffix) {
		super();
		Preconditions.checkNotNull(derivateFormSuffix);
		Preconditions.checkNotNull(regularFormSuffix);
		this.derivateFormSuffix = derivateFormSuffix;
		this.regularFormSuffix = regularFormSuffix;
		this.derivateFormPattern = derivateFormPattern;
		this.regularFormPattern = regularFormPattern;
		this.type = String.format(TYPE_FORMAT, regularFormPattern, derivateFormPattern);
	}
	
	public String getFromPattern() {
		return derivateFormPattern;
	}
	
	public String getToPattern() {
		return regularFormPattern;
	}
	
	public String getDerivateSuffix() {
		return derivateFormSuffix;
	}
	
	public String getRegularSuffix() {
		return regularFormSuffix;
	}
	
	public TermWord getBaseForm(TermWord derivateForm) {
		Preconditions.checkArgument(isKnownDerivate(derivateForm),
				MSG_CANNOT_APPLY_DERIVATION,
				this,
				derivateForm);
		return TermWord.create(
				StringUtils.replaceLast(derivateForm.getWord().getLemma(), derivateFormSuffix, regularFormSuffix),
				this.regularFormPattern);
	}

	public boolean isKnownDerivate(TermWord candidateDerivateTermWord) {
		return candidateDerivateTermWord.getSyntacticLabel().equals(derivateFormPattern)
				&& candidateDerivateTermWord.getWord().getLemma().endsWith(derivateFormSuffix);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("from", derivateFormPattern + ":"+derivateFormSuffix)
				.add("to", regularFormPattern + ":"+regularFormSuffix)
				.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SuffixDerivation) {
			SuffixDerivation sd = (SuffixDerivation) obj;
			return derivateFormSuffix.equals(sd.derivateFormSuffix)
					&& regularFormSuffix.equals(sd.regularFormSuffix)
					&& derivateFormPattern.equals(sd.derivateFormPattern)
					&& regularFormPattern.equals(sd.regularFormPattern);
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.derivateFormSuffix, this.regularFormSuffix, this.derivateFormPattern, this.regularFormPattern);
	}

	public String getType() {
		return type;
	}
}
