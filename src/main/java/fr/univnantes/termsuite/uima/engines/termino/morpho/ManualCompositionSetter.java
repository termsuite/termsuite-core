
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

package fr.univnantes.termsuite.uima.engines.termino.morpho;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.uima.resources.preproc.ManualSegmentationResource;
import fr.univnantes.termsuite.uima.resources.termino.TermIndexResource;

public class ManualCompositionSetter extends JCasAnnotator_ImplBase {
//	private static final Logger LOGGER = LoggerFactory.getLogger(ManualCompositionSetter.class);
	
	public static final String MANUAL_COMPOSITION_LIST = "ManualCompositionList";
	@ExternalResource(key=MANUAL_COMPOSITION_LIST, mandatory=true)
	private ManualSegmentationResource manualCompositions;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		Segmentation segmentation;
		for(Word word:termIndexResource.getTermIndex().getWords()) {
			segmentation = manualCompositions.getSegmentation(word.getLemma());
			if(segmentation != null) 
				if(segmentation.size() <= 1)
					word.resetComposition();
		}
	}

}
