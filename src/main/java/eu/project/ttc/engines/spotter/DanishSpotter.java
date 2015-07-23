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
package eu.project.ttc.engines.spotter;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

import com.google.common.base.Optional;

import eu.project.ttc.engines.desc.SpotterBuilder;
import eu.project.ttc.tools.TermSuitePipeline;

public class DanishSpotter implements SpotterBuilder {

	@Override
	public AnalysisEngineDescription createSpotter(
			String treeTaggerHome, 
			boolean enableXMIOutput, 
			String outputXMIDirectory, 
			boolean enableTSVOutput,
			Optional<String> regexFilePath
		)
					throws ResourceInitializationException, InvalidXMLException, ClassNotFoundException {
		return TermSuitePipeline.create("da")
			.setTreeTaggerHome(treeTaggerHome)
			.setSyntacticRegexesFilePath(regexFilePath.get())
			.enableSyntacticLabels()
			.wordTokenizer()
			.treeTagger()
			.ttNormalizer()
			.stemmer()
			.regexSpotter()
			.stopWordsFilter()
//			.writer(
//					Writer.PARAM_DIRECTORY, outputXMIDirectory)
//			.spotterTSVWriter(
//					Writer.PARAM_DIRECTORY, outputXMIDirectory,
//					SpotterTSVWriter.PARAM_IS_ENABLED, enableTSVOutput)
			.createDescription();
	}
}
