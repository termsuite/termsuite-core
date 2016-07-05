
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

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

/**
 * 
 * A registry for queues of {@link CollectionDocument}, used in {@link StreamingCollectionReader}.
 * 
 * @author Damien Cram
 * @see StreamingCollectionReader
 *
 */
public class QueueRegistry {

	/*
	 * SINGLETON
	 */
	private static QueueRegistry instance = null;

	private QueueRegistry() {
		super();
	}
	
	/**
	 * Returns the registry singleton.
	 * 
	 * @return the registry
	 */
	public static QueueRegistry getInstance() {
		if(instance == null)
			instance = new QueueRegistry();
		return instance;
	}
	
	private Map<String, BlockingQueue<CollectionDocument>> registry = Maps.newConcurrentMap();
	
	/**
	 * Retrieves and returns a registered queue by name.
	 * 
	 * @param queueName
	 * 			The name of the queue to retrieve
	 * @return
	 * 			The retrieved queue with name <code>queueName</code>
	 * 
	 * @throws IllegalArgumentException if there is queue with the name <code>queueName</code>
	 */
	public BlockingQueue<CollectionDocument> getQueue(String queueName) {
		Preconditions.checkArgument(registry.containsKey(queueName), "Queue %s does not exist", queueName);
		return registry.get(queueName);
	}

	
	/**
	 * Creates and registers a new {@link ArrayBlockingQueue} with given initial
	 * capacity.
	 *  
	 * @param queueName
	 * 			The name of the queue
	 * @param capacity
	 * 			The initial capacity of the queue
	 * @return
	 * 		the created and registered queue
	 * 
	 * @throws IllegalArgumentException if <code>queueName</code> is already registered.
	 */
	public BlockingQueue<CollectionDocument> registerQueue(String queueName, int capacity) {
		Preconditions.checkArgument(!registry.containsKey(queueName), "Queue %s already exists", queueName);
		ArrayBlockingQueue<CollectionDocument> q = Queues.newArrayBlockingQueue(capacity);
		registry.put(queueName, q);
		return q;
	}

}
