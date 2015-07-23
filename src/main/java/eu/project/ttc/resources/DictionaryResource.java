/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryResource implements Dictionary {
	private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryResource.class);
	
	private Set<String> loaded;
	
	private void setLoaded() {
		this.loaded = new HashSet<String>();
	}
	
	private Set<String> getLoaded() {
		return this.loaded;
	}
	
	private boolean isLoaded(String id) {
		return this.loaded.contains(id);
	}
	
	private String source;
	
	public String source() {
		return this.source;
	}
	
	private String target;
	
	public String target() {
		return this.target;
	}
	
	private Map<String, Set<String>> entries;
	
	private void set() {
		this.entries = new HashMap<String, Set<String>>();
	}	
	
	@Override
	public Map<String, Set<String>> get() {
		return this.entries;
	}
	
	public DictionaryResource() {
		this.setLoaded();
		this.set();
	}

	@Override
	public void add(String source,String target) {
		String key = source.toLowerCase();
		Set<String> targetEntries = this.entries.get(key);
		if (targetEntries == null) {
			targetEntries = new HashSet<String>();
			this.entries.put(key, targetEntries);
		}
		targetEntries.add(target);
	}
	
    private Set<Entry<String, String>> parse(InputStream inputStream)
            throws IOException {
        Scanner scanner = null;
        try {
            Set<Entry<String, String>> entries = new HashSet<Entry<String, String>>();
            scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\r?\\n");
            while (scanner.hasNext()) {
                String line = scanner.next();
                String[] items = line.split("\t");
                String source = items[0];
                if (source != null) {
                    String target = items[1];
                    if (target != null) {
                        Entry<String, String> entry = new SimpleEntry<String, String>(
                                source, target);
                        entries.add(entry);
                    }
                }
            }
            return entries;
        } finally {
            IOUtils.closeQuietly(scanner);
        }
    }

	
	@Override
	public void load(URI resourceIdentifier) throws Exception {
		URLConnection connection = resourceIdentifier.toURL().openConnection();
		this.load(resourceIdentifier.getPath(), connection.getInputStream());
	}

	@Override
	public void load(String id, InputStream inputStream) throws IOException {
		if (!this.isLoaded(id)) {
			this.getLoaded().add(id);
			LOGGER.info("Loading " + id);
			Set<Entry<String, String>> entries = this.parse(inputStream);
			for (Entry<String, String> entry : entries) {
				String source = entry.getKey();
				String target = entry.getValue();
				this.add(source, target);
			}
		}
	}

	public void load(DataResource data) throws ResourceInitializationException { }

}
