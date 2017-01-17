
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

package fr.univnantes.termsuite.uima;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.resource.RelativePathResolver;
import org.apache.uima.resource.impl.RelativePathResolver_impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.lina.uima.models.TreeTaggerParameter;
import fr.univnantes.lina.uima.tkregex.ae.RegexListResource;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSet;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.uima.resources.preproc.CharacterFootprintTermFilter;
import fr.univnantes.termsuite.uima.resources.preproc.FixedExpressionResource;
import fr.univnantes.termsuite.uima.resources.preproc.ManualSegmentationResource;
import fr.univnantes.termsuite.uima.resources.preproc.PrefixTree;
import fr.univnantes.termsuite.uima.resources.preproc.SimpleWordSet;
import fr.univnantes.termsuite.uima.resources.termino.CompostInflectionRules;
import fr.univnantes.termsuite.uima.resources.termino.GeneralLanguageResource;
import fr.univnantes.termsuite.uima.resources.termino.SuffixDerivationList;
import fr.univnantes.termsuite.utils.TermSuiteConstants;
import fr.univnantes.termsuite.utils.URLUtils;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.lexer.resources.SegmentBankResource;
import uima.sandbox.mapper.resources.Mapping;
import uima.sandbox.mapper.resources.MappingResource;

/**
 * 
 * A meta-type for TermSuite linguistic resources.
 * 
 * @author Damien Cram
 *
 */
public enum ResourceType {
	GENERAL_LANGUAGE(GeneralLanguageResource.class, "[LANG_SHORT]/[LANG]-general-language.txt", "", ""),
	SYNONYMS(MultimapFlatResource.class, "[LANG_SHORT]/[LANG]-synonyms.txt", "", ""),
	PREFIX_BANK(PrefixTree.class, "[LANG_SHORT]/morphology/[LANG]-prefix-bank.txt", "", ""),
	PREFIX_EXCEPTIONS(ManualSegmentationResource.class, "[LANG_SHORT]/morphology/[LANG]-prefix-exceptions.txt", "", ""),
	MANUAL_COMPOSITIONS(ManualSegmentationResource.class, "[LANG_SHORT]/morphology/[LANG]-manual-composition.txt", "", ""),
	@Deprecated 
	ROOT_BANK(Object.class, "[LANG_SHORT]/morphology/[LANG]-root-bank.txt", "", ""),
	ALLOWED_CHARS(CharacterFootprintTermFilter.class, "[LANG_SHORT]/[LANG]-allowed-chars.txt", "", ""),
	SUFFIX_DERIVATIONS(SuffixDerivationList.class, "[LANG_SHORT]/morphology/[LANG]-suffix-derivation-bank.txt", "", ""),
	SUFFIX_DERIVATION_EXCEPTIONS(MultimapFlatResource.class, "[LANG_SHORT]/morphology/[LANG]-suffix-derivation-exceptions.txt", "", ""),
	COMPOST_INFLECTION_RULES(CompostInflectionRules.class, "[LANG_SHORT]/morphology/[LANG]-compost-inflection-rules.txt", "", ""),
	COMPOST_STOP_LIST(SimpleWordSet.class, "[LANG_SHORT]/morphology/[LANG]-compost-stop-list.txt", "", ""),
	COMPOST_TRANSFORMATION_RULES(CompostInflectionRules.class, "[LANG_SHORT]/morphology/[LANG]-compost-transformation-rules.txt", "", ""),
	DICO(SimpleWordSet.class, "[LANG_SHORT]/[LANG]-dico.txt", "", ""),
	FIXED_EXPRESSIONS(FixedExpressionResource.class, "[LANG_SHORT]/[LANG]-fixed-expressions.txt", "", ""),
	TAGGER_CASE_MAPPING(MappingResource.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-case-mapping.xml", "", ""),
	TAGGER_CATEGORY_MAPPING(MappingResource.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-category-mapping.xml", "", ""),
	TAGGER_GENDER_MAPPING(MappingResource.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-gender-mapping.xml", "", ""),
	TAGGER_MOOD_MAPPING(MappingResource.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-mood-mapping.xml", "", ""),
	TAGGER_NUMBER_MAPPING(MappingResource.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-number-mapping.xml", "", ""),
	TAGGER_SUBCATEGORY_MAPPING(MappingResource.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-subcategory-mapping.xml", "", ""),
	TAGGER_TENSE_MAPPING(MappingResource.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-tense-mapping.xml", "", ""),
	MWT_RULES(RegexListResource.class, "[LANG_SHORT]/[LANG]-multi-word-rule-system.regex", "", ""),
	NEOCLASSICAL_PREFIXES(SimpleWordSet.class, "[LANG_SHORT]/morphology/[LANG]-neoclassical-prefixes.txt", "", ""),
	SEGMENT_BANK(SegmentBankResource.class, "[LANG_SHORT]/[LANG]-segment-bank.xml", "", ""),
	STOP_WORDS_FILTER(DefaultFilterResource.class, "[LANG_SHORT]/[LANG]-stop-word-filter.xml", "", ""),
	TREETAGGER_CONFIG(TreeTaggerParameter.class, "[LANG_SHORT]/tagging/[TAGGER]/[LANG]-treetagger.xml", "", ""),
	VARIANTS(YamlRuleSet.class, "[LANG_SHORT]/[LANG]-variants.yaml", "", ""), 
	;
	
	private static final String MSG_ERR_RESOURCE_NOT_FOUND = "Resource %s does not exist for resource %s (resolved URL is %s)";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceType.class);

	private Class<?> resourceClass;
	private String pathPattern;
	private String title;
	private String description;

	
	private ResourceType(Class<?> resourceClass, String pathPattern, String title, String description) {
		this.resourceClass = resourceClass;
		this.pathPattern = pathPattern;
		this.title = title;
		this.description = description;
	}
	
	public String getPathPattern() {
		return pathPattern;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	private static final String TAGGER_SHORT_PATTERN = "[TAGGER_SHORT]";
	private static final String TAGGER_PATTERN = "[TAGGER]";
	private static final String LANG_PATTERN = "[LANG]";
	private static final String LANG_SHORT_PATTERN = "[LANG_SHORT]";


	private URL checkUrl(URL url) {
		Preconditions.checkNotNull(url, "Failed to load resource %s. Url is null.", this);
		try(InputStream is = url.openStream()) {
			Preconditions.checkNotNull(is, "Failed to load resource %s. Got a null input stream for url %s",
					this, url);
			return url;
		} catch(IOException e) {
			throw new TermSuiteResourceException(
					String.format("Cannot open stream for resource %s and url %s", this, url), 
					e);
		}
	}
	
	private static RelativePathResolver resolver = null;
	private static RelativePathResolver getResolver() {
		if(resolver == null)
			resolver = new RelativePathResolver_impl(ResourceType.class.getClassLoader()); 
		return resolver;
	}
	
	public URL fromClasspath(Lang lang) {
		URL url = fromClassPathUnchecked(lang);
		return checkUrl(url);
	}

	public URL fromClassPathUnchecked(Lang lang) {
		String classpathPath = TermSuiteConstants.DEFAULT_RESOURCE_URL_PREFIX + getPath(lang);
		URL url = getClass().getResource(classpathPath);
		return url;
	}


	public boolean existsInClasspath(Lang lang, Tagger tagger) {
		return fromClassPathUnchecked(lang, tagger) != null;
	}
	

	public boolean exists(Lang lang) {
		return fromClassPathUnchecked(lang) != null;
	}

	public URL fromClasspath(Lang lang, Tagger tagger) {
		URL url = fromClassPathUnchecked(lang, tagger);
		return checkUrl(url);
	}

	public URL fromClassPathUnchecked(Lang lang, Tagger tagger) {
		String classpathPath =TermSuiteConstants.DEFAULT_RESOURCE_URL_PREFIX + getPath(lang, tagger);
		URL url = getClass().getResource(classpathPath);
		return url;
	}

	public URL fromUrlPrefix(URL prefix, Lang lang) {
		URL url = resolve(prefix, lang, null);
		return checkUrl(url);
	}

	
	public URL fromUrlPrefix(URL prefix, Lang lang, Tagger tagger) {
		URL url = resolve(prefix, lang, tagger);
		return checkUrl(url);
	}

	private URL resolve(URL prefix, Lang lang, Tagger tagger) {
		try {
			return URLUtils.join(prefix, getPath(lang,tagger));
		} catch (MalformedURLException e) {
			LOGGER.error("failed to build url: " + prefix.toString() + getPath(lang,tagger));
			throw new RuntimeException(e);
		}
	}

	public String getPath(Lang lang) {
		return getPath(lang, null);
	}
	
	public String getPath(Lang lang, Tagger tagger) {
		Preconditions.checkNotNull(lang);
		String path = getPathPattern()
				.replace(LANG_SHORT_PATTERN, lang.getCode())
				.replace(LANG_PATTERN, lang.getName().toLowerCase());
		if(getPathPattern().contains(TAGGER_PATTERN) || getPathPattern().contains(TAGGER_SHORT_PATTERN)) {
			Preconditions.checkArgument(
					tagger != null, 
					"Tagger should not be nil for resource %s.", 
					this.toString().toLowerCase());
			path = path
					.replace(TAGGER_SHORT_PATTERN, tagger.getShortName())
					.replace(TAGGER_PATTERN, tagger.getName());
		}
		
		return path;
		
	} 
	
	
	public static final ResourceType forFileName(String fileName) {
		for(Lang l:Lang.values()) {
			for(Tagger t:Tagger.values()) {
				for(ResourceType r:ResourceType.values())
					if(r.getPath(l, t).equals(fileName))
						return r;
			}
		}
		return null;
	}

	public Class<?> getResourceClass() {
		return resourceClass;
	}
	
	
}
