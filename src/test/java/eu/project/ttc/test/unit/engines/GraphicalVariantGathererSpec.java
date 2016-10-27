
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
import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.engines.GraphicalVariantGatherer;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.history.TermHistory;
import eu.project.ttc.history.TermHistoryResource;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.test.unit.Fixtures;
import eu.project.ttc.test.unit.TermFactory;
import eu.project.ttc.tools.TermSuiteResourceManager;

public class GraphicalVariantGathererSpec {
	
	
	private TermIndex termIndex;
	private Term tetetete;
	private Term tetetetx;
	private Term teteteteAccent;
	private Term abcdefghijkl;
	private Term abcdefghijkx;
	private Term abcdefghijklCapped;
	
	@Before
	public void setup() {
		this.termIndex = termIndex();
	}
	
	
	private TermIndex termIndex() {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		manager.clear();
		TermIndex termIndex = Fixtures.emptyTermIndex();
		manager.register(termIndex.getName(), termIndex);
		TermFactory termFactory = new TermFactory(termIndex);
		tetetete = termFactory.create("N:tetetete|tetetete");
		tetetetx = termFactory.create("N:tetetetx|tetetetx");
		teteteteAccent = termFactory.create("N:tétetete|tétetete");
		abcdefghijklCapped = termFactory.create("N:Abcdefghijkl|Abcdefghijkl");
		abcdefghijkl = termFactory.create("N:abcdefghijkl|abcdefghijkl");
		abcdefghijkx = termFactory.create("N:abcdefghijkx|abcdefghijkx");
		return termIndex;
	}


	private AnalysisEngine makeAE(Lang lang, float similarityThreashhold) throws Exception {
		TermSuiteResourceManager.getInstance().clear();

		AnalysisEngineDescription aeDesc = AnalysisEngineFactory.createEngineDescription(
				GraphicalVariantGatherer.class,
				GraphicalVariantGatherer.LANG, lang.getCode(),
				GraphicalVariantGatherer.SIMILARITY_THRESHOLD, similarityThreashhold
			);
		
		/*
		 * The history resource
		 */
		String  historyResourceName = "Toto";
		TermSuiteResourceManager.getInstance().register(historyResourceName, new TermHistory());
		ExternalResourceDescription historyResourceDesc = ExternalResourceFactory.createExternalResourceDescription(
				TermHistoryResource.TERM_HISTORY,
				TermHistoryResource.class, 
				historyResourceName
		);
		ExternalResourceFactory.bindResource(aeDesc, historyResourceDesc);

		
		/*
		 * The term index resource
		 */
		TermSuiteResourceManager.getInstance().register(this.termIndex.getName(), this.termIndex);
		ExternalResourceDescription termIndexDesc = ExternalResourceFactory.createExternalResourceDescription(
				TermIndexResource.TERM_INDEX,
				TermIndexResource.class, 
				this.termIndex.getName()
		);
		ExternalResourceFactory.bindResource(aeDesc, termIndexDesc);

		AnalysisEngine ae = AnalysisEngineFactory.createEngine(aeDesc);
		return ae;
	}

	@Test
	public void testCaseInsensitive() throws  Exception {
		makeAE(Lang.FR, 1.0f).collectionProcessComplete();
		assertThat(termIndex.getInboundTerRelat(this.abcdefghijkl)).hasSize(1)
		.extracting("base")
		.contains(this.abcdefghijklCapped);
		assertThat(termIndex.getOutboundRelations(this.abcdefghijkl)).hasSize(1);
		
		assertThat(termIndex.getOutboundRelations(this.abcdefghijklCapped))
			.hasSize(1)
			.extracting("variant")
			.contains(this.abcdefghijkl);
		assertThat(termIndex.getInboundTerRelat(this.abcdefghijklCapped)).hasSize(1);
	}


	@Test
	public void testWithDiacritics() throws AnalysisEngineProcessException, Exception {
		makeAE(Lang.FR, 1.0f).collectionProcessComplete();
		assertThat(termIndex.getOutboundRelations(this.tetetete))
			.hasSize(1)
			.extracting("variationType", "variant")
			.contains(tuple(RelationType.GRAPHICAL, this.teteteteAccent));
	}

	@Test
	public void testWith0_9() throws AnalysisEngineProcessException, Exception {
		makeAE(Lang.FR, 0.9f).collectionProcessComplete();
		assertThat(termIndex.getOutboundRelations(this.abcdefghijklCapped))
			.hasSize(2)
			.extracting("variant")
			.contains(this.abcdefghijkl, this.abcdefghijkx);
		
		assertThat(termIndex.getOutboundRelations(this.tetetete))
			.hasSize(1)
			.extracting("variationType", "variant")
			.contains(
					tuple(RelationType.GRAPHICAL, this.teteteteAccent)
					);
	}

	
	@Test
	public void testWith0_8() throws AnalysisEngineProcessException, Exception {
		makeAE(Lang.FR, 0.8f).collectionProcessComplete();
		assertThat(termIndex.getOutboundRelations(this.abcdefghijklCapped))
			.hasSize(2)
			.extracting("variant")
			.contains(this.abcdefghijkl, this.abcdefghijkx);
		
		assertThat(termIndex.getOutboundRelations(this.tetetete))
			.hasSize(2)
			.extracting("variationType", "variant")
			.contains(
					tuple(RelationType.GRAPHICAL, this.teteteteAccent),
					tuple(RelationType.GRAPHICAL, this.tetetetx)
					);

	}

}
