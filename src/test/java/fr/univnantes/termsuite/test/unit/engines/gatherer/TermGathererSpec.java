
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

package fr.univnantes.termsuite.test.unit.engines.gatherer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.gatherer.GathererOptions;
import fr.univnantes.termsuite.engines.gatherer.TermGatherer;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSet;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSetIO;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.unit.TermFactory;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.test.unit.UnitTests;
import fr.univnantes.termsuite.uima.ResourceType;

public class TermGathererSpec {
	private Terminology termino;
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
	
	private EngineRunner gatherer;
	private GathererOptions options;
	
	private static final String VARIANT_RULE_SET = "src/test/resources/fr/univnantes/termsuite/test/resources/variant-rules.yaml";
	@Before
	public void set() throws Exception {
		options = TermSuite.getDefaultExtractorConfig(Lang.EN).getGathererConfig();
		String text = Files.toString(new File(VARIANT_RULE_SET), Charsets.UTF_8);
		IndexedCorpus corpus = TermSuiteFactory.createIndexedCorpus(Lang.FR, "");
		this.termino = corpus.getTerminology();
		populateTermino(new TermFactory(termino));
		YamlRuleSet yamlRuleSet = YamlRuleSetIO.fromYaml(text);
		gatherer = UnitTests.createEngineRunner(
				corpus, 
				TermGatherer.class, 
				UnitTests.mockResourceModule().bind(ResourceType.VARIANTS, yamlRuleSet),
				options);
		gatherer.configure();
		gatherer.run();
	}

	private void populateTermino(TermFactory termFactory) {
		
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

	
	@Test
	public void testProcessDefault() throws AnalysisEngineProcessException{
		assertThat(termino.getOutboundRelations().get(this.geothermie_hydraulique))
			.hasSize(1)
			.extracting(TermSuiteExtractors.VARIATION_FROM_TYPE_TO)
			.contains(tuple(this.geothermie_hydraulique, VariationType.SYNTAGMATIC, this.geothermie_hydraulique_solaire));
		
		assertThat(termino.getOutboundRelations().get(this.geothermie_hydraulique_solaire))
			.hasSize(0);
	}

	
	@Test
	public void testProcessPrefix() throws AnalysisEngineProcessException{
		assertThat(termino.getOutboundRelations().get(this.machine_synchrone))
			.hasSize(1)
			.extracting(TermSuiteExtractors.VARIATION_TYPE_RULE_TO)
			.contains(tuple(VariationType.PREFIXATION, "NA-NprefA", this.machine_asynchrone));
		
		assertThat(termino.getOutboundRelations().get(this.machine_asynchrone))
			.hasSize(0);
	}

	@Test
	public void testProcessDerivation() throws AnalysisEngineProcessException{
		assertThat(termino.getOutboundRelations().get(this.phase_du_stator))
			.hasSize(1)
			.extracting(TermSuiteExtractors.VARIATION_TYPE_RULE_TO)
			.contains(tuple(VariationType.DERIVATION, "S-R2D-NPN", this.phase_statorique));
		assertThat(termino.getOutboundRelations().get(this.phase_statorique))
			.hasSize(0);
		
	}

}
