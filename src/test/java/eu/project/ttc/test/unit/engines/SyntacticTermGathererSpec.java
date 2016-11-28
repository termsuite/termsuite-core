
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

package eu.project.ttc.test.unit.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.test.unit.Fixtures;
import eu.project.ttc.test.unit.TermFactory;
import eu.project.ttc.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.termino.MemoryTermIndex;
import fr.univnantes.termsuite.uima.engines.termino.TermGathererAE;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.TermIndexResource;
import fr.univnantes.termsuite.uima.resources.termino.YamlVariantRules;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

public class SyntacticTermGathererSpec {
	private MemoryTermIndex termIndex;
	private Term machine_synchrone;
	private Term machine_asynchrone;
	private Term synchrone;
	private Term asynchrone;
	private Term stator;
	private Term phase_statorique;
	private Term statorique;
	private Term phase_du_stator;
	private Term geothermie_hydraulique_solaire;
	private Term geothermie_hydraulique;
	
	private AnalysisEngine ae;
	
	@Before
	public void set() throws Exception {
		this.termIndex = Fixtures.termIndex();
		makeAE();
		populateTermIndex(new TermFactory(termIndex));
		ae.collectionProcessComplete();
	}

	private void populateTermIndex(TermFactory termFactory) {
		
		this.machine_synchrone = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.machine_asynchrone = termFactory.create("N:machine|machin", "A:asynchrone|asynchro");
		this.synchrone = termFactory.create("A:synchrone|synchron");
		this.asynchrone = termFactory.create("A:asynchrone|asynchron");

		this.stator = termFactory.create("N:stator|stator");
		this.statorique = termFactory.create("A:statorique|statoric");
		this.phase_statorique = termFactory.create("N:phase|phas", "A:statorique|statoric");
		this.phase_du_stator = termFactory.create("N:phase|phas", "P:de|de", "N:stator|stator");

		this.geothermie_hydraulique_solaire = termFactory.create(
				"N:geothermie|géotherm", "A:hydraulique|hydraulic", "A:solaire|solair");
		this.geothermie_hydraulique = termFactory.create(
				"N:geothermie|géotherm", "A:hydraulique|hydraulic");

		
		termFactory.addPrefix(this.asynchrone, this.synchrone);
		termFactory.addDerivesInto("N A", this.stator, this.statorique);
		termFactory.setProperty(TermProperty.FREQUENCY, 1);
	}

	private void makeAE() throws ResourceInitializationException, InvalidXMLException, ClassNotFoundException {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		manager.clear();
		String historyResourceName = "history";
		TermSuiteResourceManager.getInstance().register(historyResourceName, new TermHistory());
		manager.register(termIndex.getName(), termIndex);
		AnalysisEngineDescription aeDesc = AnalysisEngineFactory.createEngineDescription(
				TermGathererAE.class
			);
		

		/*
		 * The history resource
		 */
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
				TermGathererAE.YAML_VARIANT_RULES,
				YamlVariantRules.class, 
				"file:org/project/ttc/test/resources/variant-rules.yaml"
		);
		ExternalResourceFactory.bindResource(aeDesc, rulesDesc);
		
		ae = AnalysisEngineFactory.createEngine(aeDesc);
	}
	
	@Test
	public void testProcessDefault() throws AnalysisEngineProcessException{
		assertThat(termIndex.getOutboundRelations(this.geothermie_hydraulique))
			.hasSize(1)
			.extracting(TermSuiteExtractors.RELATION_FROM_TYPE_TO)
			.contains(tuple(this.geothermie_hydraulique, RelationType.SYNTACTICAL, this.geothermie_hydraulique_solaire));
		
		assertThat(termIndex.getOutboundRelations(this.geothermie_hydraulique_solaire))
			.hasSize(0);
	}

	
	@Test
	public void testProcessPrefix() throws AnalysisEngineProcessException{
		assertThat(termIndex.getOutboundRelations(this.machine_synchrone))
			.hasSize(1)
			.extracting(TermSuiteExtractors.RELATION_TYPE_RULE_TO)
			.contains(tuple(RelationType.SYNTACTICAL, "NA-NprefA", this.machine_asynchrone));
		
		assertThat(termIndex.getOutboundRelations(this.machine_asynchrone))
			.hasSize(0);
	}

	@Test
	public void testProcessDerivation() throws AnalysisEngineProcessException{
		assertThat(termIndex.getOutboundRelations(this.phase_du_stator))
			.hasSize(1)
			.extracting(TermSuiteExtractors.RELATION_TYPE_RULE_TO)
			.contains(tuple(RelationType.SYNTACTICAL, "S-R2D-NPN", this.phase_statorique));
		assertThat(termIndex.getOutboundRelations(this.phase_statorique))
			.hasSize(0);
		
	}

}
