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
package fr.univnantes.termsuite.uima.engines.preproc;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.utils.JCasUtils;

/**
 * Logs a message indicating which document is being processed.
 * 
 * @author Damien Cram
 *
 */
public class DocumentLogger extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentLogger.class);

	public static final String NB_DOCUMENTS = "NbDocuments";
	@ConfigurationParameter(name=NB_DOCUMENTS, mandatory=true)
	private long nbDocuments;

	private int docCnt = 0;

	private long lastTop = 0;

	private static long TIME_SEP = 1000;
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		long top = System.currentTimeMillis();
		docCnt += 1;
		SourceDocumentInformation sdi = JCasUtils.getSourceDocumentAnnotation(jCas).get();
		String msg = String.format("Processing document %d on %d [%.2f%%]. Uri: %s", 
				docCnt,
				nbDocuments,
				100 * ((double)docCnt/nbDocuments),
				sdi.getUri()
				);
		if(top - lastTop > TIME_SEP ) {
			lastTop = top;
			LOGGER.info(msg);
		} else if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(msg);			
		}
	}
}
