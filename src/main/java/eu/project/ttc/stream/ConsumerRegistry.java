
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

package eu.project.ttc.stream;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import eu.project.ttc.readers.StreamingCollectionReader;

/**
 * 
 * A registry for {@link CasConsumer}s.
 * 
 * @author Damien Cram
 * @see CasConsumer
 * @see StreamingCollectionReader
 *
 */
public class ConsumerRegistry {

	/*
	 * SINGLETON
	 */
	private static ConsumerRegistry instance = null;

	private ConsumerRegistry() {
		super();
	}
	
	/**
	 * Returns the registry singleton.
	 * 
	 * @return the registry
	 */
	public static ConsumerRegistry getInstance() {
		if(instance == null)
			instance = new ConsumerRegistry();
		return instance;
	}
	
	private Map<String, CasConsumer> registry = Maps.newConcurrentMap();
	
	public CasConsumer getConsumer(String consumerName) {
		Preconditions.checkArgument(registry.containsKey(consumerName), "Queue %s does not exist", consumerName);
		return registry.get(consumerName);
	}

	
	public void registerConsumer(String consumerName, CasConsumer consumer) {
		Preconditions.checkNotNull(consumer);
		Preconditions.checkArgument(!registry.containsKey(consumerName), "Consumer %s already exists", consumerName);
		registry.put(consumerName, consumer);
	}

}
