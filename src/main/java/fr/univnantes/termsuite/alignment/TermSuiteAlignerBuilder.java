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
package fr.univnantes.termsuite.alignment;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.metrics.Cosine;
import fr.univnantes.termsuite.metrics.Jaccard;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.resources.BilingualDictionary;


public class TermSuiteAlignerBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteAlignerBuilder.class);
	
	private Optional<Terminology> sourceTerminology = Optional.empty();
	private Optional<Terminology> targetTerminology = Optional.empty();
	private Optional<String> dicoPath = Optional.empty();
	private Optional<? extends SimilarityDistance> distance = Optional.of(new Cosine());
//	private Optional<String> sourceTerminologyLanguage = Optional.empty();
//	private Optional<String> targetTerminologyLanguage = Optional.empty();
	
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

	public TermSuiteAlignerBuilder setSourceTerminology(Terminology sourceTerminology) {
		this.sourceTerminology = Optional.of(sourceTerminology);
		return this;
	}
	
	public TermSuiteAlignerBuilder setDistance(SimilarityDistance distance) {
		this.distance = Optional.of(distance);
		return this;
	}
	
	public TermSuiteAlignerBuilder setTargetTerminology(Terminology targetTerminology) {
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
	
	public TermSuiteAlignerBuilder setDistanceCosine() {
		return setDistance(new Cosine());
	}
	
	public TermSuiteAlignerBuilder setDistanceJaccard() {
		return setDistance(new Jaccard());
	}
	
}
