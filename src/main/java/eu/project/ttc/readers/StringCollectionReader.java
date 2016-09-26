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
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.utils.JCasUtils;

public class StringCollectionReader extends CollectionReader_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringCollectionReader.class);
	
	public static final String PARAM_TEXT = "Text";
	@ConfigurationParameter(name = PARAM_TEXT, mandatory=true)
	private String text;
	
	public static final String PARAM_LANGUAGE = "TxtLanguage";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory=true)
	private String mLanguage;

	private boolean done = false;
	private StringPreparator preparator;
	
	@Override
	public void initialize() throws ResourceInitializationException {
		this.text = (String) getConfigParameterValue(PARAM_TEXT);
		this.mLanguage = (String) getConfigParameterValue(PARAM_LANGUAGE);
		
		this.preparator = new StringPreparator();
	}

	@Override
	public void getNext(CAS cas) throws IOException, CollectionException {
		LOGGER.info("Reading inline string of size " + text.length());
		try {
			cas.setDocumentText(this.text);
			cas.setDocumentLanguage(mLanguage);
			JCasUtils.initJCasSDI(cas.getJCas(), this.mLanguage, preparator.prepare(this.text), "http://inline.text/");
			this.done  = true;
		} catch (CASException e) {
			throw new CollectionException(e);
		}
	}



	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return !done;
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[]{};
	}

	@Override
	public void close() throws IOException {
	}
}
