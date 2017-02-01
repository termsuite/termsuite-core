
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

package fr.univnantes.termsuite.test.unit.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.index.providers.EqualityIndicesProvider;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.unit.TermFactory;

public class TermValueProvidersSpec {
	
	private Term machine_synchrone, synchrone, asynchrone, machine_statorique, machine_de_stator, statorique,
				stator, machine_synchrone_de_stator, hommegrenouille, hommegrenouille_de_stator, aveccapitale;

	private Terminology termino ;
	
	@Before
	public void init() {
		termino = new Terminology("Test", Lang.FR);
		populateTermino(new TermFactory(termino));
	}
	
	private void populateTermino(TermFactory termFactory) {
		
		this.machine_synchrone = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.synchrone = termFactory.create("A:synchrone|synchron");
		this.asynchrone = termFactory.create("A:asynchrone|asynchron");
		this.machine_statorique = termFactory.create("N:machine|machin", "A:statorique|statoric");
		this.machine_de_stator = termFactory.create("N:machine|machin", "P:de|de", "N:stator|stator");
		this.statorique = termFactory.create("A:statorique|statoric");
		this.stator = termFactory.create("N:stator|stator");
		this.machine_synchrone_de_stator = termFactory.create("N:machine|machin", "A:synchrone|synchron", "P:de|de", "N:stator|stator");
		this.hommegrenouille = termFactory.create("N:homme-grenouille|homme-grenouille");
		this.hommegrenouille_de_stator = termFactory.create("N:homme-grenouille|homme-grenouille", "P:de|de", "N:stator|stator");
		this.aveccapitale = termFactory.create("N:Aveccapitale|Aveccapital");
		termFactory.wordComposition(CompoundType.NATIVE, "homme-grenouille", "homme|homme", "grenouille|grenouille");
		termFactory.addPrefix(this.asynchrone, this.synchrone);
		termFactory.addDerivesInto("N A", this.stator, this.statorique);
	}

	
	@Test
	public void testWordHorizontalAxis() throws InstantiationException, IllegalAccessException {
		TermFactory fac = new TermFactory(termino);
		Term horizontalAxisMorp = fac.create("N:horizontal-axis|horizontal-axis");
		Term horizontalAxisSyntag = fac.create("A:horizontal|horizontal", "N:axis|axis");
		fac.wordComposition(CompoundType.NATIVE, "horizontal-axis", "horizon|horizon", "tal|t", "axis|axis");
		fac.wordComposition(CompoundType.NATIVE, "horizontal", "horizon|horizon", "tal|t");
		TermIndexValueProvider provider = TermIndexType.ALLCOMP_PAIRS.getProviderClass().newInstance();

		assertThat(provider.getClasses(horizontalAxisMorp))
			.hasSize(8)
			.contains("axis+horizontal");
		
		assertThat(provider.getClasses(horizontalAxisSyntag))
			.hasSize(7)
			.contains("horizon+t")
			.contains("horizon+tal")
			.contains("axis+horizontal")
			.contains("axis+horizont")
			.contains("axis+horizon")
			.contains("axis+t")
			.contains("axis+tal")
		;
	}

	@Test
	public void testEqualityIndicesProvider() throws InstantiationException, IllegalAccessException {
		LinkedList<Integer> newLinkedList = Lists.newLinkedList();
		newLinkedList.add(0);
		TermIndexValueProvider provider = new EqualityIndicesProvider(newLinkedList);

		assertThat(provider.getClasses(stator))
			.containsOnly("stator");
		assertThat(provider.getClasses(machine_de_stator))
			.containsOnly("machine");
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.containsOnly("machine");
		
		newLinkedList.add(1);

		assertThat(provider.getClasses(stator))
			.isEmpty();
		assertThat(provider.getClasses(machine_de_stator))
			.containsOnly("machine:de");
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.containsOnly("machine:synchrone");

	}

	
	@Test
	public void testAllCompLemmaSubstringPairsProvider() throws InstantiationException, IllegalAccessException {
		TermIndexValueProvider provider = TermIndexType.ALLCOMP_PAIRS.getProviderClass().newInstance();
		assertThat(provider.getClasses(machine_synchrone))
			.hasSize(1)
			.contains("machine+synchrone");
		assertThat(provider.getClasses(synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(asynchrone))
			.hasSize(0);
		assertThat(provider.getClasses(machine_statorique))
			.hasSize(1).contains("machine+statorique");
		assertThat(provider.getClasses(machine_de_stator))
			.hasSize(1).contains("machine+stator");
		assertThat(provider.getClasses(statorique))
			.hasSize(0);
		assertThat(provider.getClasses(stator))
			.hasSize(0);
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.hasSize(3).contains("machine+stator", "machine+synchrone", "stator+synchrone");
		assertThat(provider.getClasses(hommegrenouille))
			.hasSize(1).contains("grenouille+homme");
		assertThat(provider.getClasses(hommegrenouille_de_stator))
			.hasSize(4).contains(
					"homme-grenouille+stator", "grenouille+homme", "grenouille+stator", "homme+stator");
		assertThat(provider.getClasses(aveccapitale))
			.hasSize(0);
	}

	@Test
	public void testWordLemmaStemProvider() throws InstantiationException, IllegalAccessException {
		TermIndexValueProvider provider = TermIndexType.WORD_COUPLE_LEMMA_STEM.getProviderClass().newInstance();
		assertThat(provider.getClasses(machine_synchrone))
			.hasSize(1)
			.contains("machine+synchro");
		assertThat(provider.getClasses(synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(asynchrone))
			.hasSize(0);
		assertThat(provider.getClasses(machine_statorique))
			.hasSize(1).contains("machine+statoric");
		assertThat(provider.getClasses(machine_de_stator))
			.hasSize(1).contains("machine+stator");
		assertThat(provider.getClasses(statorique))
			.hasSize(0);
		assertThat(provider.getClasses(stator))
			.hasSize(0);
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.hasSize(3).contains("machine+stator", "machine+synchro", "stator+synchro");
		assertThat(provider.getClasses(hommegrenouille))
			.hasSize(0);
		assertThat(provider.getClasses(hommegrenouille_de_stator))
			.hasSize(1).contains("homme-grenouille+stator");
		assertThat(provider.getClasses(aveccapitale))
			.hasSize(0);
	}

	@Test
	public void testTermLemmaLowerCaseProvider() throws InstantiationException, IllegalAccessException {
		TermIndexValueProvider provider = TermIndexType.LEMMA_LOWER_CASE.getProviderClass().newInstance();
		assertThat(provider.getClasses(machine_synchrone))
			.hasSize(1)
			.contains("machine synchrone");
		assertThat(provider.getClasses(synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(machine_statorique))
			.hasSize(1).contains("machine statorique");
		assertThat(provider.getClasses(machine_de_stator))
			.hasSize(1).contains("machine de stator");
		assertThat(provider.getClasses(statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.hasSize(1).contains("machine synchrone de stator");
		assertThat(provider.getClasses(hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(hommegrenouille_de_stator))
			.hasSize(1).contains("homme-grenouille de stator");
		assertThat(provider.getClasses(aveccapitale))
			.hasSize(1).contains("aveccapitale");
	}

	@Test
	public void testSingleWordLemmaProvider() throws InstantiationException, IllegalAccessException {
		TermIndexValueProvider provider = TermIndexType.SWT_LEMMAS.getProviderClass().newInstance();
		assertThat(provider.getClasses(machine_synchrone))
			.hasSize(2)
			.contains("machine", "synchrone");
		assertThat(provider.getClasses(synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(machine_statorique))
			.hasSize(2).contains("machine", "statorique");
		assertThat(provider.getClasses(machine_de_stator))
			.hasSize(2).contains("machine",  "stator");
		assertThat(provider.getClasses(statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.hasSize(3).contains("machine", "synchrone", "stator");
		assertThat(provider.getClasses(hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(hommegrenouille_de_stator))
			.hasSize(2).contains("homme-grenouille", "stator");
		assertThat(provider.getClasses(aveccapitale))
			.hasSize(1).contains("aveccapitale");
	}
	
	
	@Test
	public void testSingleWordLemmaSwtOnlyProvider() throws InstantiationException, IllegalAccessException {
		TermIndexValueProvider provider = TermIndexType.SWT_LEMMAS_SWT_TERMS_ONLY.getProviderClass().newInstance();
		assertThat(provider.getClasses(machine_synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(machine_statorique))
			.hasSize(0);
		assertThat(provider.getClasses(machine_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(hommegrenouille_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(aveccapitale))
			.hasSize(1).contains("aveccapitale");
	}


	@Test
	public void testWordLemmaProvider() throws InstantiationException, IllegalAccessException {
		TermIndexValueProvider provider = TermIndexType.WORD_LEMMAS.getProviderClass().newInstance();
		assertThat(provider.getClasses(machine_synchrone))
			.hasSize(2)
			.contains("machine", "synchrone");
		assertThat(provider.getClasses(synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(machine_statorique))
			.hasSize(2).contains("machine", "statorique");
		assertThat(provider.getClasses(machine_de_stator))
			.hasSize(3).contains("machine",  "de", "stator");
		assertThat(provider.getClasses(statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(machine_synchrone_de_stator))
			.hasSize(4).contains("machine", "synchrone", "stator");
		assertThat(provider.getClasses(hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(hommegrenouille_de_stator))
			.hasSize(3).contains("homme-grenouille", "stator");
		assertThat(provider.getClasses(aveccapitale))
			.hasSize(1).contains("aveccapitale");
	}
	
}
