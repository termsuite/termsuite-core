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
package eu.project.ttc.engines.variant;


public class VariantRuleBuilder {
	private VariantRule variantRule;
	private VariantRuleBuilder(String name) {
		this.variantRule = new VariantRule(name);
	}
	
	public static VariantRuleBuilder start(String name) {
		return new VariantRuleBuilder(name);
	}
	
	public VariantRuleBuilder addSourcePattern(String p) {
		this.variantRule.addSourcePattern(p);
		return this;
	}

	public VariantRuleBuilder addTargetPattern(String p) {
		this.variantRule.addTargetPattern(p);
		return this;
	}

	public VariantRuleBuilder sourceCompound() {
		this.variantRule.setSourceCompound(true);
		return this;
	}

	public VariantRuleBuilder targetCompound() {
		this.variantRule.setTargetCompound(true);
		return this;
	}

	public VariantRuleBuilder rule(String groovyRule) {
		this.variantRule.setGroovyRule(groovyRule);
		return this;
	}

	public VariantRule create() {
		return variantRule;
	}
}
