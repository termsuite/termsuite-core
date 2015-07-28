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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.project.ttc.engines.desc.Lang;


/**
 * A helper class that build language-dependent term suite resource paths.
 * 
 * @author Damien Cram
 *
 */
public class TermSuiteResourceHelper {
	
	private Lang lang;
	private String resourcePath;
	
	public TermSuiteResourceHelper(Lang lang) {
		super();
		this.lang = lang;
		this.resourcePath = "file:" + lang.getCode() + File.separator;
//		this.resourcePath = Paths.get("file:eu", "project","ttc","resources", this.lang.getName());
	}

	public TermSuiteResourceHelper(Lang lang, String resourcePath) {
		this(lang);
	}
	

	public Path getTTParameter() {
		return Paths.get(resourcePath + this.lang.getName() + "-treetagger.xml");
	}

	public Path getAllowedChars() {
		return Paths.get(resourcePath + this.lang.getName() +"-allowed-chars.txt");
	}

	public Path getSegmentBank() {
		return Paths.get(resourcePath + this.lang.getName() +"-segment-bank.xml");
	}
	
	private Path getMapping(String tagger, String mappingType) {
		return Paths.get(resourcePath + this.lang.getName() +"-"+tagger+"-"+mappingType+"-mapping.xml");
	}
	
	public Path getCaseMapping(String tagger) {
		return getMapping(tagger, "case");
	}

	public Path getMoodMapping(String tagger) {
		return getMapping(tagger, "mood");
	}
	
	public Path getCategoryMapping(String tagger) {
		return getMapping(tagger, "category");
	}
	
	public Path getNumberMapping(String tagger) {
		return getMapping(tagger, "number");
	}
	
	public Path getSubcategoryMapping(String tagger) {
		return getMapping(tagger, "subcategory");
	}
	
	public Path getTenseMapping(String tagger) {
		return getMapping(tagger, "tense");
	}

	public Path getGenderMapping(String tagger) {
		return getMapping(tagger, "gender");
	}

	public Path getFrozenExpressionList() {
		return Paths.get(resourcePath + this.lang.getName() +"-frozen-expressions.list");
	}
	
	public Path getMWRegexes() {
		return Paths.get(resourcePath + this.lang.getName() +"-multi-word-rule-system.regex");
	}

	public Path getStopWords() {
		return Paths.get(resourcePath + this.lang.getName() +"-stop-word-filter.xml");
	}

	public Path getGroovyVariantRules() {
		return Paths.get(resourcePath + this.lang.getName() +"-variants.groovy");
	}
	
	public Path getYamlVariantRules() {
		return Paths.get(resourcePath + this.lang.getName() +"-variants.yaml");
	}
	
	public boolean resourceExists(String resourceURI) {
		return resourceURI != null && ClassLoader.getSystemClassLoader().getResourceAsStream(resourceURI) != null;
	}

	public Path getGeneralLanguageFrequencies() {
		return Paths.get(resourcePath + "GeneralLanguage." + this.lang.getNameUC());
	}
	public Path getPrefixBank() {
		return Paths.get(resourcePath + "Prefix." + this.lang.getNameUC());
	}
	public Path getRootBank() {
		return Paths.get(resourcePath + "RootBank." + this.lang.getNameUC());
	}

	public Path getEmptyDictionary() {
		return Paths.get("file:eu","project","ttc","resources","all","empty-dictionary.txt");
	}

	public Path getNeoclassicalPrefixes() {
		return Paths.get(resourcePath + this.lang.getName() +"-neoclassical-prefixes.txt");
	}

	public Path getLanguageDico() {
		return Paths.get(resourcePath + this.lang.getName() +"-dico.txt");
	}


	public Path getCompostStopList() {
		return Paths.get(resourcePath + this.lang.getName() +"-compost-stop-list.txt");
	}

	public Path getCompostInflectionRules() {
		return Paths.get(resourcePath + this.lang.getName() +"-compost-inflection-rules.txt");
	}
	
	public Path getCompostTransformationRules() {
		return Paths.get(resourcePath + this.lang.getName() +"-compost-transformation-rules.txt");
	}
	
	public String getMateLemmatizerModelFileName() {
		return "mate-lemma-"+lang.getCode()+".model";
	}

	public String getMateTaggerModelFileName() {
		return "mate-pos-"+lang.getCode()+".model";
	}
}
