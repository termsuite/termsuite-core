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
package eu.project.ttc.engines.desc;

import java.util.Locale;

import eu.project.ttc.engines.spotter.ChineseSpotter;
import eu.project.ttc.engines.spotter.DanishSpotter;
import eu.project.ttc.engines.spotter.EnglishSpotter;
import eu.project.ttc.engines.spotter.FrenchSpotter;
import eu.project.ttc.engines.spotter.GermanSpotter;
import eu.project.ttc.engines.spotter.LatvianSpotter;
import eu.project.ttc.engines.spotter.RussianSpotter;
import eu.project.ttc.engines.spotter.SpanishSpotter;
import eu.project.ttc.utils.OccurrenceBuffer;

public enum Lang {
	FR("french", Locale.FRENCH, OccurrenceBuffer.NO_CLEANING, FrenchSpotter.class, 0.5f, 0.1f, 0.1f, 0.3f, 0.7f, 3),
	EN("english", Locale.ENGLISH, OccurrenceBuffer.NO_CLEANING, EnglishSpotter.class, 0.7f, 0.1f, 0.1f, 0.1f, 0.85f, 3),
	ES("spanish", Locale.FRENCH, OccurrenceBuffer.NO_CLEANING, SpanishSpotter.class, 0.5f, 0.1f, 0.1f, 0.3f, 1f, 3),
	DE("german", Locale.GERMAN, OccurrenceBuffer.NO_CLEANING, GermanSpotter.class, 0.5f, 0.3f, 0.1f, 0.1f, 0.85f, 4),
	ZH("chinese", Locale.CHINESE, OccurrenceBuffer.NO_CLEANING, ChineseSpotter.class, 0.5f, 0.1f, 0.1f, 0.3f, 0.7f, 2),
	LV("latvian", Locale.GERMAN, OccurrenceBuffer.NO_CLEANING, LatvianSpotter.class,0.5f, 0.1f, 0.1f, 0.3f, 0.8f, 3),
	RU("russian", Locale.JAPAN, OccurrenceBuffer.NO_CLEANING, RussianSpotter.class,0.3f, 0.1f, 0.4f, 0.2f, 0.7f, 3),
	DA("danish", Locale.GERMAN, OccurrenceBuffer.NO_CLEANING, DanishSpotter.class,0.5f, 0.1f, 0.1f, 0.3f, 0.8f, 3);
	
	private final float compostAlpha;
	private final float compostBeta;
	private final float compostGamma;
	private final float compostDelta;
	private final float compostThreshold;
	private final int compostMaxComponentNumber;
	private final Locale locale;
	private final String longLang;
	private final String regexPostProcessingStrategy;
	private final Class<? extends SpotterBuilder> spotterBuilder;

    private Lang(String longLang, Locale locale, String regexPostProcessingStrategy, Class<? extends SpotterBuilder> spotterBuilder,
    		float compostAlpha,
    		float compostBeta,
    		float compostGamma,
    		float compostDelta,
    		float compostThreshold,
    		int compostMaxComponentNumber
    		) {
    	this.locale = locale;
        this.longLang = longLang;
        this.regexPostProcessingStrategy = regexPostProcessingStrategy;
        this.spotterBuilder = spotterBuilder;
        this.compostAlpha = compostAlpha;
        this.compostBeta = compostBeta;
        this.compostGamma = compostGamma;
        this.compostDelta = compostDelta;
        this.compostThreshold = compostThreshold;
        this.compostMaxComponentNumber = compostMaxComponentNumber;
    }
    
    public String getName() {
		return longLang;
	}
    
    public String getNameUC() {
		return getName().substring(0,1).toUpperCase() + getName().substring(1);
	}
    
    @Override
    public String toString() {
    	return getCode();
    }

	public String getCode() {
		return name().toLowerCase();
	}
    
    public String getRegexPostProcessingStrategy() {
		return regexPostProcessingStrategy;
	}
    
	public static void checkLang(String lang) {
		if(!isLanguageSupported(lang))
			throw new LanguageException(lang);
	}

	public static boolean isLanguageSupported(String lang) {
		for(Lang l:Lang.values()) {
			if(l.getCode().equals(lang)) 
				return true;
		}
		return false;
	}
	
	public static Lang forName(String lang) {
		for(Lang l:Lang.values()) {
			if(l.getCode().equals(lang))
				return l;
		}
		throw new LanguageException(lang);
	}
	
	public Class<? extends SpotterBuilder> getSpotterBuilder() {
		return spotterBuilder;
	}

	public Locale getLocale() {
		return locale;
	}
	
	public float getCompostAlpha() {
		return compostAlpha;
	}
	
	public float getCompostBeta() {
		return compostBeta;
	}
	public float getCompostDelta() {
		return compostDelta;
	}
	public float getCompostGamma() {
		return compostGamma;
	}
	
	public int getCompostMaxComponentNumber() {
		return compostMaxComponentNumber;
	}
	
	public float getCompostThreshold() {
		return compostThreshold;
	}
}
