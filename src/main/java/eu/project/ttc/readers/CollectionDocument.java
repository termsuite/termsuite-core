
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

/**
 * An interface of input documents sent to collection readers
 * 
 * @author Damien Cram
 * 
 * @see StreamingCollectionReader
 *
 */
public interface CollectionDocument {

	/**
	 * The document sentinelle  sent to the reader to stipulate that the stream is ended
	 * 
	 */
	public static final CollectionDocument LAST_DOCUMENT = new CollectionDocument() {
		@Override
		public String getUri() {
			return "http://termsuite.github.io/documents/last";
		}
		@Override
		public String getText() {
			return null;
		}
	};

	/**
	 * The unique identifier of the document.
	 * 
	 * @return the uri
	 */
	public String getUri();
	
	/**
	 * The context of the text.
	 * 
	 * @return
	 */
	public String getText();

}
