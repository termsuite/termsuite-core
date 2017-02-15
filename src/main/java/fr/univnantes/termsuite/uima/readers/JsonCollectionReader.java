
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

package fr.univnantes.termsuite.uima.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;

/**
 * Created by smeoni on 26/05/16.
 */
public class JsonCollectionReader extends AbstractTermSuiteCollectionReader{

    @Override
    protected void fillCas(CAS cas, File file) throws IOException, CollectionException {
        JsonCasDeserializer.deserialize(new FileInputStream(file), cas);
    }
    @Override
    protected String getDocumentText(String uri, String encoding) throws IOException {
        throw new IllegalStateException("AbstractTermSuiteCollectionReader#getDocumentText() Should not be invoked on this Reader.");
    }
}