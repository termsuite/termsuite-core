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
package fr.univnantes.termsuite.uima.resources.termino;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.uima.engines.termino.gathering.GroovyAdapter;
import fr.univnantes.termsuite.uima.engines.termino.gathering.VariantRule;
import fr.univnantes.termsuite.uima.engines.termino.gathering.VariantRuleIndex;
import fr.univnantes.termsuite.uima.engines.termino.gathering.VariantRuleYamlIO;

public class YamlVariantRules implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(YamlVariantRules.class);

	private List<VariantRule> variantRules;
	private GroovyAdapter groovyAdapter;


	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		this.groovyAdapter = new GroovyAdapter();
		InputStream inputStream = null;
		InputStreamReader reader = null;
		StringWriter writer = null;
		try {
			inputStream = aData.getInputStream();
			writer = new StringWriter();
			reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			IOUtils.copy(reader, writer);
			VariantRuleYamlIO yamlIO = VariantRuleYamlIO.fromYaml(writer.toString());
			this.variantRules = ImmutableList.copyOf(yamlIO.getVariantRules());
			
			// set the adapter
			for(VariantRule rule:this.variantRules)
				rule.setGroovyAdapter(this.groovyAdapter);
			
		} catch (IOException e) {
			LOGGER.error("Could not load the yaml variant rules resource dur to IOException");
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(writer);
		}
	}
	
	public List<VariantRule> getVariantRules() {
		return variantRules;
	}
	
	public List<VariantRule> getVariantRules(VariantRuleIndex variantRuleIndex) {
		List<VariantRule> rules = Lists.newArrayList();
		for(VariantRule rule:getVariantRules())
			if(rule.getIndex() == variantRuleIndex)
				rules.add(rule);
		return rules;
	}

	
	public VariantRule getMatchingRule(VariantRuleIndex variantRuleIndex, Term source, Term target) {
		for(VariantRule rule:getVariantRules(variantRuleIndex)) {
			if(rule.isSourceCompound() && !source.isCompound())
				continue;
			if(rule.isTargetCompound() && !target.isCompound())
				continue;
			if(!rule.getSourcePatterns().contains(source.getPattern()))
				continue;
			if(!rule.getTargetPatterns().contains(target.getPattern()))
				continue;
			if(rule.matchExpression(source, target))
				return rule;
		}
		return null;
	}
	
	public void clearAdapterCache() {
		this.groovyAdapter.clear();
	}

	public void initialize(TermIndex termIndex) {
		for(VariantRule variantRule:this.variantRules) {
			variantRule.initialize(termIndex);
		}
	}
}
