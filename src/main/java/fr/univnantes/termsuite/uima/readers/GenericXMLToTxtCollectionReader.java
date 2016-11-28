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
package fr.univnantes.termsuite.uima.readers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

public class GenericXMLToTxtCollectionReader extends AbstractTermSuiteCollectionReader {
	private static final Logger logger = LoggerFactory.getLogger(GenericXMLToTxtCollectionReader.class);

	private int failedFileCounter = 0;

	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();
		
	}
	
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
			AbstractToTxtSaxHandler handler = new AbstractToTxtSaxHandler(droppedTags, txtTags);
			xmlReader.setContentHandler(handler);
			String str = Joiner.on("\n").join(Files.readLines(new File(absolutePath), collectionType.getCharset()));
			InputSource inputSource = new InputSource( new StringReader( replaceAll(str)) );
			xmlReader.parse(inputSource);
			text = handler.getText();
		} catch (ParserConfigurationException | SAXException e) {
			logger.error("Could not read document: {}", absolutePath);
			logger.warn("Ignoring document " + absolutePath);
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
				return name.endsWith("." + collectionType.getFileExtension());
			}
		};
	}
	
	@Override
	protected void lastFileRead() {
		super.lastFileRead();
		logger.info("Number of ignored input files due to failures: " + failedFileCounter);
	}


	private static String replaceAll(String s) {
		return s.replaceAll("&apos;", "'")
				.replaceAll("&quot;", "\"");
	}
}
