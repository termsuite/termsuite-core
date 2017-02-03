package fr.univnantes.termsuite.test.unit.engines.gatherer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Multimap;

import fr.univnantes.termsuite.engines.gatherer.DerivationGatherer;
import fr.univnantes.termsuite.engines.gatherer.PrefixationGatherer;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.unit.TermFactory;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class RelationPairsBasedGathererSpec {
	private Term synchrone, asynchrone, machine_synchrone_de_stator, machine_synchrone, machine_statorique, machine_de_stator, statorique, stator;

	private TerminologyService terminology;
	private PrefixationGatherer prefixationAe;
	private DerivationGatherer derivationAe;

	@Before
	public void setup() throws InstantiationException, IllegalAccessException {
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(Lang.FR, "");
		Terminology termino = indexedCorpus.getTerminology();
		TermFactory termFactory = new TermFactory(termino);
		terminology = UnitTests.getTerminologyService(indexedCorpus);
		
		this.statorique = termFactory.create("A:statorique|statoric");
		this.stator = termFactory.create("N:stator|stator");
		this.machine_synchrone = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.synchrone = termFactory.create("A:synchrone|synchron");
		this.asynchrone = termFactory.create("A:asynchrone|asynchron");
		this.machine_statorique = termFactory.create("N:machine|machin", "A:statorique|statoric");
		this.machine_synchrone_de_stator = termFactory.create("N:machine|machin", "A:synchrone|synchron", "P:de|de", "N:stator|stator");
		this.machine_de_stator = termFactory.create("N:machine|machin", "P:de|de", "N:stator|stator");
		termFactory.addPrefix(this.asynchrone, this.synchrone);
		termFactory.addDerivesInto("N A", this.stator, this.statorique);

		prefixationAe = new PrefixationGatherer();
		UnitTests.injectIndexes(prefixationAe, "terminology", termino);
		UnitTests.setField(prefixationAe, "terminology", terminology);

		derivationAe = new DerivationGatherer();
		UnitTests.injectIndexes(derivationAe, "terminology", termino);
		UnitTests.setField(derivationAe, "terminology", terminology);

	}
	
	@Test
	public void testDerivationKeying() throws Exception {
		Multimap<String, Term> classes = derivationAe.getRelationIndex();
		assertThat(classes.get("stator+statorique"))
			.contains(this.machine_de_stator)
			.contains(this.machine_statorique)
			.contains(this.machine_synchrone_de_stator)
			.contains(this.stator)
			.contains(this.statorique)
			.hasSize(5);
		assertThat(classes.keySet()).hasSize(1).contains("stator+statorique");
	}


	@Test
	public void testPrefixationKeying() throws Exception {
		Multimap<String, Term> classes = prefixationAe.getRelationIndex();

		assertThat(classes.get("asynchrone+synchrone"))
			.contains(machine_synchrone)
			.contains(machine_synchrone_de_stator)
			.contains(asynchrone)
			.contains(synchrone)
			.hasSize(4);
			
		assertThat(classes.keySet()).hasSize(1).contains("asynchrone+synchrone");
	}
}
