
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

package eu.project.ttc.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.resource.RelativePathResolver;
import org.apache.uima.resource.impl.RelativePathResolver_impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteResourceException;
import eu.project.ttc.utils.TermSuiteConstants;
import eu.project.ttc.utils.URLUtils;

/**
 * 
 * A meta-type for TermSuite linguistic resources.
 * 
 * @author Damien Cram
 *
 */
public enum TermSuiteResource {
	GENERAL_LANGUAGE("[LANG_SHORT]/[LANG]-general-language.txt", "", ""),
	PREFIX_BANK("[LANG_SHORT]/morphology/[LANG]-prefix-bank.txt", "", ""),
	PREFIX_EXCEPTIONS("[LANG_SHORT]/morphology/[LANG]-prefix-exceptions.txt", "", ""),
	MANUAL_COMPOSITIONS("[LANG_SHORT]/morphology/[LANG]-manual-composition.txt", "", ""),
	ROOT_BANK("[LANG_SHORT]/morphology/[LANG]-root-bank.txt", "", ""),
	ALLOWED_CHARS("[LANG_SHORT]/[LANG]-allowed-chars.txt", "", ""),
	SUFFIX_DERIVATIONS("[LANG_SHORT]/morphology/[LANG]-suffix-derivation-bank.txt", "", ""),
	SUFFIX_DERIVATION_EXCEPTIONS("[LANG_SHORT]/morphology/[LANG]-suffix-derivation-exceptions.txt", "", ""),
	COMPOST_INFLECTION_RULES("[LANG_SHORT]/morphology/[LANG]-compost-inflection-rules.txt", "", ""),
	COMPOST_STOP_LIST("[LANG_SHORT]/morphology/[LANG]-compost-stop-list.txt", "", ""),
	COMPOST_TRANSFORMATION_RULES("[LANG_SHORT]/morphology/[LANG]-compost-transformation-rules.txt", "", ""),
	DICO("[LANG_SHORT]/[LANG]-dico.txt", "", ""),
	FIXED_EXPRESSIONS("[LANG_SHORT]/[LANG]-fixed-expressions.txt", "", ""),
	TAGGER_CASE_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-case-mapping.xml", "", ""),
	TAGGER_CATEGORY_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-category-mapping.xml", "", ""),
	TAGGER_GENDER_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-gender-mapping.xml", "", ""),
	TAGGER_MOOD_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-mood-mapping.xml", "", ""),
	TAGGER_NUMBER_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-number-mapping.xml", "", ""),
	TAGGER_SUBCATEGORY_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-subcategory-mapping.xml", "", ""),
	TAGGER_TENSE_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-tense-mapping.xml", "", ""),
	MWT_RULES("[LANG_SHORT]/[LANG]-multi-word-rule-system.regex", "", ""),
	NEOCLASSICAL_PREFIXES("[LANG_SHORT]/morphology/[LANG]-neoclassical-prefixes.txt", "", ""),
	SEGMENT_BANK("[LANG_SHORT]/[LANG]-segment-bank.xml", "", ""),
	STOP_WORDS_FILTER("[LANG_SHORT]/[LANG]-stop-word-filter.xml", "", ""),
	TREETAGGER_CONFIG("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-treetagger.xml", "", ""),
	VARIANTS("[LANG_SHORT]/[LANG]-variants.yaml", "", ""), 
	;
	
	private static final String MSG_ERR_RESOURCE_NOT_FOUND = "Resource %s does not exist for resource %s (resolved URL is %s)";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteResource.class);

	
	private String pathPattern;
	private String title;
	private String description;

	
	private TermSuiteResource(String pathPattern, String title, String description) {
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
			resolver = new RelativePathResolver_impl(TermSuiteResource.class.getClassLoader()); 
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
	
	
	public static final TermSuiteResource forFileName(String fileName) {
		for(Lang l:Lang.values()) {
			for(Tagger t:Tagger.values()) {
				for(TermSuiteResource r:TermSuiteResource.values())
					if(r.getPath(l, t).equals(fileName))
						return r;
			}
		}
		return null;
	}
	
	
}
