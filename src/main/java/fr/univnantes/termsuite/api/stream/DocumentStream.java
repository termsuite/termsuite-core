
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

package fr.univnantes.termsuite.api.stream;

import fr.univnantes.termsuite.uima.readers.CollectionDocument;

public class DocumentStream {
	private DocumentProvider provider;
	private Thread streamThread;
	private CasConsumer consumer;
	private String queueName;

	public DocumentStream(Thread streamThread, DocumentProvider provider, CasConsumer consumer, String queueName) {
		super();
		this.streamThread = streamThread;
		this.provider = provider;
		this.queueName = queueName;
		this.consumer = consumer;
	}

	public void addDocument(CollectionDocument doc) {
		provider.provide(doc);
	}

	public Thread getStreamThread() {
		return streamThread;
	}

	public void flush() {
		provider.provide(CollectionDocument.LAST_DOCUMENT);
		try {
			streamThread.join();
		} catch (InterruptedException e) {
			new RuntimeException(e);
		}
	}
	
	public CasConsumer getConsumer() {
		return consumer;
	}
	
	public String getQueueName() {
		return queueName;
	}

}
