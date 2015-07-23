/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package eu.project.ttc.readers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class TeiCollectionReader extends AbstractTermSuiteCollectionReader {
	private static String MATH_ML_URI = "http://www.w3.org/1998/Math/MathML";
	private static String MATH_ML_PREFIX = "mml";
	
	private static final Logger logger = LoggerFactory.getLogger(TeiCollectionReader.class);

	private int failedFileCounter = 0;

	@Override
	protected String getDocumentText(String absolutePath, String encoding) throws IOException {
		String text;
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
		    spf.setNamespaceAware(true);
		    spf.setFeature("http://xml.org/sax/features/namespaces", true);
		    spf.setFeature("http://xml.org/sax/features/validation", false);
		    spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		    spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			TeiToTxtSaxHandler handler = new TeiToTxtSaxHandler();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(absolutePath));
			text = handler.getText();
		} catch (ParserConfigurationException | SAXException e) {
			logger.error("Could not read TEI document: {}", absolutePath);
			logger.warn("Ignoring TEI document " + absolutePath);
			text = "";
			this.failedFileCounter++;
		}
		return text;
	}

	@Override
	protected FilenameFilter getFileFilter() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
	}
	
	@Override
	protected void lastFileRead() {
		logger.info("Number of ignored input files due to failures: " + failedFileCounter);
	}

}
