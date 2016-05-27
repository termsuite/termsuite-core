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
package eu.project.ttc.tools;

import eu.project.ttc.engines.desc.Lang;


/**
 * A helper class that builds language-dependent term suite resource paths.
 * 
 * @author Damien Cram
 *
 */
public class TermSuiteResourceHelper {
	
	private Lang lang;
	private String resourceUrl;
	
	public TermSuiteResourceHelper(Lang lang, String urlPrefix) {
		super();
		
		/*
		 * To access resources in a jar:
		 * jar:file:/home/user/a/b/c/foo.jar!/com/example/stuff/config.txt
		 */
		
		this.lang = lang;
		
		/*
		 * Separator must be "/" since resources are always 
		 * loaded from url by UIMA.
		 */
		this.resourceUrl = urlPrefix + lang.getCode() + "/";
	}

	public TermSuiteResourceHelper(Lang lang) {
		this(lang, "file:");
	}
	

	public String getTaggerResource(TermSuiteResource tsResource, Tagger tagger) {
		return resourceUrl + tsResource.getTaggerPath(lang, tagger);
	}
	public String getResource(TermSuiteResource tsResource) {
		return resourceUrl + tsResource.getPath(lang);
	}
	
	public String getTTParameter() {
		return getResource(TermSuiteResource.TREETAGGER_CONFIG);
	}

	public String getAllowedChars() {
		return getResource(TermSuiteResource.ALLOWED_CHARS);
	}

	public String getSegmentBank() {
		return getResource(TermSuiteResource.SEGMENT_BANK);
	}
	
	public String getCaseMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_CASE_MAPPING, tagger);
	}

	public String getMoodMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_MOOD_MAPPING, tagger);
	}
	
	public String getCategoryMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_CATEGORY_MAPPING, tagger);
	}
	
	public String getNumberMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_NUMBER_MAPPING, tagger);
	}
	
	public String getSubcategoryMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_SUBCATEGORY_MAPPING, tagger);
	}
	
	public String getTenseMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_TENSE_MAPPING, tagger);
	}

	public String getGenderMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_GENDER_MAPPING, tagger);
	}

	public String getFrozenExpressionList() {
		return getResource(TermSuiteResource.FROZEN_EXPRESSIONS);
	}
	
	public String getMWRegexes() {
		return getResource(TermSuiteResource.MWT_RULES);
	}

	public String getStopWords() {
		return getResource(TermSuiteResource.STOP_WORDS_FILTER);
	}

	public String getYamlVariantRules() {
		return getResource(TermSuiteResource.VARIANTS);
	}

	@Deprecated
	public boolean resourceExists(String resourceURI) {
		return resourceURI != null && ClassLoader.getSystemClassLoader().getResourceAsStream(resourceURI) != null;
	}
	

	public String getGeneralLanguageFrequencies() {
		return getResource(TermSuiteResource.GENERAL_LANGUAGE);
	}
	public String getPrefixBank() {
		return getResource(TermSuiteResource.PREFIX_BANK);
	}
	public String getRootBank() {
		return getResource(TermSuiteResource.ROOT_BANK);
	}

	@Deprecated
	public String getEmptyDictionary() {
		return "file:eu/project/ttc/resources/all/empty-dictionary.txt";
	}

	public String getNeoclassicalPrefixes() {
		return getResource(TermSuiteResource.NEOCLASSICAL_PREFIXES);		
	}

	public String getLanguageDico() {
		return getResource(TermSuiteResource.DICO);
	}


	public String getCompostStopList() {
		return getResource(TermSuiteResource.COMPOST_STOP_LIST);
	}

	public String getCompostInflectionRules() {
		return getResource(TermSuiteResource.COMPOST_INFLECTION_RULES);
	}
	
	public String getCompostTransformationRules() {
		return getResource(TermSuiteResource.COMPOST_TRANSFORMATION_RULES);
	}
	
	public String getMateLemmatizerModelFileName() {
		return "mate-lemma-"+lang.getCode()+".model";
	}

	public String getMateTaggerModelFileName() {
		return "mate-pos-"+lang.getCode()+".model";
	}

	public String getFixedExpressionRegexes() {
		return getResource(TermSuiteResource.FIXED_EXPRESSION_REGEXES);
	}
}
