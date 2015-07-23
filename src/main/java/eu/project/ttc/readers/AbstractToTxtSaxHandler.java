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
 * Parses xml files from an xml corpus based on a list of two lists of tag names:
 *  - the list of dropped tags (not interesting tags) : their contents are skipped
 *  - the list of txt tags : tags whose content is kept in the out put txt file
 * 
 * @author Damien Cram
 *
 */
public class AbstractToTxtSaxHandler extends DefaultHandler {
	/* tags that will not be read (replaced with withspaces) */
	private String[] dropTags = new String[] {};

	/* tags that be read */
	private String[] txtTags = new String[] {};
	
	private static final char NEW_LINE = '\n';
	private static final char POINT = '.';

	private StringBuffer sb = new StringBuffer();

	private int inTextDeep = 0;
	private int inDroppedTagDeep = 0;
	
	

	public AbstractToTxtSaxHandler(String[] dropTags, String[] txtTags) {
		super();
		this.dropTags = dropTags;
		this.txtTags = txtTags;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (tagFound(txtTags, localName)) {
			this.inTextDeep++;
		} else if (tagFound(dropTags, localName)) {
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

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		checkCharacters(ch);
		if (inText() && !inDroppedTag()) {
			for(int i = start;i<start+length;i++) 
				sb.append(ch[i]);
			sb.append(POINT);
			sb.append(NEW_LINE);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (tagFound(txtTags, localName)) {
			this.inTextDeep--;
		} else if (tagFound(dropTags, localName)) {
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
		return sb.toString();
	}
}