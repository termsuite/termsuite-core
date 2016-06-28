/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package eu.project.ttc.engines;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Optional;

import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.utils.JCasUtils;

/**
 * Imports all {@link TermOccAnnotation} to a {@link TermIndex}.
 */
public class TermOccAnnotationImporter extends JCasAnnotator_ImplBase {
	
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String KEEP_OCCURRENCES_IN_TERM_INDEX = "KeepOccurrencesInTermIndex";
	@ConfigurationParameter(name = KEEP_OCCURRENCES_IN_TERM_INDEX, mandatory = false, defaultValue = "true")
	private boolean keepOccurrencesInTermIndex;


	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	};

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		Optional<SourceDocumentInformation> sdi = JCasUtils.getSourceDocumentAnnotation(jCas);
		String currentFileURI = sdi.isPresent() ? sdi.get().getUri() : "(no source uri given)";
		FSIterator<Annotation> it = jCas.getAnnotationIndex(TermOccAnnotation.type).iterator();
		TermOccAnnotation toa;
		while(it.hasNext()) {
			toa = (TermOccAnnotation) it.next();
			this.termIndexResource.getTermIndex().addTermOccurrence(
					toa, 
					currentFileURI, 
					keepOccurrencesInTermIndex);
			
		}
		this.termIndexResource.getTermIndex().getOccurrenceStore().flush();
	}
}
