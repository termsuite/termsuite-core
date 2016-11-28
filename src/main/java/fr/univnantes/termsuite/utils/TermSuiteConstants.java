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
package fr.univnantes.termsuite.utils;

import com.google.common.collect.ImmutableSet;

public class TermSuiteConstants {
	public static final char COMPOUND_CHAR = '-';
	public static final char WHITESPACE = ' ';
	public static final String WHITESPACE_PATTERN_STRING = "\\s+";
	public static final String WHITESPACE_STRING = " ";
	public static final String CARD_TAG = "@card@";
	public static final String CARD_MATE = "CD";
	public static final String NOUN = "noun";
	public static final String ADJ = "adjective";
	public static final String ADV = "adverb";
	public static final String VERB = "verb";
	public static final String NAME = "name";
	public static final char PLUS = '+';
	public static final char HYPHEN = '-';
	public static final String DIESE = "#";
	public static final String LINE_BREAK = "\n";
	public static final String TAB = "\t";
	public static final String COLONS = ":";

	
	public static final String N = "N";
	public static final String V = "V";
	public static final String R = "R";
	public static final String A = "A";
	
	
	public static final ImmutableSet<String> TERM_CATEGORIES = ImmutableSet.of(
			TermSuiteConstants.NOUN, 
			TermSuiteConstants.VERB, 
			TermSuiteConstants.ADJ, 
			TermSuiteConstants.ADV);
	
	/**
	 * The UIMA Tokens Regex labels that are considered to define single word terms
	 */
	public static final ImmutableSet<String> TERM_MATCHER_LABELS = ImmutableSet.of(
			TermSuiteConstants.N, 
			TermSuiteConstants.V, 
			TermSuiteConstants.R, 
			TermSuiteConstants.A);
	public static final String COMMA = ",";
	public static final String EMPTY_STRING = "";
	public static final String DEFAULT_RESOURCE_URL_PREFIX = "/fr/univnantes/termsuite/resources/";
}
