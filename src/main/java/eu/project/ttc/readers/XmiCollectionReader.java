package eu.project.ttc.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class XmiCollectionReader extends AbstractTermSuiteCollectionReader {
	private static final Logger logger = LoggerFactory.getLogger(XmiCollectionReader.class);

	@Override
	protected void fillCas(CAS cas, File file) throws IOException, CollectionException {
		try {
			XmiCasDeserializer.deserialize(new FileInputStream(file), cas);
		} catch (SAXException e) {
			logger.error("Could not deserialize xmi file.", e);
		}
	}

	@Override
	protected String getDocumentText(String uri, String encoding) throws IOException {
		throw new IllegalStateException("AbstractTermSuiteCollectionReader#getDocumentText() Should not be invoked on this Reader.");
	}
}
