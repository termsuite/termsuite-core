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

import java.nio.file.Path;
import java.nio.file.Paths;

import eu.project.ttc.engines.desc.Lang;


/**
 * A helper class that builds language-dependent term suite resource paths.
 * 
 * @author Damien Cram
 *
 */
public class TermSuiteResourceHelper {
	
	private Lang lang;
	private String resourcePath;
	
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
		this.resourcePath = urlPrefix + lang.getCode() + "/";
	}

	public TermSuiteResourceHelper(Lang lang) {
		this(lang, "file:");
	}
	

	public Path getTaggerResource(TermSuiteResource tsResource, Tagger tagger) {
		return Paths.get(resourcePath + tsResource.getTaggerPath(lang, tagger));
	}
	public Path getResource(TermSuiteResource tsResource) {
		return Paths.get(resourcePath + tsResource.getPath(lang));
	}
	
	public Path getTTParameter() {
		return getResource(TermSuiteResource.TREETAGGER_CONFIG);
	}

	public Path getAllowedChars() {
		return getResource(TermSuiteResource.ALLOWED_CHARS);
	}

	public Path getSegmentBank() {
		return getResource(TermSuiteResource.SEGMENT_BANK);
	}
	
	public Path getCaseMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_CASE_MAPPING, tagger);
	}

	public Path getMoodMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_MOOD_MAPPING, tagger);
	}
	
	public Path getCategoryMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_CATEGORY_MAPPING, tagger);
	}
	
	public Path getNumberMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_NUMBER_MAPPING, tagger);
	}
	
	public Path getSubcategoryMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_SUBCATEGORY_MAPPING, tagger);
	}
	
	public Path getTenseMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_TENSE_MAPPING, tagger);
	}

	public Path getGenderMapping(Tagger tagger) {
		return getTaggerResource(TermSuiteResource.TAGGER_GENDER_MAPPING, tagger);
	}

	public Path getFrozenExpressionList() {
		return getResource(TermSuiteResource.FROZEN_EXPRESSIONS);
	}
	
	public Path getMWRegexes() {
		return getResource(TermSuiteResource.MWT_RULES);
	}

	public Path getStopWords() {
		return getResource(TermSuiteResource.STOP_WORDS_FILTER);
	}

	public Path getYamlVariantRules() {
		return getResource(TermSuiteResource.VARIANTS);
	}

	@Deprecated
	public boolean resourceExists(String resourceURI) {
		return resourceURI != null && ClassLoader.getSystemClassLoader().getResourceAsStream(resourceURI) != null;
	}
	

	public Path getGeneralLanguageFrequencies() {
		return getResource(TermSuiteResource.GENERAL_LANGUAGE);
	}
	public Path getPrefixBank() {
		return getResource(TermSuiteResource.PREFIX_BANK);
	}
	public Path getRootBank() {
		return getResource(TermSuiteResource.ROOT_BANK);
	}

	@Deprecated
	public Path getEmptyDictionary() {
		return Paths.get("file:eu","project","ttc","resources","all","empty-dictionary.txt");
	}

	public Path getNeoclassicalPrefixes() {
		return getResource(TermSuiteResource.NEOCLASSICAL_PREFIXES);		
	}

	public Path getLanguageDico() {
		return getResource(TermSuiteResource.DICO);
	}


	public Path getCompostStopList() {
		return getResource(TermSuiteResource.COMPOST_STOP_LIST);
	}

	public Path getCompostInflectionRules() {
		return getResource(TermSuiteResource.COMPOST_INFLECTION_RULES);
	}
	
	public Path getCompostTransformationRules() {
		return getResource(TermSuiteResource.COMPOST_TRANSFORMATION_RULES);
	}
	
	public String getMateLemmatizerModelFileName() {
		return "mate-lemma-"+lang.getCode()+".model";
	}

	public String getMateTaggerModelFileName() {
		return "mate-pos-"+lang.getCode()+".model";
	}
}
