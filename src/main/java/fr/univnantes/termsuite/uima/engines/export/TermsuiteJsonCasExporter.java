
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.uima.readers.TermSuiteJsonCasSerializer;

/**
 * Created by Simon Meoni on 02/06/16.
 */
public class TermsuiteJsonCasExporter extends CasExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TermsuiteJsonCasExporter.class);
    public static final String OUTPUT_DIRECTORY = "OutputDirectory";

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        String name = this.getExportFilePath(aJCas, "json");
        File file = new File(this.directoryFile, name);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(fos, "UTF-8");
            TermSuiteJsonCasSerializer.serialize(writer,aJCas);

        } catch (IOException e) {
            LOGGER.error("Failure while serializing " + name + "\nCaused by"
                    + e.getClass().getCanonicalName() + ":" + e.getMessage(), e);
        }
    }
}
