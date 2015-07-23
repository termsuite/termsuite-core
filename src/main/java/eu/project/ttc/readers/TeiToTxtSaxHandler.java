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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Parses tei input files into a String where offsets are the same, but all tags replaced 
 * with whitspaces.
 * 
 * @author Damien Cram
 *
 */
public class TeiToTxtSaxHandler extends DefaultHandler {
	/* tags that will not be read (replaced with withspaces) */
	private static final String[] DROP_TAGS = new String[] { "cross-ref",
			"float-anchor", "hsp" };

	/* tags that be read */
	private static final String[] TXT_TAGS = new String[] { "title",
			"section-title", "section-para", "simple-para", "note-para",
			"para", "textref" };
	
	private static final char WHITESPACE = ' ';
	private static final char NEW_LINE = '\n';

	private StringBuffer sb = new StringBuffer();

	private int inTextDeep = 0;
	private int inDroppedTagDeep = 0;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (tagFound(TXT_TAGS, localName)) {
			this.inTextDeep++;
		} else if (tagFound(DROP_TAGS, localName)) {
			this.inDroppedTagDeep++;
		}
	}

	public boolean tagFound(String[] ref, String tagName) {
		for(String c:ref) {
			if(c.equals(tagName))
				return true;
		}
		return false;
	}

	
	private int i = 0;
	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		checkCharacters(ch);
		if (inText() && !inDroppedTag()) {
			for(;i<start;i++)
				sb.append(ch[i] == NEW_LINE ? NEW_LINE : WHITESPACE);
			for(;i<start+length;i++) 
				sb.append(ch[i]);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (tagFound(TXT_TAGS, localName)) {
			this.inTextDeep--;
		} else if (tagFound(DROP_TAGS, localName)) {
			this.inDroppedTagDeep--;
		}

	}
	
	private boolean inText() {
		return inTextDeep > 0;
	}
	private boolean inDroppedTag() {
		return inDroppedTagDeep > 0;
	}
	
	private char[] internalAry;
	/*
	 * Checks that the char array is always the same all along the execution of the parser
	 */
	private void checkCharacters(char[] chars) {
		if(internalAry == null)
			internalAry = chars;
	}
	
	public String getText() {
		for(;i<internalAry.length;i++) 
			sb.append(WHITESPACE);
		return sb.toString();
	}
}