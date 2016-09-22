
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

package eu.project.ttc.readers;

import static eu.project.ttc.readers.JsonCasConstants.F_BEGIN;
import static eu.project.ttc.readers.JsonCasConstants.F_CASE;
import static eu.project.ttc.readers.JsonCasConstants.F_CATEGORY;
import static eu.project.ttc.readers.JsonCasConstants.F_CORPUS_SIZE;
import static eu.project.ttc.readers.JsonCasConstants.F_CUMULATED_DOCUMENT_SIZE;
import static eu.project.ttc.readers.JsonCasConstants.F_DEGREE;
import static eu.project.ttc.readers.JsonCasConstants.F_DOCUMENT_INDEX;
import static eu.project.ttc.readers.JsonCasConstants.F_DOCUMENT_SIZE;
import static eu.project.ttc.readers.JsonCasConstants.F_END;
import static eu.project.ttc.readers.JsonCasConstants.F_FORMATION;
import static eu.project.ttc.readers.JsonCasConstants.F_GENDER;
import static eu.project.ttc.readers.JsonCasConstants.F_LABELS;
import static eu.project.ttc.readers.JsonCasConstants.F_LAST_SEGMENT;
import static eu.project.ttc.readers.JsonCasConstants.F_LEMMA;
import static eu.project.ttc.readers.JsonCasConstants.F_MOOD;
import static eu.project.ttc.readers.JsonCasConstants.F_NB_DOCUMENTS;
import static eu.project.ttc.readers.JsonCasConstants.F_NUMBER;
import static eu.project.ttc.readers.JsonCasConstants.F_OFFSET_IN_SOURCE;
import static eu.project.ttc.readers.JsonCasConstants.F_PATTERN;
import static eu.project.ttc.readers.JsonCasConstants.F_PERSON;
import static eu.project.ttc.readers.JsonCasConstants.F_REGEX_LABEL;
import static eu.project.ttc.readers.JsonCasConstants.F_SPOTTING_RULE_NAME;
import static eu.project.ttc.readers.JsonCasConstants.F_STEM;
import static eu.project.ttc.readers.JsonCasConstants.F_SUB_CATEGORY;
import static eu.project.ttc.readers.JsonCasConstants.F_TAG;
import static eu.project.ttc.readers.JsonCasConstants.F_TENSE;
import static eu.project.ttc.readers.JsonCasConstants.F_TERM_KEY;
import static eu.project.ttc.readers.JsonCasConstants.F_URI;
import static eu.project.ttc.readers.JsonCasConstants.F_WORDS;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import eu.project.ttc.types.FixedExpression;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;

import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;

/**
 * Created by smeoni on 27/05/16.
 */
public class TermSuiteJsonCasDeserializer {
	private static final Logger logger = LoggerFactory.getLogger(TermSuiteJsonCasDeserializer.class);

    private static JsonParser parser;
    private static JsonToken token;

    public static void deserialize(InputStream inputStream, CAS cas) {
    	deserialize(inputStream, cas, Charset.defaultCharset().name());
    }

    public static void deserialize(InputStream inputStream, CAS cas, String encoding) {
    	Preconditions.checkNotNull(inputStream, "Paramater input stream is null");
    	Preconditions.checkNotNull(inputStream, "Paramater CAS is null");
    	
        try {


            JsonFactory factory = new JsonFactory();
            parser = factory.createParser(inputStream);

            SourceDocumentInformation sdi = (SourceDocumentInformation) cas.createAnnotation(cas.getJCas().getCasType(SourceDocumentInformation.type), 0, 0);
            WordAnnotation wa = (WordAnnotation) cas.createAnnotation(cas.getJCas().getCasType(WordAnnotation.type), 0, 0);
            TermOccAnnotation toa = (TermOccAnnotation) cas.createAnnotation(cas.getJCas().getCasType(TermOccAnnotation.type), 0, 0);
            FixedExpression fe = (FixedExpression) cas.createAnnotation(cas.getJCas().getCasType(FixedExpression.type), 0, 0);
            boolean inSdi = false;
            boolean inWa = false;
            boolean inToa = false;
            boolean inFe = false;
            boolean inCoveredText = false;

            while ((token=parser.nextToken()) != null)
            {

                    if (inSdi){

                        if (token == JsonToken.END_OBJECT) {
                            inSdi = false;
                        }
                        else {
                            fillSdi(parser,token,sdi);
                        }
                    }

                    else if (inWa){
                        if (token == JsonToken.END_ARRAY) {
                            inWa = false;
                        }
                        else if (token == JsonToken.END_OBJECT) {
                            wa.addToIndexes();
                            wa = (WordAnnotation) cas.createAnnotation(cas.getJCas().getCasType(WordAnnotation.type), 0, 0);
                        }
                        fillWordAnnotations(parser, token, wa);
                    }

                    else if (inToa){
                        if (token == JsonToken.END_ARRAY
                                && Objects.equals(parser.getParsingContext().getCurrentName(), "term_occ_annotations")) {
                            inToa = false;
                        }
                        else if (token == JsonToken.END_OBJECT) {
                            toa.addToIndexes();
                            toa = (TermOccAnnotation) cas.createAnnotation(cas.getJCas().getCasType(TermOccAnnotation.type), 0, 0);
                        }
                        FillTermOccAnnotations(parser, token, toa, cas);
                    }

                    else if (inFe){
                        if (token == JsonToken.END_ARRAY
                                && Objects.equals(parser.getParsingContext().getCurrentName(), "fixed_expressions")) {
                            inFe = false;
                        }
                        else if (token == JsonToken.END_OBJECT) {
                            fe.addToIndexes();
                            fe = (FixedExpression) cas.createAnnotation(cas.getJCas().getCasType(FixedExpression.type), 0, 0);
                        }
                        FillFixedExpressions(parser, token, fe, cas);
                    }

                    else if (inCoveredText){
                        if (token == JsonToken.VALUE_STRING) {
                            String text = parser.getText();
                            cas.setDocumentText(text);
                        }
                    }

                    else if ("sdi".equals(parser.getParsingContext().getCurrentName())) {
                        inSdi = true;
                    }

                    else if ("word_annotations".equals(parser.getParsingContext().getCurrentName())) {
                        inWa = true;
                    }

                    else if ("term_occ_annotations".equals(parser.getParsingContext().getCurrentName())) {
                        inToa = true;
                    }

                    else if ("fixed_expressions".equals(parser.getParsingContext().getCurrentName())) {
                        inFe = true;
                    }

                    else if ("covered_text".equals(parser.getParsingContext().getCurrentName())) {
                        inCoveredText = true;
                    }
                }
            sdi.addToIndexes();
        } catch (IOException | CASException e) {
            logger.error("An error occurred during TermSuite Json Cas parsing", e);
        }
    }

    private static void FillFixedExpressions(JsonParser parser, JsonToken token, FixedExpression fe, CAS cas) throws IOException {
        if (token.equals(JsonToken.FIELD_NAME)){
            switch (parser.getCurrentName()){
                case F_BEGIN :
                    fe.setBegin(parser.nextIntValue(0));
                    break;
                case F_END :
                    fe.setEnd(parser.nextIntValue(0));
                    break;
            }
        }
    }

    private static void fillWords(TermOccAnnotation toa, CAS cas) throws CASException, IOException {
        FSArray fs = (FSArray) cas.createArrayFS(toa.getPattern().size());
        int i = 0;
        int begin = -1;
        int end = -1;
        while(i != toa.getPattern().size()){
            if (begin != -1 && token == JsonToken.VALUE_NUMBER_INT){
                end = parser.getValueAsInt();
            }
            else if (token == JsonToken.VALUE_NUMBER_INT){
                begin = parser.getValueAsInt();
            }
            else if (end != -1){
                List<WordAnnotation> wa = JCasUtil.selectCovered(cas.getJCas(),WordAnnotation.class,begin,end);
                fs.set(i,wa.get(0));
                begin = -1;
                end = -1;
                i++;
            }
            token = parser.nextToken();
        }
        toa.setWords(fs);

    }

    private static void fillWordAnnotations(JsonParser parser, JsonToken token, WordAnnotation wa) throws IOException {
        if (token.equals(JsonToken.FIELD_NAME)){
            switch (parser.getCurrentName()){
                case F_CATEGORY :
                    wa.setCategory(parser.nextTextValue());
                    break;
                case F_LEMMA :
                    wa.setLemma(parser.nextTextValue());
                    break;
                case F_STEM :
                    wa.setStem(parser.nextTextValue());
                    break;
                case F_TAG :
                    wa.setTag(parser.nextTextValue());
                    break;
                case F_SUB_CATEGORY :
                    wa.setSubCategory(parser.nextTextValue());
                    break;
                case F_REGEX_LABEL :
                    wa.setRegexLabel(parser.nextTextValue());
                    break;
                case F_NUMBER :
                    wa.setNumber(parser.nextTextValue());
                    break;
                case F_GENDER :
                    wa.setGender(parser.nextTextValue());
                    break;
                case F_CASE :
                    wa.setCase(parser.nextTextValue());
                    break;
                case F_MOOD :
                    wa.setMood(parser.nextTextValue());
                    break;
                case F_TENSE :
                    wa.setTense(parser.nextTextValue());
                    break;
                case F_PERSON :
                    wa.setPerson(parser.nextTextValue());
                    break;
                case F_DEGREE :
                    wa.setDegree(parser.nextTextValue());
                    break;
                case F_FORMATION :
                    wa.setFormation(parser.nextTextValue());
                    break;
                case F_LABELS :
                    wa.setLabels(parser.nextTextValue());
                    break;
                case F_BEGIN :
                    wa.setBegin(parser.nextIntValue(0));
                    break;
                case F_END :
                    wa.setEnd(parser.nextIntValue(0));
                    break;
            }
        }
    }

    private static void fillSdi(JsonParser parser , JsonToken token, SourceDocumentInformation sdi) throws IOException {
        if (token.equals(JsonToken.FIELD_NAME)){
            switch (parser.getCurrentName()){
                case F_URI :
                    sdi.setUri(parser.nextTextValue());
                    break;
                case F_OFFSET_IN_SOURCE :
                    sdi.setOffsetInSource(parser.nextIntValue(0));
                    break;
                case F_DOCUMENT_INDEX :
                    sdi.setDocumentIndex(parser.nextIntValue(0));
                    break;
                case F_NB_DOCUMENTS :
                    sdi.setNbDocuments(parser.nextIntValue(0));
                    break;
                case F_DOCUMENT_SIZE :
                    sdi.setDocumentSize(parser.nextIntValue(0));
                    break;
                case F_CUMULATED_DOCUMENT_SIZE :
                    sdi.setCumulatedDocumentSize(parser.nextLongValue(0));
                    break;
                case F_CORPUS_SIZE :
                    sdi.setCorpusSize(parser.nextLongValue(0));
                    break;
                case F_LAST_SEGMENT :
                    sdi.setLastSegment(parser.nextBooleanValue());
                    break;
                case F_BEGIN :
                    sdi.setBegin(parser.nextIntValue(0));
                    break;
                case F_END :
                    sdi.setEnd(parser.nextIntValue(0));
                    break;
            }
        }
    }

    private static void FillTermOccAnnotations(JsonParser parser , JsonToken token, TermOccAnnotation toa, CAS cas) throws IOException, CASException {
        if (token.equals(JsonToken.FIELD_NAME)){
            switch (parser.getCurrentName()){
                case F_PATTERN :
                    String[] patternTable = parser.nextTextValue().split(" ");
                    StringArray stringArray = new StringArray(cas.getJCas(), patternTable.length);

                    for (int i = 0; i < patternTable.length; i++){
                        stringArray.set(i,patternTable[i]);
                    }
                    toa.setPattern(stringArray);
                    break;

                case F_SPOTTING_RULE_NAME :
                    toa.setSpottingRuleName(parser.nextTextValue());
                    break;
                case F_TERM_KEY :
                    toa.setTermKey(parser.nextTextValue());
                    break;
                case F_WORDS :
                    fillWords(toa,cas);
                    break;
                case F_BEGIN :
                    toa.setBegin(parser.nextIntValue(0));
                    break;
                case F_END :
                    toa.setEnd(parser.nextIntValue(0));
                    break;
            }
        }
    }
}
