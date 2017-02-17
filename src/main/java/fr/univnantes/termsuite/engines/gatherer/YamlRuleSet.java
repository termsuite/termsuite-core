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
package fr.univnantes.termsuite.engines.gatherer;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;

import fr.univnantes.termsuite.framework.TermSuiteResource;

public class YamlRuleSet implements TermSuiteResource {
	private Multimap<VariationType, VariantRule> variantRules;

	public Collection<VariantRule> getVariantRules(VariationType key) {
		return variantRules.get(key);
	}

	public YamlRuleSet() {}
	
	public YamlRuleSet(Iterable<VariantRule> rules) {
		init(rules);
	}

	private void init(Iterable<VariantRule> rules) {
		variantRules = HashMultimap.create();
		for(VariantRule rule:rules) 
			variantRules.put(rule.getVariationType(), rule);
	}
	
	public Collection<VariantRule> getVariantRules() {
		return variantRules.values();
	}

	@Override
	public void load(Reader reader) throws IOException {
		YamlRuleSet set = YamlRuleSetIO.fromYaml(CharStreams.toString(reader));
		this.init(set.getVariantRules());
	}
}
