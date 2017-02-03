
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

package fr.univnantes.termsuite.test.unit.engines.splitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.FileInputStream;

import org.apache.uima.resource.DataResource;
import org.junit.Before;
import org.junit.Test;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.engines.splitter.ManualSuffixDerivationDetecter;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.unit.TermFactory;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.test.unit.UnitTests;
import fr.univnantes.termsuite.uima.ResourceType;
import io.codearte.catchexception.shade.mockito.Mockito;

public class ManualSuffixDerivationDetecterSpec {
	
	private Terminology termino;

	private Term ferme_n;
	private Term ferme_a;
	private Term paysage;
	private Term pays;
	private Term paysVraiDerive;
	
	
	@Before
	public void set() throws Exception {
		IndexedCorpus corpus = TermSuiteFactory.createIndexedCorpus(Lang.EN, "");
		this.termino = corpus.getTerminology();
		DataResource dataReosurce = Mockito.mock(DataResource.class);
		Mockito.when(dataReosurce.getInputStream())
			.thenReturn(new FileInputStream("src/test/resources/fr/univnantes/termsuite/test/resources/suffix-derivation-exceptions.txt"));
		MultimapFlatResource derivationExceptions = new MultimapFlatResource();
		derivationExceptions.load(dataReosurce);
		populateTermino(new TermFactory(termino));
		EngineRunner engine = UnitTests.createEngineRunner(
				corpus, 
				ManualSuffixDerivationDetecter.class, 
				UnitTests.mockResourceModule().bind(
						ResourceType.SUFFIX_DERIVATION_EXCEPTIONS, derivationExceptions));
		
		engine.run();
	}

	private void populateTermino(TermFactory termFactory) {
		
		this.ferme_n = termFactory.create("N:ferme|ferm");
		this.ferme_a = termFactory.create("A:fermé|ferm");
		this.paysage = termFactory.create("N:paysage|paysag");
		this.pays = termFactory.create("N:pays|pay");
		this.paysVraiDerive = termFactory.create("N:paysvraiderivé|paysvraideriv");
		
		termFactory.addDerivesInto("N A", this.ferme_n, this.ferme_a);
		termFactory.addDerivesInto("N N", this.pays, this.paysage);
		termFactory.addDerivesInto("N N", this.pays, this.paysVraiDerive);
	}

	@Test
	public void testProcessCollectionComplete() {
		assertThat(UnitTests.outRels(termino, ferme_a)).hasSize(0);
		assertThat(UnitTests.outRels(termino, ferme_n)).hasSize(0);
		assertThat(UnitTests.outRels(termino, this.pays))
			.hasSize(1)
			.extracting(TermSuiteExtractors.RELATION_FROM_TYPE_TO)
			.contains(tuple(this.pays, RelationType.DERIVES_INTO, paysVraiDerive))
			/*
			 * Should not detect pays->paysage because it is an exception
			 */
			;
	}
}
