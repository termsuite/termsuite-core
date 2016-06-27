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
package org.ttc.project.test;

import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.models.Term;
import eu.project.ttc.tools.TermSuitePipeline;
import org.apache.uima.cas.CASException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ttc.project.Fixtures;
import org.ttc.project.TestUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Simon Meoni on 24/06/16.
 */
public class TermsuiteJsonCas {

    TermSuitePipeline termSuitePipeline;
    TermSuitePipeline termSuitePipelineTerm;
    File jsonBaseFile;
    File jsonResFile;
    private Term recouvrement;
    private Term total;
    private Term recouvrement_total;
    private Term pizza;
    List<String> groupingKeyList = new ArrayList<>();

    @Before
    public void setup() throws CASException {

        recouvrement = Fixtures.term9();
        total = Fixtures.term7();
        recouvrement_total = Fixtures.term12();
        pizza = Fixtures.term13();
        groupingKeyList.add(total.getGroupingKey());
        groupingKeyList.add(recouvrement_total.getGroupingKey());
        groupingKeyList.add(recouvrement.getGroupingKey());
        groupingKeyList.add(pizza.getGroupingKey());
        
        termSuitePipeline =  TermSuitePipeline.create("fr")
                .setResourcePath("/home/smeoni/partage-Termith/scripts/logiciels/termsuite-resources")
                .setCollection(TermSuiteCollection.JSON, "src/test/resources/org/project/ttc/test/temsuite/json/cas" , "UTF-8")
                .haeTermsuiteJsonCasExporter("src/test/resources/org/project/ttc/test/temsuite/json/cas/res")
                .run();

        termSuitePipelineTerm = TermSuitePipeline.create("fr")
                .setResourcePath("/home/smeoni/partage-Termith/scripts/logiciels/termsuite-resources")
                .setCollection(TermSuiteCollection.JSON, "src/test/resources/org/project/ttc/test/temsuite/json/cas" , "UTF-8")
                .aeRegexSpotter()
                .run();
        jsonBaseFile = new File("src/test/resources/org/project/ttc/test/temsuite/json/cas/test.json");
        jsonResFile = new File("src/test/resources/org/project/ttc/test/temsuite/json/cas/res/test.json");
    }

    @Test
    public void ResFileExist() {
        assertTrue(jsonResFile.exists());
    }

    @Test
    public void IOComparison() throws FileNotFoundException {
        final String baseFile = TestUtil.readFile(jsonBaseFile);
        final String resFile = TestUtil.readFile(jsonResFile);
        assertEquals("this two files must be equals", baseFile,resFile);
    }

    @Test
    public void CanCreateTerminology(){
        Collection<Term> termCollection = termSuitePipelineTerm.getTermIndex().getTerms();
        for (Term term : termCollection){
            assertTrue(groupingKeyList.contains(term.getGroupingKey()));
        }
    }
    @After
    public void deleteGenerateJson(){
        if (jsonResFile.exists()){
            jsonResFile.delete();
        }
    }
}
