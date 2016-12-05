
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

package fr.univnantes.termsuite.test.unit.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Before;
import org.junit.Test;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.termino.MemoryTermIndex;
import fr.univnantes.termsuite.test.unit.Fixtures;
import fr.univnantes.termsuite.test.unit.TermFactory;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.uima.engines.termino.morpho.SuffixDerivationExceptionSetter;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.TermIndexResource;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

public class SuffixDerivationExceptionSetterSpec {
	
	private MemoryTermIndex termIndex;

	private Term ferme_n;
	private Term ferme_a;
	private Term paysage;
	private Term pays;
	private Term paysVraiDerive;
	
	private AnalysisEngine ae;
	
	@Before
	public void set() throws Exception {
		this.termIndex = Fixtures.termIndex();
		makeAE();
		populateTermIndex(new TermFactory(termIndex));
		ae.collectionProcessComplete();
	}

	private void populateTermIndex(TermFactory termFactory) {
		
		this.ferme_n = termFactory.create("N:ferme|ferm");
		this.ferme_a = termFactory.create("A:fermé|ferm");
		this.paysage = termFactory.create("N:paysage|paysag");
		this.pays = termFactory.create("N:pays|pay");
		this.paysVraiDerive = termFactory.create("N:paysvraiderivé|paysvraideriv");
		
		termFactory.addDerivesInto("N A", this.ferme_n, this.ferme_a);
		termFactory.addDerivesInto("N N", this.pays, this.paysage);
		termFactory.addDerivesInto("N N", this.pays, this.paysVraiDerive);
	}


	private void makeAE() throws ResourceInitializationException, InvalidXMLException, ClassNotFoundException {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		manager.clear();
		manager.register(termIndex.getName(), termIndex);
		
		AnalysisEngineDescription aeDesc = AnalysisEngineFactory.createEngineDescription(
				SuffixDerivationExceptionSetter.class
			);
		

		/*
		 * The history resource
		 */
		String  historyResourceName = "Toto";
		manager.register(historyResourceName, new TermHistory());
		ExternalResourceDescription historyResourceDesc = ExternalResourceFactory.createExternalResourceDescription(
				TermHistoryResource.TERM_HISTORY,
				TermHistoryResource.class, 
				historyResourceName
		);
		ExternalResourceFactory.bindResource(aeDesc, historyResourceDesc);
		


		/*
		 * The term index resource
		 */
		ExternalResourceDescription termIndexDesc = ExternalResourceFactory.createExternalResourceDescription(
				TermIndexResource.TERM_INDEX,
				TermIndexResource.class, 
				this.termIndex.getName()
		);
		ExternalResourceFactory.bindResource(aeDesc, termIndexDesc);

		/*
		 * The rule list resources
		 */
		ExternalResourceDescription rulesDesc = ExternalResourceFactory.createExternalResourceDescription(
				SuffixDerivationExceptionSetter.SUFFIX_DERIVATION_EXCEPTION,
				MultimapFlatResource.class, 
				"file:fr/univnantes/termsuite/test/resources/suffix-derivation-exceptions.txt"
		);
		ExternalResourceFactory.bindResource(aeDesc, rulesDesc);
		
		ae = AnalysisEngineFactory.createEngine(aeDesc);
	}
	

	@Test
	public void testProcessCollectionComplete() {
		assertThat(termIndex.getOutboundRelations(this.pays))
			.hasSize(1)
			.extracting(TermSuiteExtractors.RELATION_FROM_TYPE_TO)
			.contains(tuple(this.pays, RelationType.DERIVES_INTO, paysVraiDerive));
		assertThat(termIndex.getOutboundRelations(ferme_a)).hasSize(0);
	}
}