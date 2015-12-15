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
package eu.project.ttc.tools;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import eu.project.ttc.engines.BilingualAligner;
import eu.project.ttc.metrics.Cosine;
import eu.project.ttc.metrics.SimilarityDistance;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.BilingualDictionary;


public class TermSuiteAlignerBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteAlignerBuilder.class);
	
	private Optional<TermIndex> sourceTerminology = Optional.absent();
	private Optional<TermIndex> targetTerminology = Optional.absent();
	private Optional<String> dicoPath = Optional.absent();
	private Optional<? extends SimilarityDistance> distance = Optional.of(new Cosine());
//	private Optional<String> sourceTerminologyLanguage = Optional.absent();
//	private Optional<String> targetTerminologyLanguage = Optional.absent();
	
	private TermSuiteAlignerBuilder() {}
	public static TermSuiteAlignerBuilder start() {
		return new TermSuiteAlignerBuilder();
	}
	
//	/**
//	 * 
//	 * @deprecated not used currently
//	 * @param sourceTerminologyLanguage
//	 * @return
//	 */
//	@Deprecated
//	public TermSuiteAlignerBuilder setSourceTerminologyLanguage(String sourceTerminologyLanguage) {
//		this.sourceTerminologyLanguage = Optional.of(sourceTerminologyLanguage);
//		return this;
//	}
//	
//	public TermSuiteAlignerBuilder setTargetTerminologyLanguage(String targetTerminologyLanguage) {
//		this.targetTerminologyLanguage = Optional.of(targetTerminologyLanguage);
//		return this;
//	}

	public TermSuiteAlignerBuilder setSourceTerminology(TermIndex sourceTerminology) {
		this.sourceTerminology = Optional.of(sourceTerminology);
		return this;
	}
	
	public TermSuiteAlignerBuilder setDistance(SimilarityDistance distance) {
		this.distance = Optional.of(distance);
		return this;
	}
	
	public TermSuiteAlignerBuilder setTargetTerminology(TermIndex targetTerminology) {
		this.targetTerminology = Optional.of(targetTerminology);
		return this;
	}
	
	public TermSuiteAlignerBuilder setDicoPath(String dicoPath) {
		Preconditions.checkArgument(new File(dicoPath).exists(), "File %s does not exist", dicoPath);
		Preconditions.checkArgument(new File(dicoPath).isFile(), "Not a file: %s", dicoPath);
		this.dicoPath = Optional.of(dicoPath);
		return this;
	}
	
	/**
	 * Creates the bilingual single-word aligner.
	 * 
	 * @return the aligner object
	 */
	public BilingualAligner create() {
		Preconditions.checkArgument(dicoPath.isPresent(), "No dictionary file set");
		Preconditions.checkArgument(targetTerminology.isPresent(), "No target terminology given");
			
		try {
			BilingualDictionary dico;
			dico = BilingualDictionary.load(dicoPath.get());
			return new BilingualAligner(dico, sourceTerminology.get(), targetTerminology.get(), distance.get());
		} catch (IOException e) {
			LOGGER.error("Could not create BilingualSWAligner due to io exception: %s", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
}
