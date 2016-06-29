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
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import eu.project.ttc.utils.TermSuiteConstants;

/**
 * 
 * @author Damien Cram
 *
 */
public class FixedExpressionResource implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(FixedExpressionResource.class);

	public static final String FIXED_EXPRESSION_RESOURCE = "FixedExpressionResource";

	private Set<String> fixedExpressionLemmas = Sets.newHashSet();
	
	public void load(DataResource data) throws ResourceInitializationException {
		InputStream inputStream = null;
		try {
			inputStream = data.getInputStream();
			Scanner scanner = null;
			try {
				String fixedExpression, line;
				String[] str;
				scanner = new Scanner(inputStream, "UTF-8");
				scanner.useDelimiter(TermSuiteConstants.LINE_BREAK);
				while (scanner.hasNext()) {
					line = scanner.next().split(TermSuiteConstants.DIESE)[0].trim();
					str = line.split(TermSuiteConstants.TAB);
					fixedExpression = str[0];
					
					fixedExpressionLemmas.add(fixedExpression);
				}
			} catch (Exception e) {
				throw new ResourceInitializationException(e);
			} finally {
				IOUtils.closeQuietly(scanner);
			}
		} catch (IOException e) {
			LOGGER.error("Could not load file {}",data.getUrl());
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	
	public Set<String> getFixedExpressionLemmas() {
		return Collections.unmodifiableSet(fixedExpressionLemmas);
	}


	public boolean containsLemma(String lemma) {
		return fixedExpressionLemmas.contains(lemma.toLowerCase());
	}
}
