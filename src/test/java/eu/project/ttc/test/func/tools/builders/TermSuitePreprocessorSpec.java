package eu.project.ttc.test.func.tools.builders;

import static eu.project.ttc.test.TermSuiteAssertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.test.func.FunctionalTests;
import eu.project.ttc.tools.builders.Document;
import eu.project.ttc.tools.builders.TermSuitePreprocessor;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;

public class TermSuitePreprocessorSpec {

	
	Lang lang;
	List<Document> documents;
	Document document1;
	Document document2;
	
	
	@Before
	public void setup() {
		lang = Lang.FR;
		documents = Lists.newArrayList();
		document1 = new Document(lang, "url1", "L'énergie éolienne est l'énergie de demain.");
		documents.add(document1);
		document2 = new Document(lang, "url2", "Une éolienne produit de l'énergie.");
		documents.add(document2);
	}
	
	
	@Test
	public void testFromTxtCorpusExtTxt() {
		Iterator<JCas> it = TermSuitePreprocessor
				.fromTxtCorpus(lang, FunctionalTests.CORPUS1_PATH.toString())
				.setTreeTaggerHome(FunctionalTests.getTaggerPath())
				.stream().iterator();
		
		JCas cas1 = it.next();
		JCas cas2 = it.next();
		assertFalse(it.hasNext());

		Path path1 = Paths.get(
				System.getProperty("user.dir"), 
				FunctionalTests.CORPUS1_PATH.toString(), 
				"file1.txt");
		assertThat(cas1).hasUrl(path1.toString());
		Path path2 = Paths.get(
				System.getProperty("user.dir"), 
				FunctionalTests.CORPUS1_PATH.toString(), 
				"dir1","file3.txt");

		assertThat(cas2).hasUrl(path2.toString());
	}

	@Test
	public void testFromTxtCorpusExtTxtAndDocument() {
		Iterator<JCas> it = TermSuitePreprocessor
				.fromTxtCorpus(
						lang, 
						FunctionalTests.CORPUS1_PATH.toString(),
						"**/*")
				.setTreeTaggerHome(FunctionalTests.getTaggerPath())
				.stream().iterator();
		
		assertAllDocuments(it);
	}

	@Test
	public void testFromTxtCorpusExtWildcard() {
		Iterator<JCas> it = TermSuitePreprocessor
				.fromTxtCorpus(
						lang, 
						FunctionalTests.CORPUS1_PATH.toString(),
						"**/*.{txt,document}")
				.setTreeTaggerHome(FunctionalTests.getTaggerPath())
				.stream().iterator();
		
		assertAllDocuments(it);
	}


	private void assertAllDocuments(Iterator<JCas> it) {
		JCas cas1 = it.next();
		JCas cas2 = it.next();
		JCas cas3 = it.next();
		assertFalse(it.hasNext());

		Path path1 = Paths.get(
				System.getProperty("user.dir"), 
				FunctionalTests.CORPUS1_PATH.toString(), 
				"file2.document");
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


	
	@Test
	public void testPreprocessorFromTextString() {
		
		Iterator<JCas> iterator = TermSuitePreprocessor
			.fromTextString(lang, document1.getText())
			.setTreeTaggerHome(FunctionalTests.getTaggerPath())
			.stream().iterator();
		JCas cas = iterator.next();
	
		assertFalse(iterator.hasNext());

		assertThat(cas)
			.containsAnnotation(TermOccAnnotation.class, 2, 18)
			.containsAnnotation(WordAnnotation.class, 2, 9);
	}
	

	@Test
	public void testPreprocessorFromDocumentStream() {
		Iterator<JCas> it = TermSuitePreprocessor
			.fromDocumentStream(lang, documents.stream())
			.setTreeTaggerHome(FunctionalTests.getTaggerPath())
			.stream().iterator();
		
		JCas cas1 = it.next();
		JCas cas2 = it.next();
		
		assertFalse(it.hasNext());
		
		assertThat(cas1)
			.containsAnnotation(TermOccAnnotation.class, 2, 18)
			.containsAnnotation(WordAnnotation.class, 2, 9);
		assertThat(cas2)
			.containsAnnotation(TermOccAnnotation.class, 13, 33)
			.containsAnnotation(WordAnnotation.class, 4, 12);
	}



}
