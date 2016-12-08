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
package fr.univnantes.termsuite.uima.engines.export;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import fr.univnantes.termsuite.export.TbxExporter;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.engines.termino.AbstractTerminologyExporter;

/**
 * Exports a {@link Terminology} in TSV format
 * 
 * @author Damien Cram
 *
 */
public class TbxExporterAE extends AbstractTerminologyExporter {
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		TbxExporter.export(this.terminoResource.getTerminology(), writer, traverser);
	}
}
