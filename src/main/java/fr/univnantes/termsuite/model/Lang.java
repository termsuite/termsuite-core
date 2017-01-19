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
package fr.univnantes.termsuite.model;

import java.util.Locale;
import java.util.NoSuchElementException;

import fr.univnantes.termsuite.LanguageException;

public enum Lang {
	FR("french", Locale.FRENCH),
	EN("english", Locale.ENGLISH),
	ES("spanish", Locale.FRENCH),
	DE("german", Locale.GERMAN),
	ZH("chinese", Locale.SIMPLIFIED_CHINESE),
	LV("latvian", Locale.ENGLISH),
	RU("russian", Locale.ENGLISH),
	DA("danish", Locale.GERMAN)
	;
	
	private final String longLang;
	private final Locale locale;

    private Lang(String longLang, Locale locale) {
        this.longLang = longLang;
        this.locale = locale;
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
	
	public Locale getLocale() {
		return locale;
	}
	
	public static Lang fromCode(String code) {
		for(Lang l:values())
			if(l.getCode().equals(code))
				return l;
		throw new NoSuchElementException(code);
	}
}
