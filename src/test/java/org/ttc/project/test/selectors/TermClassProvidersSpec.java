package org.ttc.project.test.selectors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.ttc.project.TermFactory;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.models.index.TermValueProvider;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.models.occstore.MemoryOccurrenceStore;

public class TermClassProvidersSpec {
	
	private Term machine_synchrone, synchrone, asynchrone, machine_statorique, machine_de_stator, statorique,
				stator, machine_synchrone_de_stator, hommegrenouille, hommegrenouille_de_stator, term11, aveccapitale;

	private TermIndex termIndex ;
	TermFactory termFactory;
	
	@Before
	public void init() {
		termIndex = new MemoryTermIndex("Test", Lang.FR, new MemoryOccurrenceStore());
		termFactory = new TermFactory(termIndex);
		populateTermIndex();
	}
	
	private void populateTermIndex() {
		
		this.machine_synchrone = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.synchrone = termFactory.create("A:synchrone|synchron");
		this.asynchrone = termFactory.create("A:asynchrone|asynchron");
		this.machine_statorique = termFactory.create("N:machine|machin", "A:statorique|statoric");
		this.machine_de_stator = termFactory.create("N:machine|machin", "P:de|de", "N:stator|stator");
		this.statorique = termFactory.create("A:statorique|statoric");
		this.stator = termFactory.create("N:stator|stator");
		this.machine_synchrone_de_stator = termFactory.create("N:machine|machin", "A:synchrone|synchron", "P:de|de", "N:stator|stator");
		this.hommegrenouille = termFactory.create("N:homme-grenouille|homme-grenouill");
		this.hommegrenouille_de_stator = termFactory.create("N:homme-grenouille|homme-grenouill", "P:de|de", "N:stator|stator");
		this.term11 = termFactory.create("N:machine|machin");
		this.aveccapitale = termFactory.create("N:Aveccapitale|Aveccapital");


		termFactory.wordComposition(CompoundType.NATIVE, "homme-grenouille", "homme|homme", "grenouille|grenouille");
		termFactory.addPrefix(this.asynchrone, this.synchrone);
		termFactory.addDerivesInto("N A", this.stator, this.statorique);
	}

	@Test
	public void testWordLemmaLemmaProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_LEMMA_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(1)
			.contains("machine+synchrone");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(1).contains("machine+statorique");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(1).contains("machine+stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(3).contains("machine+stator", "machine+synchrone", "stator+synchrone");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1)
			.contains("grenouille+homme");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(4).contains(
				"grenouille+homme", 
				"grenouille+stator", 
				"homme+stator", 
				"homme-grenouille+stator");
	}
	
	@Test
	public void testWordLemmaLemmaWithCompoundsProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_LEMMA_PROVIDER;
		Term termWithSize2Comp = termFactory.create("N:xyzt|xyzt");
		termFactory.wordComposition(CompoundType.NATIVE, "xyzt", "xy|xy", "zt|zt");
		Term termWithSize3Comp = termFactory.create("N:abcdef|abcdef");
		termFactory.wordComposition(CompoundType.NATIVE, "abcdef", "ab|ab", "cd|cd", "ef|ef");
		Term simpleTerm = termFactory.create("N:simple|simple");
		Term complexTerm = termFactory.create("N:abcdef|abcdef", "N:simple|simple", "P:de|de", "N:xyzt|xyzt");

		assertThat(provider.getClasses(termIndex, termWithSize2Comp))
			.hasSize(1).contains(
				"xy+zt");

		assertThat(provider.getClasses(termIndex, termWithSize3Comp))
			.hasSize(5).contains(
					"ab+cdef", "abcd+ef",
					"ab+cd", "ab+ef", "cd+ef"
				);

		assertThat(provider.getClasses(termIndex, complexTerm))
			.hasSize(33).contains(
					/* inner classes */
					"ab+cd","ab+ef","ab+cdef",
					"abcd+ef", "cd+ef",
					"xy+zt",
					"ab+simple", "ab+xy", "ab+zt", "ab+xyzt",
					"cd+simple", "cd+xy", "cd+zt", "cd+xyzt",
					"ef+simple", "ef+xy", "ef+zt", "ef+xyzt",
					"abcd+simple", "abcd+xy", "abcd+zt", "abcd+xyzt",
					"cdef+simple", "cdef+xy", "cdef+zt", "cdef+xyzt",
					"abcdef+simple", "abcdef+xy", "abcdef+zt", "abcdef+xyzt",
					"simple+xy", "simple+zt", "simple+xyzt"
				);


		
	}


	@Test
	public void testWordLemmaStemProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_STEM_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(1)
			.contains("machine+synchro");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(1).contains("machine+statoric");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(1).contains("machine+stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(3).contains("machine+stator", "machine+synchro", "stator+synchro");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(1).contains("homme-grenouille+stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(0);
	}

	@Test
	public void testTermLemmaLowerCaseProvider() {
		TermValueProvider provider = TermValueProviders.TERM_LEMMA_LOWER_CASE_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(1)
			.contains("machine synchrone");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(1).contains("machine statorique");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(1).contains("machine de stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(1).contains("machine synchrone de stator");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(1).contains("homme-grenouille de stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(1).contains("aveccapitale");
	}

	@Test
	public void testWordNoClassProvider() {
		TermValueProvider provider = TermValueProviders.TERM_NOCLASS_PROVIDER;
		for(Term t:Lists.newArrayList(machine_synchrone, synchrone, asynchrone, machine_statorique, machine_de_stator, statorique, stator, machine_synchrone_de_stator, hommegrenouille, hommegrenouille_de_stator, term11))
			assertThat(provider.getClasses(termIndex, t))
				.hasSize(1)
				.contains("noclass");
	}

	@Test
	public void testSingleWordLemmaProvider() {
		TermValueProvider provider = TermValueProviders.TERM_SINGLE_WORD_LEMMA_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(1).contains("Aveccapitale");
	}

	@Test
	public void testWordLemmaIfSWTProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_IF_SWT_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(2)
			.contains("machine", "synchrone");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(2).contains("machine", "statorique");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(2).contains("machine", "stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(3).contains("machine", "synchrone", "stator");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(2).contains("homme-grenouille", "stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(1).contains("Aveccapitale");

	}
}
