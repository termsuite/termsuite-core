
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import eu.project.ttc.api.TermSuiteException;

public class TermSuiteResourceManager {
	private static TermSuiteResourceManager instance;
	
	private Map<String, Object> resources = Maps.newHashMap();
	
	private TermSuiteResourceManager() {
	}

	public static TermSuiteResourceManager getInstance() {
		if(instance == null)
			instance = new TermSuiteResourceManager();
		return instance;
	}
	
	public void register(String resourceName, Object resource) {
		Preconditions.checkArgument(
				!this.resources.containsKey(resourceName),
				"Resource already registered: %s",
				resourceName);
		this.resources.put(resourceName, resource);
	}
	
	public Object get(String resourceName) {
		Preconditions.checkArgument(
				this.resources.containsKey(getDecodedName(resourceName)),
				"No such resource: %s",
				getDecodedName(resourceName));
		return this.resources.get(getDecodedName(resourceName));
	}

	public boolean contains(String resourceName) {
			return this.resources.containsKey(getDecodedName(resourceName));
	}

	private String getDecodedName(String resourceName) {
		try {
			return URLDecoder.decode(resourceName, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			throw new TermSuiteException(e);
		}
	}


	public Object remove(String resourceName) {
		return this.resources.remove(resourceName);
	}

	public void clear() {
		this.resources.clear();
	}
}
