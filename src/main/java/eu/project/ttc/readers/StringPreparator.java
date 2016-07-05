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
package eu.project.ttc.readers;

import java.util.HashMap;
import java.util.Map;

public class StringPreparator {
	
	private int replacements = 0;
	
	private Map<Character, Character> map;
	
	public StringPreparator() {
		map = new HashMap<Character, Character>();
		map.put('`', '"');
		map.put('‘', '"');
		map.put('’', '\'');
		map.put('“', '"');
		map.put('”', '"');
		map.put('„', '"');
		map.put('\u00A0', ' ');
		map.put('\u2009', ' ');
		map.put('\u202F', ' ');
	}
	
	/**
	 * Replaces unusual characters in the input string (unusual quotes, apostrophes, whitespaces, etc)
	 * @param input
	 * @return
	 */
	public String prepare(String input) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length(); i++){
		    char c = input.charAt(i);        
		    if(map.get(c) != null) {
		    	replacements++;
		    	sb.append(map.get(c));
		    } else
		    	sb.append(c);
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "StringPreparator[replacements="+replacements+"]";
	}
}
