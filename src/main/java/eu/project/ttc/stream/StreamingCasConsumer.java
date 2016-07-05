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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.TermSuiteCas;
import eu.project.ttc.readers.StreamingCollectionReader;
import eu.project.ttc.utils.JCasUtils;

/**
 * An output AE for streaming collection readers.
 * 
 * @author Damien Cram
 * 
 * @see StreamingCollectionReader
 *
 */
public class StreamingCasConsumer extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamingCasConsumer.class);
	
	public static final String PARAM_CONSUMER_NAME = "ConsumerName";
	@ConfigurationParameter(name = PARAM_CONSUMER_NAME, mandatory=false, defaultValue = PARAM_CONSUMER_NAME)
	private String consumerName;

	private CasConsumer consumer;
	
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.consumer = ConsumerRegistry.getInstance().getConsumer(consumerName);
	}
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		LOGGER.info("Consuming document {}", JCasUtils.getSourceDocumentAnnotation(jCas).get().getUri());
		consumer.consume(new TermSuiteCas(jCas));
	}
}
