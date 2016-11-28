
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
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import fr.univnantes.termsuite.types.WordAnnotation;

/**
 * Created by smeoni on 01/12/15.
 */

public class JsonCasExporter extends CasExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonCasExporter.class);


    public static final String[] STRING_FEATURES = {"lemma", "tag", };
    public static final String[] INT_FEATURES = {"begin", "end"};
    public static final String[] DOUBLE_FEATURES = {};


    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    /*
     *  Cette méthode est appelée par le framework UIMA
     *  pour chaque document  de ta collection (corpus).
     *
     *  Tu peux générer ton fichier compagnon dans cette méthode.
     *  (Je te donne l'astuce pour retrouver le nom et le chemin du fichier
     *  de ton corpus correspondant au CAS passé en paramètre de cette
     *  méthode plus tard)
     */

        FSIterator<Annotation> it =  aJCas.getAnnotationIndex().iterator();
        Annotation a;
        JsonFactory jsonFactory = new JsonFactory();
        String name = this.getExportFilePath(aJCas, "json");
        File file = new File(this.directoryFile, name);
        FileWriter writer = null;
        try {
        	writer = new FileWriter(file);
            LOGGER.debug("Writing " + file.getPath());
            JsonGenerator jg = jsonFactory.createGenerator(writer);
            jg.useDefaultPrettyPrinter();
            jg.writeStartObject();
            jg.writeStringField("file",name);
            jg.writeArrayFieldStart("tag");
            while(it.hasNext()) {
                a = it.next();
                if (a instanceof WordAnnotation) {
                    jg.writeStartObject();
                    WordAnnotation wordAnno = (WordAnnotation) a;
                    for(Feature feat:wordAnno.getType().getFeatures()) {
                        FeatureStructure featureValue = wordAnno.getFeatureValue(feat);
                        if(featureValue != null) {
                            jg.writeFieldName(feat.getName());
                            jg.writeObject(featureValue);
                        }
                    }
                    jg.writeStringField("tag",wordAnno.getTag());
                    jg.writeStringField("lemma",wordAnno.getLemma());
                    jg.writeNumberField("begin",wordAnno.getBegin());
                    jg.writeNumberField("end",wordAnno.getEnd());
                    jg.writeEndObject();
                }
            }
            jg.writeEndArray();
            jg.writeEndObject();
            jg.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Failure while serializing " + name + "\nCaused by"
                    + e.getClass().getCanonicalName() + ":" + e.getMessage(), e);
        }
    }
}

