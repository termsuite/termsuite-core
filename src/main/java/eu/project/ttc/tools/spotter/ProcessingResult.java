/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.tools.spotter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.resource.metadata.FsIndexDescription;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.TypeSystemUtil;
import org.apache.uima.util.XmlCasDeserializer;
import org.xml.sax.SAXException;

public class ProcessingResult {
	
	private File file;
	
	private void setFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return this.file;
	}
	
	private static File TemporaryDirectory;
	
	static {
		TemporaryDirectory = new File(System.getProperty("java.io.tmpdir"));
	}
	
	private TypeSystemDescription typeSystem;
	
	private void setTypeSystem(TypeSystemDescription typeSystem) {
		this.typeSystem = typeSystem;
	}
	
	public TypeSystemDescription getTypeSystem() {
		return this.typeSystem;
	}
	
	public String toString() {
		return this.getFile().getName();
	}
	
 	public void setCas(CAS cas) throws AnalysisEngineProcessException {
 		TypeSystemDescription ts = TypeSystemUtil.typeSystem2TypeSystemDescription(cas.getTypeSystem());
 		this.setTypeSystem(ts);
 		try { 
 	 		String uri = this.doRetrieve(cas);
 	 		File file = new File(TemporaryDirectory,this.doFlatten(uri));
 	 		this.setFile(file);
 			this.doWrite(cas,file);
 		} catch (Exception e) { 
 			throw new AnalysisEngineProcessException(e);
 		}
	}
	
	private String doRetrieve(CAS cas) throws Exception {
		Type type = cas.getTypeSystem().getType(SourceDocumentInformation.class.getCanonicalName());
		AnnotationIndex<AnnotationFS> index = cas.getAnnotationIndex(type);
		FSIterator<AnnotationFS> iterator = index.iterator();
		if (iterator.hasNext()) {
			SourceDocumentInformation annotation = (SourceDocumentInformation) iterator.next();
			return annotation.getUri();
		} else {
			throw new Exception("No " + SourceDocumentInformation.class.getCanonicalName() + " annotations found.");
		}
	}

	private String doFlatten(String uri) {
		String separator = System.getProperty("file.separator");
		int index = uri.lastIndexOf(separator);
		int last = uri.lastIndexOf(".");
		if (index == -1 || last == -1 || index >= last) {
			return uri;
		} else {
			return uri.substring(index + 1,last) + ".xmi";
		}
	}

	/**
	 * Serialize a CAS to a file in XMI format.
	 */
	private void doWrite(CAS cas,File file) throws IOException {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			XmiCasSerializer.serialize(cas,cas.getTypeSystem(),stream);
			stream.close();
		} catch (SAXException e) {
			// ignore
		}
	}
	
	public CAS getCas() throws Exception {
		CAS cas = null;
		TypeSystemDescription typeSystem = this.getTypeSystem();
		TypePriorities typePriorities = null;
		FsIndexDescription[] indexes = new FsIndexDescription[0];
		cas = CasCreationUtils.createCas(typeSystem,typePriorities,indexes);
		FileInputStream is = new FileInputStream(this.getFile());
		XmlCasDeserializer.deserialize(is,cas,true);
		is.close();
		return cas;
	}
	
}
