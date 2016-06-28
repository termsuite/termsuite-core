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
package eu.project.ttc.test.unit.readers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import eu.project.ttc.readers.TermSuiteJsonCasDeserializer;
import eu.project.ttc.readers.TermSuiteJsonCasSerializer;
import eu.project.ttc.test.unit.TestUtil;
import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;

/**
 * Created by Simon Meoni on 24/06/16.
 */
public class TermsuiteJsonCasSerializerDeserializerSpec {

    private File jsonExpectedFile;
    private File jsonResFile;
    private JCas jCas = JCasFactory.createJCas();
    private JCas fixtureCas = JCasFactory.createJCas();
    FileInputStream fis;
    public TermsuiteJsonCasSerializerDeserializerSpec() throws UIMAException {
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws UIMAException, IOException {

        jsonExpectedFile = new File("src/test/resources/org/project/ttc/test/termsuite/json/cas/test.json");
        jsonResFile = temporaryFolder.newFile("test1.json");

        fis = new FileInputStream(jsonExpectedFile);

        SourceDocumentInformation sdi = (SourceDocumentInformation) fixtureCas.getCas()
                .createAnnotation(fixtureCas.getCasType(SourceDocumentInformation.type), 0, 37);
        sdi.setUri("test.json");
        sdi.setOffsetInSource(0);
        sdi.setDocumentIndex(1);
        sdi.setNbDocuments(1);
        sdi.setDocumentSize(21);
        sdi.setCumulatedDocumentSize(21);
        sdi.setLastSegment(true);
        sdi.addToIndexes();

        WordAnnotation wa = (WordAnnotation) fixtureCas.getCas()
                .createAnnotation(fixtureCas.getCasType(WordAnnotation.type), 0, 2);
        wa.setCategory("article");
        wa.setLemma("le");
        wa.setStem("le");
        wa.setTag("DET:ART");
        wa.setSubCategory("det");
        wa.setRegexLabel("D");
        wa.setNumber("sg");
        wa.setGender("ms");
        wa.setCase("det");
        wa.setMood("test");
        wa.setTense("test");
        wa.setPerson("test");
        wa.addToIndexes();

        wa = (WordAnnotation) fixtureCas.getCas()
                .createAnnotation(fixtureCas.getCasType(WordAnnotation.type), 3, 15);
        wa.setCategory("noun");
        wa.setLemma("recouvrement");
        wa.setStem("recouvr");
        wa.setTag("NOM");
        wa.setSubCategory("nom");
        wa.setRegexLabel("N");
        wa.setGender("ms");
        wa.setCase("nom");
        wa.setTense("test");
        wa.setPerson("test");
        wa.addToIndexes();

        WordAnnotation wa1 = (WordAnnotation) fixtureCas.getCas()
                .createAnnotation(fixtureCas.getCasType(WordAnnotation.type), 16, 21);
        wa1.setCategory("adjective");
        wa1.setLemma("total");
        wa1.setStem("tot");
        wa1.setTag("ADJ");
        wa1.setSubCategory("test");
        wa1.setRegexLabel("A");
        wa1.setNumber("sg");
        wa1.setGender("ms");
        wa1.setCase("dat");
        wa1.setMood("test");
        wa1.setTense("test");
        wa1.setPerson("test");
        wa1.setDegree("test");
        wa1.setFormation("test");
        wa1.setLabels("label");
        wa1.addToIndexes();

        TermOccAnnotation toa = (TermOccAnnotation) fixtureCas.getCas()
                .createAnnotation(fixtureCas.getCasType(TermOccAnnotation.type),3,21);


        StringArray stringArray = new StringArray(fixtureCas, 2);
        stringArray.set(0,"N");
        stringArray.set(1,"A");
        FSArray fs = (FSArray) fixtureCas.getCas().createArrayFS(2);
        fs.set(0,wa);
        fs.set(1,wa1);
        toa.setPattern(stringArray);
        toa.setSpottingRuleName("na");
        toa.setTermKey("na: recouvrement total");
        toa.setWords(fs);
        toa.addToIndexes();

        fixtureCas.setDocumentText("le recouvrement total");


    }

    @Test
    public void SerializationTest() throws IOException {

        //Test Out File
        TermSuiteJsonCasSerializer.serialize(new FileWriter(jsonResFile),fixtureCas);
        final String baseFile = TestUtil.readFile(jsonExpectedFile);
        final String resFile = TestUtil.readFile(jsonResFile);
        assertEquals(baseFile,resFile);
    }

    @Test
    public void DeserializationTest(){
        TermSuiteJsonCasDeserializer.deserialize(fis, jCas.getCas());

        //Test Sdi
        assertEquals(fixtureCas.getAnnotationIndex(SourceDocumentInformation.type).iterator().next().toString(),
                jCas.getAnnotationIndex(SourceDocumentInformation.type).iterator().next().toString());

        //Test wordAnnotation
        FSIterator<Annotation> itWaEx = fixtureCas.getAnnotationIndex(WordAnnotation.type).iterator();
        FSIterator<Annotation> itWa = jCas.getAnnotationIndex(WordAnnotation.type).iterator();
        while (itWa.hasNext()){
            assertEquals(itWaEx.next().toString(),itWa.next().toString());
        }
        assertEquals(
        		fixtureCas.getAnnotationIndex(TermOccAnnotation.type).iterator().next().toString(),
                jCas.getAnnotationIndex(TermOccAnnotation.type).iterator().next().toString());

        //Test termOccAnnotation
        TermOccAnnotation toaExpected = (TermOccAnnotation) fixtureCas.getAnnotationIndex(TermOccAnnotation.type).iterator().next();
        TermOccAnnotation toa = (TermOccAnnotation) jCas.getAnnotationIndex(TermOccAnnotation.type).iterator().next();
        for (int i = 0; i < toa.getWords().size(); i++){
            assertEquals(toaExpected.getWords().get(i).toString(),toa.getWords().get(i).toString());
        }

        //Test covered Text
        assertEquals("le recouvrement total", jCas.getDocumentText());
    }

}
