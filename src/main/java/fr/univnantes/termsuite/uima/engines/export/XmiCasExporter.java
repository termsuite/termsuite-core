
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
package fr.univnantes.termsuite.uima.engines.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.types.SourceDocumentInformation;

public class XmiCasExporter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(XmiCasExporter.class);
	
	public static final String OUTPUT_DIRECTORY = "OutputDirectory";
	@ConfigurationParameter(name = OUTPUT_DIRECTORY, mandatory=true)
	protected String directoryPath;

	protected File directoryFile;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.directoryFile = new File(this.directoryPath);
		if(!this.directoryFile.exists())
			this.directoryFile.mkdirs();
		Preconditions.checkState(this.directoryFile.exists(), String.format("Directory %s does noes exists", this.directoryPath));
		Preconditions.checkNotNull(this.directoryFile.isDirectory(), String.format("Not a directory: %s", this.directoryPath));
		Preconditions.checkState(this.directoryFile.canWrite(), String.format("Cannot write to directory %s.", this.directoryPath));
	}
	
	protected String getExportFilePath(JCas cas) {
		AnnotationIndex<Annotation> index = cas.getAnnotationIndex(SourceDocumentInformation.type);
		FSIterator<Annotation> iterator = index.iterator();
		if (iterator.hasNext()) {
			SourceDocumentInformation annotation = (SourceDocumentInformation) iterator.next();
			File file = new File(annotation.getUri());
			String name = file.getName();
			int i = name.lastIndexOf('.');
			if (i == -1) {
				return name + ".xmi";
			} else {
				return name.substring(0, i) + ".xmi";
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException { 
		try {
			String name = this.getExportFilePath(cas);
			if (name == null) { 
				LOGGER.warn("Skiping CAS Serialization, because no SourceDocumentInformation was found.");
			} else {
				File file = new File(this.directoryFile, name);
				OutputStream stream = new FileOutputStream(file);
				try {
					LOGGER.debug("Writing " + file.getAbsolutePath());
					XmiCasSerializer.serialize(cas.getCas(), cas.getTypeSystem(), stream);
				} catch (SAXParseException e) {
					LOGGER.warn("Failure while serializing " + file + "\nCaused by " + e.getClass().getCanonicalName() + ": " + e.getMessage());
				} finally {
					stream.close();
				}
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
}
