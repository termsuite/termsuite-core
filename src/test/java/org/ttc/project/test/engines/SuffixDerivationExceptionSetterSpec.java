package org.ttc.project.test.engines;

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
import org.ttc.project.Fixtures;
import org.ttc.project.TermFactory;

import eu.project.ttc.engines.morpho.SuffixDerivationExceptionSetter;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.resources.TermIndexResource;
import fr.univnantes.julestar.uima.resources.MultimapFlatResource;

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
		MemoryTermIndexManager manager = MemoryTermIndexManager.getInstance();
		manager.clear();
		manager.register(termIndex);
		AnalysisEngineDescription aeDesc = AnalysisEngineFactory.createEngineDescription(
				SuffixDerivationExceptionSetter.class
			);
		

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
				"file:org/project/ttc/test/resources/suffix-derivation-exceptions.txt"
		);
		ExternalResourceFactory.bindResource(aeDesc, rulesDesc);
		
		ae = AnalysisEngineFactory.createEngine(aeDesc);
	}
	

	@Test
	public void testProcessCollectionComplete() {
		assertThat(this.pays.getVariations())
			.hasSize(1)
			.extracting("variationType", "variant")
			.contains(tuple(VariationType.DERIVES_INTO, paysVraiDerive));
		assertThat(this.ferme_a.getVariations()).hasSize(0);
	}
}
