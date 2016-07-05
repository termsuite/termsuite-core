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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import eu.project.ttc.models.TermIndex;

public class MemoryTermIndexManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(MemoryTermIndexManager.class);


	private static final MemoryTermIndexManager INSTANCE = new MemoryTermIndexManager();
	private Map<String, TermIndex> indexes = Maps.newHashMap();
	
	private MemoryTermIndexManager() {
	}
	
	public static MemoryTermIndexManager getInstance() {
		return INSTANCE;
	}
	
	public TermIndex getIndex(String indexName) {
		if(!indexes.containsKey(indexName))
			throw new IllegalStateException("No such term index: " + indexName);
		return indexes.get(indexName);
	}

	public void register(TermIndex index) {
		Preconditions.checkNotNull(index.getName());
		if(indexes.containsKey(index.getName()))
			throw new IllegalStateException("Term index already registered: " + index.getName());
		LOGGER.debug("registering the index: " + index.getName());
		indexes.put(index.getName(), index);		
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("indexes", this.indexes.keySet()).toString();
	}

	public void clear() {
		this.indexes.clear();
	}

	public boolean containsTermIndex(String indexName) {
		return this.indexes.containsKey(indexName);
	}

	public Map<String, TermIndex> getTermIndexes() {
		return ImmutableMap.copyOf(indexes);
	}
}
