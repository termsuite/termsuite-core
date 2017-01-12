
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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.gatherer.GraphicalGatherer;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.metrics.FastDiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.test.unit.Fixtures;
import fr.univnantes.termsuite.test.unit.TermFactory;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

public class GraphicalVariantGathererSpec {
	
	
	private Terminology termino;
	private TerminologyService terminoService;
	private Term tetetete;
	private Term tetetetx;
	private Term teteteteAccent;
	private Term abcdefghijkl;
	private Term abcdefghijkx;
	private Term abcdefghijklCapped;
	
	@Before
	public void setup() {
		this.termino = termino();
	}
	
	
	private Terminology termino() {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		manager.clear();
		Terminology termino = Fixtures.emptyTermino();
		manager.register(termino.getName(), termino);
		TermFactory termFactory = new TermFactory(termino);
		tetetete = termFactory.create("N:tetetete|tetetete");
		tetetete.setSpecificity(1d);
		tetetetx = termFactory.create("N:tetetetx|tetetetx");
		tetetetx.setSpecificity(2d);
		teteteteAccent = termFactory.create("N:tétetete|tétetete");
		teteteteAccent.setSpecificity(3d);
		abcdefghijklCapped = termFactory.create("N:Abcdefghijkl|Abcdefghijkl");
		abcdefghijklCapped.setSpecificity(4d);
		abcdefghijkl = termFactory.create("N:abcdefghijkl|abcdefghijkl");
		abcdefghijkl.setSpecificity(5d);
		abcdefghijkx = termFactory.create("N:abcdefghijkx|abcdefghijkx");
		abcdefghijkx.setSpecificity(6d);
		return termino;
	}


	private GraphicalGatherer makeAE(double similarityThreashhold) throws Exception {
		return new GraphicalGatherer()
				.setDistance(new FastDiacriticInsensitiveLevenshtein(false))
				.setSimilarityThreshold((float)similarityThreashhold);
	}

	@Test
	public void testCaseInsensitive() throws  Exception {
		makeAE( 1.0d).gather(terminoService);
		assertThat(termino.getInboundRelations(this.abcdefghijkl)).hasSize(0)
			.extracting("from");
		assertThat(termino.getInboundRelations(this.abcdefghijklCapped)).hasSize(1)
			.extracting("from")
			.contains(this.abcdefghijkl);
		assertThat(termino.getOutboundRelations(this.abcdefghijkl)).hasSize(1);
		assertThat(termino.getOutboundRelations(this.abcdefghijklCapped)).hasSize(0);
		
		assertThat(termino.getOutboundRelations(this.abcdefghijkl))
			.hasSize(1)
			.extracting("to")
			.contains(this.abcdefghijklCapped);
		assertThat(termino.getInboundRelations(this.abcdefghijkl)).hasSize(0);
	}


	@Test
	public void testWithDiacritics() throws AnalysisEngineProcessException, Exception {
		makeAE( 1.0d).gather(terminoService);
		assertThat(termino.getOutboundRelations(this.tetetete))
			.hasSize(0)
			.extracting(TermSuiteExtractors.VARIATION_TYPE_TO);

		assertThat(termino.getOutboundRelations(this.teteteteAccent))
			.hasSize(1)
			.extracting(TermSuiteExtractors.VARIATION_TYPE_TO)
			.contains(tuple(VariationType.GRAPHICAL, this.tetetete));
}
	
	

	@Test
	public void testWith0_9() throws AnalysisEngineProcessException, Exception {
		makeAE( 0.9d).gather(terminoService);
		assertThat(termino.getOutboundRelations(this.abcdefghijkx))
			.hasSize(2)
			.extracting("to")
			.contains(this.abcdefghijkl, this.abcdefghijklCapped);
		
		assertThat(termino.getOutboundRelations(this.teteteteAccent))
			.hasSize(1)
			.extracting(TermSuiteExtractors.VARIATION_TYPE_TO)
			.contains(
					tuple(VariationType.GRAPHICAL, this.tetetete)
					);
	}

	
	@Test
	public void testWith0_8() throws AnalysisEngineProcessException, Exception {
		makeAE(0.8d).gather(terminoService);
		assertThat(termino.getOutboundRelations(this.abcdefghijklCapped))
			.hasSize(0);
		
		assertThat(termino.getOutboundRelations(this.abcdefghijkl))
			.hasSize(1)
			.extracting("to")
			.contains(this.abcdefghijklCapped);

		assertThat(termino.getOutboundRelations(this.abcdefghijkx))
			.hasSize(2)
			.extracting("to")
			.contains(this.abcdefghijklCapped, this.abcdefghijkl);
	}

}
