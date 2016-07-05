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

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.util.Progress;

// TODO Create an abstract super class for collection reading
public class EmptyCollectionReader extends CollectionReader_ImplBase {

	private boolean loaded = false;
	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		// do nothing
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		if(!loaded) {
			loaded = true;
			return true;
		} else
			return false;
	}

	@Override
	public Progress[] getProgress() {
		return null;
	}

	@Override
	public void close() throws IOException {
	}
}
