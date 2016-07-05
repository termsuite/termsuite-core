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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eu.project.ttc.utils.TermSuiteConstants;

public class BilingualDictionary {
	private static final Logger logger = LoggerFactory.getLogger(BilingualDictionary.class);
	
	private Multimap<String, String> targetTerms = HashMultimap.create();
	
	private BilingualDictionary() {}
	
	public static BilingualDictionary load(String filePath) throws IOException {
		logger.info("Loading dictionary {}", filePath);
		File f = new File(filePath);
		BufferedReader br = new BufferedReader(new FileReader(f));
		BilingualDictionary dico = new BilingualDictionary();
		
		String line;
		StringTokenizer st;
		while((line = br.readLine()) != null) {
			st = new StringTokenizer(line, TermSuiteConstants.TAB);
			String source = st.nextToken();
			String target = st.nextToken();
			dico.targetTerms.put(source.toLowerCase(), target.toLowerCase());
		}
		br.close();
		
		return dico;
	}

	public Collection<String> getTranslations(String source) {
		return this.targetTerms.get(source.toLowerCase());
	}
}
