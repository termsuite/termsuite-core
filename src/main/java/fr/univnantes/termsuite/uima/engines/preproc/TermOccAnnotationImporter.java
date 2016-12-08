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

import java.util.Optional;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import fr.univnantes.termsuite.engines.SWTFlagSetter;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;
import fr.univnantes.termsuite.utils.JCasUtils;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermSuiteUtils;

/**
 * Imports all {@link TermOccAnnotation} to a {@link Terminology}.
 */
public class TermOccAnnotationImporter extends JCasAnnotator_ImplBase {
	
	
	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	private TerminologyResource terminoResource;
	
	public static final String KEEP_OCCURRENCES_IN_TERMINOLOGY = "KeepOccurrencesInTerminology";
	@ConfigurationParameter(name = KEEP_OCCURRENCES_IN_TERMINOLOGY, mandatory = false, defaultValue = "true")
	private boolean keepOccurrencesInTermino;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

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
			String gKey = TermSuiteUtils.getGroupingKey(toa);
			TermHistory history = historyResource.getHistory();
			watch(gKey, history);
	
			this.terminoResource.getTerminology().addTermOccurrence(
					toa, 
					currentFileURI, 
					keepOccurrencesInTermino);
			
		}
		this.terminoResource.getTerminology().getOccurrenceStore().flush();
	}

	private void watch(String gKey, TermHistory history) {
		if(history.isWatched(gKey)) {
			if(this.terminoResource.getTerminology().getTermByGroupingKey(gKey) == null)
				history.saveEvent(
					gKey, 
					this.getClass(), 
					"Term added to Terminology " + terminoResource.getTerminology().getName());
		}
	}
	
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		new SWTFlagSetter().setSWTFlags(terminoResource.getTerminology());
	}
}
