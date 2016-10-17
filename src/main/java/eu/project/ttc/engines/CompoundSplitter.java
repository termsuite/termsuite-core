
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.engines;

import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;

import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.resources.Dictionary;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.TermSuiteConstants;

public class CompoundSplitter extends JCasAnnotator_ImplBase {

	public static final String DICTIONARY = "Dictionary";
	@ExternalResource(key = DICTIONARY, mandatory = true)
	private Dictionary dictionary;

	@ExternalResource(key = TermIndexResource.TERM_INDEX, mandatory = true)
	private TermIndexResource termIndexResource;
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		this.getContext().getLogger().log(Level.INFO, "Detecting compounds");
		if(termIndexResource.getTermIndex().getTerms().isEmpty())
			return;

		Iterator<Term> it = this.termIndexResource.getTermIndex().singleWordTermIterator();
		while (it.hasNext()) {
			Word word = it.next().firstWord().getWord();
			if(word.isCompound())
				continue;
			int index = word.getLemma().indexOf(TermSuiteConstants.COMPOUND_CHAR);
			if (index != -1 && index > 0 && index < word.getLemma().length() - 1) {
				WordBuilder.start()
					.setCompoundType(CompoundType.NATIVE)
					.addComponent(0, index, word.getLemma().substring(0, index))
					.addComponent(index+1, word.getLemma().length(), word.getLemma().substring(index+1, word.getLemma().length()))
					.create();
			}
		}
	}
	
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		// nothing to do at CAS level
	}
}