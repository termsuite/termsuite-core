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
package fr.univnantes.termsuite.model;

import java.nio.charset.Charset;

public enum TermSuiteCollection {
	ZZ_IZV("xml", Charset.forName("UTF-8")), // russian general language
	ZZ_SGML("sgml", Charset.forName("iso-8859-1")), // english, spannish, german general language
	ZZ_LEMONDE("xml", Charset.forName("ISO-8859-15")), // french general language
	TXT("txt", Charset.forName("UTF-8")),
	ISTEX_API("/", Charset.forName("UTF-8")),
	TEI("tei", Charset.forName("UTF-8")),
	EMPTY("", Charset.forName("UTF-8")), 
	XMI("xmi", Charset.forName("UTF-8")),
	JSON("json", Charset.forName("UTF-8"));

	private String defaultFileExtension;
	private Charset charset;

	private TermSuiteCollection(String defaultFileExtension, Charset charset) {
		this.defaultFileExtension = defaultFileExtension;
		this.charset = charset;
	}

	public String getFileExtension() {
		return defaultFileExtension;
	}
	
	public Charset getCharset() {
		return charset;
	}
}
