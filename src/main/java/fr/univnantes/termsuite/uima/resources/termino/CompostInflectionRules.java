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
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class CompostInflectionRules implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompostInflectionRules.class);

	public class InflectionRule {
		private String fromSuffixe;
		private String toSuffixe;
		private String regex;
		
		private InflectionRule(String fromSuffixe, String toSuffixe) {
			super();
			this.fromSuffixe = fromSuffixe;
			this.toSuffixe = toSuffixe;
			this.regex = this.fromSuffixe + "$";
		}
		public boolean canApplyTo(String str) {
			return str.endsWith(this.fromSuffixe);
		}
		public String inflect(String str) {
			return str.replaceAll(regex, this.toSuffixe);
		}
	}
	private List<InflectionRule> inflectionRules;
	
	public void load(DataResource data) throws ResourceInitializationException {
		InputStream inputStream;
		this.inflectionRules = Lists.newArrayList();
		try {
			inputStream = data.getInputStream();
			Scanner scanner = null;
			try {
				scanner = new Scanner(inputStream, "UTF-8");
				scanner.useDelimiter(TermSuiteConstants.LINE_BREAK);
				while (scanner.hasNext()) {
					String rawLine = scanner.next();
					String line = rawLine.split(";")[0];
					String[] args = line.split(",");
					if(args.length != 2 && !line.trim().isEmpty()) {
						LOGGER.warn("Bad inflection rules format: " + rawLine);
					} else {
						this.inflectionRules.add(new InflectionRule(args[0].trim(), args[1].trim()));
					}
				}
				this.inflectionRules = ImmutableList.copyOf(this.inflectionRules);
			} catch (Exception e) {
				throw new ResourceInitializationException(e);
			} finally {
				IOUtils.closeQuietly(scanner);
				IOUtils.closeQuietly(inputStream);
			}
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	public Collection<String> getInflections(String base) {
		Set<String> inflections = Sets.newHashSet();
		for(InflectionRule rule:this.inflectionRules) {
			if(rule.canApplyTo(base)) 
				inflections.add(rule.inflect(base));
		}
		return inflections;
	}
}
