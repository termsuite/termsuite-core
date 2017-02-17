package fr.univnantes.termsuite.test.func.io;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.io.IndexedCorpusExporter;
import fr.univnantes.termsuite.io.IndexedCorpusImporter;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class JsonIOReturnSpec {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonIOReturnSpec.class);
	
	private static IndexedCorpus corpus;
	
	@BeforeClass
	public static void runPipeline() throws IOException {
		LOGGER.info("Preprocessing {}", FunctionalTests.getCorpusWE(Lang.FR));
		corpus = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(FunctionalTests.getCorpusWE(Lang.FR), 5000000);
		
		
		LOGGER.info("Extracting terminology for {}", FunctionalTests.getCorpusWE(Lang.FR));
		TermSuite.terminoExtractor().execute(corpus);
		
		LOGGER.info("Terminology extracted.");
		
		logTerminology();
	}

	private static void logTerminology() {
		LOGGER.info("Nb words: {}", corpus.getTerminology().getWords().size());
		LOGGER.info("Nb terms: {}", corpus.getTerminology().getTerms().size());
		LOGGER.info("Nb relations: {}", corpus.getTerminology().getRelations().size());
		LOGGER.info("Nb compound words: {}", corpus.getTerminology().getWords().values().stream().filter(Word::isCompound).count());
		
		
		for(RelationType rType:RelationType.values())
			LOGGER.info("Nb relations of type {}: {}", rType, corpus.getTerminology().getRelations().stream().filter(r->r.getType()==rType).count());

		
		Set<Relation> variations = corpus.getTerminology().getRelations().stream().filter(r->r.getType()==RelationType.VARIATION).collect(toSet());
		for(VariationType vType:VariationType.values())
			LOGGER.info("Nb variations of type {}: {}", vType, variations.stream()
					.filter(r->r.isPropertySet(vType.getRelationProperty()))
					.filter(r->r.getBoolean(vType.getRelationProperty()))
					.count()
				);
	}
	
	@Test
	public void testJsonReturnBeforeExtraction() throws IOException {
		IndexedCorpusExporter exporter = TermSuiteFactory.createJsonExporter();
		
		String json = exporter.exportToString(corpus);

		IndexedCorpusImporter importer = TermSuiteFactory.createJsonLoader();
		IndexedCorpus corpus2 = importer.loadFromString(json);
	
		Terminology termino2 = corpus2.getTerminology();
		Terminology termino = corpus.getTerminology();

		assertTerminologyProperties(termino2, termino);
		assertWordSet(termino2, termino);
		assertTermSet(termino2, termino);
		assertRelationSet(termino2, termino);
	}
	
	public void assertRelationSet(Terminology termino2, Terminology termino) {
		/*
		 * 1
		 */
		Set<Relation> relationsInTermino1NotInTermino2 = Sets.newHashSet(termino.getRelations());
		relationsInTermino1NotInTermino2.removeAll(termino2.getRelations());
		assertThat(relationsInTermino1NotInTermino2).isEmpty();
		
		/*
		 * 2
		 */
		Set<Relation> relationsInTermino2NotInTermino1 = Sets.newHashSet(termino2.getRelations());
		relationsInTermino2NotInTermino1.removeAll(termino.getRelations());
		assertThat(relationsInTermino2NotInTermino1).isEmpty();

		/*
		 * 3
		 */
		Set<Relation> relationIntersection = Sets.newHashSet(termino.getRelations());
		relationIntersection.retainAll(termino2.getRelations());
		assertThat(relationIntersection)
			.hasSize(termino.getRelations().size())
			.containsAll(termino.getRelations());
		
		/*
		 * 4
		 */
		for(Relation rel1:termino.getRelations()) {
			Optional<Relation> rel2 = termino.getRelations().stream().filter(r2-> r2.equals(rel1)).findAny();
			assertTrue("Expected to find relation " + rel1 + " in termino2", rel2.isPresent());
			assertEquals("Expected same relation properties", rel1.getProperties(), rel2.get().getProperties());
		}

	}

	public void assertTerminologyProperties(Terminology termino2, Terminology termino) {
		assertEquals("Expected same number of spotted terms", termino.getNbSpottedTerms().longValue(), termino2.getNbSpottedTerms().longValue());
		assertEquals("Expected same number of word annotations", termino.getNbWordAnnotations().longValue(), termino2.getNbWordAnnotations().longValue());
		assertEquals("Expected same name", termino.getName(), termino2.getName());
		assertEquals("Expected same corpus id", termino.getCorpusId(), termino2.getCorpusId());
		assertEquals("Expected same lang", termino.getLang(), termino2.getLang());
	}

	public void assertTermSet(Terminology termino2, Terminology termino) {
		/*
		 * 1
		 */
		Set<String> termsInTermino1NotInTermino2 = Sets.newHashSet(termino.getTerms().keySet());
		termsInTermino1NotInTermino2.removeAll(termino2.getTerms().keySet());
		assertThat(termsInTermino1NotInTermino2).isEmpty();
		
		/*
		 * 2
		 */
		Set<String> termsInTermino2NotInTermino1 = Sets.newHashSet(termino2.getTerms().keySet());
		termsInTermino2NotInTermino1.removeAll(termino.getTerms().keySet());
		assertThat(termsInTermino2NotInTermino1).isEmpty();

		/*
		 * 3
		 */
		Set<String> termsIntersectionWords = Sets.newHashSet(termino.getTerms().keySet());
		termsIntersectionWords.retainAll(termino2.getTerms().keySet());
		assertThat(termsIntersectionWords)
			.hasSize(termino.getTerms().size())
			.containsAll(termino.getTerms().keySet());
		
		/*
		 * 4
		 */
		
		for(String gKey:termino.getTerms().keySet()) {
			assertTermEquals(termino.getTerms().get(gKey), termino2.getTerms().get(gKey));
		}
	}
	
	private void assertTermEquals(Term t1, Term t2) {
		assertEquals("Expected same properties", t1.getProperties(), t2.getProperties());
		assertEquals("Expected same term size", t1.getWords().size(), t2.getWords().size());
		for(int i=0; i<t1.getWords().size();i++) {
			assertEquals("Expected same syntactic labels", t1.getWords().get(i).getSyntacticLabel(), t2.getWords().get(i).getSyntacticLabel());
			assertEquals("Expected same word lemmas", t1.getWords().get(i).getWord().getLemma(), t2.getWords().get(i).getWord().getLemma());
		}
	}

	
	public void assertWordSet(Terminology termino2, Terminology termino) {
		/*
		 * 1
		 */
		Set<String> wordsInTermino1NotInTermino2 = Sets.newHashSet(termino.getWords().keySet());
		wordsInTermino1NotInTermino2.removeAll(termino2.getWords().keySet());
		assertThat(wordsInTermino1NotInTermino2).isEmpty();
		
		/*
		 * 2
		 */
		Set<String> wordsInTermino2NotInTermino1 = Sets.newHashSet(termino2.getWords().keySet());
		wordsInTermino2NotInTermino1.removeAll(termino.getWords().keySet());
		assertThat(wordsInTermino2NotInTermino1).isEmpty();

		/*
		 * 3
		 */
		Set<String> wordsIntersectionWords = Sets.newHashSet(termino.getWords().keySet());
		wordsIntersectionWords.retainAll(termino2.getWords().keySet());
		assertThat(wordsIntersectionWords)
			.hasSize(termino.getWords().size())
			.containsAll(termino.getWords().keySet());


		/*
		 * 4
		 */
		for(String lemma:termino.getWords().keySet())
			assertWordEquals(termino.getWords().get(lemma), termino2.getWords().get(lemma));
	}

	private void assertWordEquals(Word word, Word word2) {
		assertEquals("Expected same lemma", word.getLemma(), word2.getLemma());
		assertEquals("Expected same stem", word.getStem(), word2.getStem());

		assertEquals("Expected same compound type", word.getCompoundType(), word2.getCompoundType());
		
		assertEquals("Expected same components size", word.getComponents().size(), word2.getComponents().size());
		
		if(word.isCompound())
			assertTrue(word2.isCompound());
		if(word.getCompoundType() == CompoundType.NEOCLASSICAL) 
			assertEquals("Expected same components size", word.getNeoclassicalAffix(), word2.getNeoclassicalAffix());
			
		
		for(int i=0;i<word.getComponents().size(); i++) 
			assertComponentEquals(word.getComponents().get(i), word2.getComponents().get(i));
		
	}

	private void assertComponentEquals(Component c, Component c2) {
		assertEquals("Expected same begin", c.getBegin(), c2.getBegin());
		assertEquals("Expected same end", c.getEnd(), c2.getEnd());
		assertEquals("Expected same lemma", c.getLemma(), c2.getLemma());
		assertEquals("Expected same substring", c.getSubstring(), c2.getSubstring());
	}
	

}
