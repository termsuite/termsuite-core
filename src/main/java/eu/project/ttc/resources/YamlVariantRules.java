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
package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.engines.variant.VariantRuleYamlIO;
import eu.project.ttc.models.Term;

public class YamlVariantRules implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(YamlVariantRules.class);

	private List<VariantRule> variantRules;

	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		InputStream inputStream;
		try {
			inputStream = aData.getInputStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer);
			VariantRuleYamlIO yamlIO = VariantRuleYamlIO.fromYaml(writer.toString());
			this.variantRules = ImmutableList.copyOf(yamlIO.getVariantRules());
		} catch (IOException e) {
			LOGGER.error("Could not load the yaml variant rules resource dur to IOException");
			throw new ResourceInitializationException(e);
		}
	}
	
	public List<VariantRule> getVariantRules() {
		return variantRules;
	}
	
	public VariantRule getMatchingRule(Term source, Term target) {
		for(VariantRule rule:variantRules) {
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
	
}
