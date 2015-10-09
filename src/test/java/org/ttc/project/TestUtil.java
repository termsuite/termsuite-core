package org.ttc.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.mockito.Mockito;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.common.collect.ObjectArrays;

import eu.project.ttc.models.TermIndex;
import eu.project.ttc.readers.TeiToTxtSaxHandler;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.types.TermOccAnnotation;
import fr.univnantes.lina.uima.tkregex.RegexOccurrence;

public class TestUtil {

	public static RegexOccurrence createOccurrence(int begin, int end, String lemma) {
		RegexOccurrence o = Mockito.mock(RegexOccurrence.class);
		Mockito.when(o.toString()).thenReturn(String.format("%s[%d,%d]", lemma, begin, end));
		Mockito.when(o.getBegin()).thenReturn(begin);
		Mockito.when(o.getEnd()).thenReturn(end);
		Mockito.when(o.getCategory()).thenReturn(lemma);
		return o;
	}
	
	public static void createTermOccurrence(int begin, int end, String lemma, JCas cas) {
		TermOccAnnotation anno = (TermOccAnnotation) cas.getCas().createAnnotation(
				cas.getCasType(TermOccAnnotation.type),
				begin,
				end);
		anno.addToIndexes();
	}

	public static String readFile(String string) {
		return readFile(new InputStreamReader(getIS(string), Charset.forName("UTF-8")));
	}
	public static String readFile(File file) throws FileNotFoundException {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			return readFile(reader);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
		
	public static String readFile(Reader reader) {
		try {
			BufferedReader br = new BufferedReader(reader);
			try {
				StringBuilder sb = new StringBuilder();
				int c;

				while ((c = br.read()) != -1) {
					sb.append((char)c);
				}
				return sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream getIS(String file) {
		InputStream is = TestUtil.class.getClassLoader().getResourceAsStream(file);
		return is;
	}

	public static String getTeiTxt(String filename) throws ParserConfigurationException,
	SAXException, IOException, FileNotFoundException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(false);
	    spf.setFeature("http://xml.org/sax/features/namespaces", true);
	    spf.setFeature("http://xml.org/sax/features/validation", false);
	    spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	    spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		SAXParser saxParser = spf.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		TeiToTxtSaxHandler handler = new TeiToTxtSaxHandler();
		xmlReader.setContentHandler(handler);
		xmlReader.parse(new InputSource(TestUtil.getIS(filename)));
		String text = handler.getText();
		return text;
	}
	
	public static AnalysisEngine createAE(TermIndex termIndex, Class<? extends AnalysisComponent> cls, Object... config) {
		MemoryTermIndexManager manager = MemoryTermIndexManager.getInstance();
		manager.clear();

		ExternalResourceDescription termIndexResourceDesc = ExternalResourceFactory.createExternalResourceDescription(
				TermIndexResource.class, 
				termIndex.getName());
		
		
		manager.register(termIndex);
		
		Object[] config2 = ObjectArrays.concat(config, new Object[]{
				TermIndexResource.TERM_INDEX, termIndexResourceDesc
		}, Object.class);
		try {
			return AnalysisEngineFactory.createEngine(cls, config2);
		} catch (ResourceInitializationException e) {
			throw new RuntimeException(e);
		}
	}


}
