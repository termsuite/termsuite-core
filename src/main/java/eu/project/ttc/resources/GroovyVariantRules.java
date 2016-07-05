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
package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

@Deprecated
public class GroovyVariantRules implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(GroovyVariantRules.class);

	private GroovyObject script;

	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		InputStream inputStream = null;
		try {
			inputStream = aData.getInputStream();
			loadResource(inputStream);
		} catch (IOException e) {
			LOGGER.error("Could not load the groovy variant rules resource");
			throw new ResourceInitializationException(e);
		} finally {
			if(inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new ResourceInitializationException(e);
				}
		}
	}

	public void loadResource(final InputStream inputStream)
			throws ResourceInitializationException {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader(
				parent);
		StringWriter writer = null;
		InputStreamReader reader = null;
		try {
			String scriptString = getScriptHeader();
			writer = new StringWriter();
			reader = new InputStreamReader(inputStream, "UTF-8");
			IOUtils.copy(reader, writer);
			scriptString += "\n" + writer.toString();
			scriptString += "\n" + getScriptFooter();
			Class<?> groovyClass = groovyClassLoader.parseClass(scriptString);
			LOGGER.info("Loading groovy variant rules");
			this.script = (GroovyObject) groovyClass.newInstance();
			this.script.invokeMethod("init", new Object[] {});
			initProfilingWithZeroCount();
			LOGGER.info("Groovy variant rules loaded.");
		} catch (Exception e) {
			LOGGER.error("Could not load the groovy variant rules resource");
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(writer);
			try {
				groovyClassLoader.close();
			} catch (IOException e) {
				LOGGER.warn( "Could not close groovy class loader");
			}
		}
	}

	private static String getScriptHeader() {
		
		return "import groovy.transform.Field"
				+ "\n" + "@Field Map rules = new LinkedHashMap();"
				+ "\n" + "@Field Map errors = new HashMap();"
				+ "\n" + "def void rule(name, closure) {"
				+ "\n" + "	rules.put(name, closure);"
				+ "\n" + "}"
				+ "\n" + "def Set getRuleNames() {"
				+ "\n" + "	return rules.keySet();"
				+ "\n" + "}"
				+ "\n" + "def String getMatchingRule(source, target) {"
				+ "\n" + "	for (Iterator it = rules.entrySet().iterator(); it.hasNext();) {"
				+ "\n" + "		e = it.next();"
				+ "\n" + "		try {"
				+ "\n" + "			if(e.getValue()(source, target))"
				+ "\n" + "				return e.getKey();"
				+ "\n" + "}catch(Throwable t) {"
				+ "\n" + "	    	if(errors.get(e.getKey()) == null)"
						+ "				errors.put(e.getKey(), new LinkedList());"
				+ "\n" + "	    	errors.get(e.getKey()).add(t);"
				+ "\n" + "}"
				+ "\n" + "	}"
				+ "\n" + "  return null;"
				+ "\n" + "}"
				+ "\n" + "def init()  {" 
				+ "\n";
		
	}
	private static String getScriptFooter() {
		return  "\n}";
	}

	public Map<?,?> getErrors() {
		try {
			return (Map<?,?>)this.script.getProperty("errors");
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * Makes the profiler display "0" when a rule does not match, instead of leaving it absent
	 */
	public void initProfilingWithZeroCount() {
		try {
			Object invokeMethod = this.script.invokeMethod("getRuleNames", new Object[] { });
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getMatchingRule(Object s, Object t) {
		try {
			Object invokeMethod = this.script.invokeMethod("getMatchingRule", new Object[] { s, t });
			return (String) invokeMethod;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
