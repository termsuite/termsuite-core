package fr.univnantes.termsuite.test.func.api;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.univnantes.termsuite.api.Preprocessor;
import fr.univnantes.termsuite.api.TXTCorpus;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.test.func.FunctionalTests;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.types.WordAnnotation;

public class PreprocessorSpec {
	
	@Rule 
	public TemporaryFolder folder = new TemporaryFolder();

	TXTCorpus corpus;
	
	
	@Test
	public void testToIndexedCorpusOnTXTCorpus() {
		IndexedCorpus indexedCorpus = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(FunctionalTests.CORPUS1, 500000);

		assertThat(indexedCorpus.getTerminology())
			.containsTerm("n: énergie");
	}
	
	
	@Test
	public void testToIndexedCorpusOnBlankCasStream() {
		Stream<JCas> casStream = FunctionalTests.CORPUS1
				.documents()
				.map(doc -> Preprocessor.toCas(
						doc, 
						FunctionalTests.CORPUS1.readDocumentText(doc)));
		Lang lang = FunctionalTests.CORPUS1.getLang();
		
		IndexedCorpus indexedCorpus = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(lang, casStream, 500000);

		assertTrue(indexedCorpus.getTerminology().getTerms().size() > 0);
		
		assertThat(indexedCorpus.getTerminology())
			.containsTerm("n: énergie");
	}

	
	@Before
	public void setup() {
		corpus = new TXTCorpus(Lang.FR, FunctionalTests.CORPUS1_PATH);
	}
	
	@Test
	public void testJSONOnCorpus1() {
		TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.exportAnnotationsToJSON(folder.getRoot().toPath())
			.run(corpus);
		
		assertThat(Paths.get(folder.getRoot().getAbsolutePath(), "file1.json").toFile()).exists();
		assertThat(Paths.get(folder.getRoot().getAbsolutePath(), "file2.json").toFile()).exists();
		assertThat(Paths.get(folder.getRoot().getAbsolutePath(), "file3.json").toFile()).exists();
	}
	
	@Test
	public void testXMIOnCorpus1() {
		TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.exportAnnotationsToXMI(folder.getRoot().toPath())
			.run(corpus);
		
		assertThat(Paths.get(folder.getRoot().getAbsolutePath(), "file1.xmi").toFile()).exists();
		assertThat(Paths.get(folder.getRoot().getAbsolutePath(), "file2.xmi").toFile()).exists();
		assertThat(Paths.get(folder.getRoot().getAbsolutePath(), "file3.xmi").toFile()).exists();
	}

	@Test
	public void testStreamOnCorpus1() {
		Stream<JCas> stream = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.asStream(corpus);
		
		assertAllDocuments(stream);
	}

	@Test
	public void testTerminoOnCorpus1() {
		IndexedCorpus ic = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.toIndexedCorpus(corpus, 500000);
		
		assertTermino(ic.getTerminology());

	}

	@Test
	public void testParallelTerminoOnCorpus1() {
		IntStream.range(0, 1).forEach(i -> {
			setup();
			IndexedCorpus ic = TermSuite.preprocessor()
					.setTaggerPath(FunctionalTests.getTaggerPath())
					.toIndexedCorpus(corpus, 500000);
			assertTermino(ic.getTerminology());
		});
	}

	private void assertTermino(Terminology termino) {
		assertThat(termino)
				.containsTerm("n: éolienne", 1)
				.containsTerm("a: éolien", 2)
				.containsTerm("n: énergie", 5)
				.containsTerm("na: énergie éolien", 2)
				.containsTerm("npn: énergie de demain", 1)
				.containsTerm("npn: énergie du futur", 1)
				.containsTerm("n: futur", 1)
				.containsTerm("n: demain", 1)
				.hasNTerms(8);

		assertThat(termino.getWords().values())
				.extracting("lemma")
				.containsOnly("éolienne", "éolien", "énergie", "demain", "futur", "de", "du")
				.hasSize(7);
	}

		
	private void assertAllDocuments(Stream<JCas> stream) {
		Iterator<JCas> it = stream.iterator();
		JCas cas1 = it.next();
		
		assertThat(cas1)
			.urlEndsWith("file2.txt")
			.containsText("Une éolienne donne de l'énergie.")
			.containsAnnotation(TermOccAnnotation.class, 4, 12)
			.containsAnnotation(TermOccAnnotation.class, 24, 31)
			.containsAnnotation(WordAnnotation.class, 4, 12)
			.containsAnnotation(WordAnnotation.class, 24, 31)
			;
		
		JCas cas2 = it.next();
		
		assertThat(cas2)
			.urlEndsWith("file1.txt")
			.containsText("L'énergie éolienne est l'énergie de demain.")
			.containsAnnotation(TermOccAnnotation.class, 2, 18)
			.containsAnnotation(WordAnnotation.class, 2, 9)
			.containsAnnotation(WordAnnotation.class, 10, 18)
			;

		JCas cas3 = it.next();
		assertThat(cas3)
			.urlEndsWith("dir1/file3.txt")
			.containsText("L'énergie du futur sera l'énergie éolienne.")
			.containsAnnotation(TermOccAnnotation.class, 2, 18)
			.containsAnnotation(WordAnnotation.class, 2, 9)
			.containsAnnotation(WordAnnotation.class, 13, 18)
			;

		assertFalse(it.hasNext());

		Path path1 = Paths.get(
				System.getProperty("user.dir"), 
				FunctionalTests.CORPUS1_PATH.toString(), 
				"file2.txt");
		assertThat(cas1).hasUrl(path1.toString());
		Path path2 = Paths.get(
				System.getProperty("user.dir"), 
				FunctionalTests.CORPUS1_PATH.toString(), 
				"file1.txt");
		assertThat(cas2).hasUrl(path2.toString());
		Path path3 = Paths.get(
				System.getProperty("user.dir"), 
				FunctionalTests.CORPUS1_PATH.toString(), 
				"dir1","file3.txt");
		assertThat(cas3).hasUrl(path3.toString());
	}
}
