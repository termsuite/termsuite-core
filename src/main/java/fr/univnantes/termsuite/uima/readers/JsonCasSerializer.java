
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

import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_BEGIN;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_CASE;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_CATEGORY;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_CORPUS_SIZE;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_CUMULATED_DOCUMENT_SIZE;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_DEGREE;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_DOCUMENT_INDEX;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_DOCUMENT_SIZE;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_END;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_FIXED_EXPRESSIONS;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_FORMATION;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_GENDER;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_LABELS;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_LAST_SEGMENT;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_LEMMA;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_MOOD;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_NB_DOCUMENTS;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_NUMBER;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_OFFSET_IN_SOURCE;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_PATTERN;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_PERSON;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_REGEX_LABEL;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_SDI;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_SPOTTING_RULE_NAME;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_STEM;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_SUB_CATEGORY;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_TAG;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_TENSE;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_TERM_KEY;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_TERM_OCC_ANNOTATIONS;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_TEXT;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_URI;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_WORDS;
import static fr.univnantes.termsuite.uima.readers.JsonCasConstants.F_WORD_ANNOTATIONS;

import java.io.IOException;
import java.io.Writer;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Joiner;

import fr.univnantes.termsuite.types.FixedExpression;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.types.WordAnnotation;

/**
 * Created by smeoni on 27/05/16.
 */
public class JsonCasSerializer {

    public static void serialize(Writer writer, JCas jCas) throws IOException {

        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator jg = jsonFactory.createGenerator(writer);
        jg.useDefaultPrettyPrinter();
        jg.writeStartObject();
        jg.writeFieldName(F_SDI);
        writeSDI(jg, jCas);
        jg.writeFieldName(F_WORD_ANNOTATIONS);
        writeWordAnnotations(jg, jCas);
        jg.writeFieldName(F_TERM_OCC_ANNOTATIONS);
        writeTermOccAnnotations(jg, jCas);
        jg.writeFieldName(F_FIXED_EXPRESSIONS);
        writeFixedExpressions(jg, jCas);
        writeCoveredText(jg, jCas);
        jg.writeEndObject();
        jg.flush();
        writer.close();
    }

    private static void writeCoveredText(JsonGenerator jg, JCas jCas) throws IOException {
        String text = jCas.getDocumentText();
        writeStringField(jg,F_TEXT,text);
    }

    private static void writeSDI(JsonGenerator jg, JCas jCas) throws IOException {
        SourceDocumentInformation sdi =  (SourceDocumentInformation)jCas.getAnnotationIndex(SourceDocumentInformation.type).iterator().next();
        jg.writeStartObject();
        writeStringField(jg,F_URI,sdi.getUri());
        writeIntField(jg,F_OFFSET_IN_SOURCE,sdi.getOffsetInSource());
        writeIntField(jg,F_DOCUMENT_INDEX,sdi.getDocumentIndex());
        writeIntField(jg,F_NB_DOCUMENTS,sdi.getNbDocuments());
        writeLongField(jg,F_DOCUMENT_SIZE,sdi.getDocumentSize());
        writeLongField(jg,F_CUMULATED_DOCUMENT_SIZE,sdi.getCumulatedDocumentSize());
        writeLongField(jg,F_CORPUS_SIZE,sdi.getCorpusSize());
        writeBooleanField(jg,F_LAST_SEGMENT,sdi.getLastSegment());
        writeOffsets(jg, sdi);
        jg.writeEndObject();
    }
    private static void writeWordAnnotations(JsonGenerator jg, JCas jCas) throws IOException {
        jg.writeStartArray();
        FSIterator<Annotation> it = jCas.getAnnotationIndex(WordAnnotation.type).iterator();
        while(it.hasNext()) {
            WordAnnotation wa = (WordAnnotation) it.next();
            jg.writeStartObject();
            writeStringField(jg,F_CATEGORY, wa.getCategory());
            writeStringField(jg,F_LEMMA, wa.getLemma());
            writeStringField(jg,F_STEM, wa.getStem());
            writeStringField(jg,F_TAG, wa.getTag());
            writeStringField(jg,F_SUB_CATEGORY, wa.getSubCategory());
            writeStringField(jg,F_REGEX_LABEL, wa.getRegexLabel());
            writeStringField(jg,F_NUMBER, wa.getNumber());
            writeStringField(jg,F_GENDER, wa.getGender());
            writeStringField(jg,F_CASE, wa.getCase());
            writeStringField(jg,F_MOOD, wa.getMood());
            writeStringField(jg,F_TENSE, wa.getTense());
            writeStringField(jg,F_PERSON, wa.getPerson());
            writeStringField(jg,F_DEGREE, wa.getDegree());
            writeStringField(jg,F_FORMATION, wa.getFormation());
            writeStringField(jg,F_LABELS, wa.getLabels());
            writeOffsets(jg, wa);
            jg.writeEndObject();
        }
        jg.writeEndArray();
    }

    private static void writeOffsets(JsonGenerator jg, Annotation a) throws IOException {
        writeIntField(jg,F_BEGIN, a.getBegin());
        writeIntField(jg,F_END, a.getEnd());
    }

    private static void writeTermOccAnnotations(JsonGenerator jg, JCas jCas) throws IOException {
        jg.writeStartArray();
        FSIterator<Annotation> it = jCas.getAnnotationIndex(TermOccAnnotation.type).iterator();
        while(it.hasNext()) {
             TermOccAnnotation toa = (TermOccAnnotation) it.next();
            jg.writeStartObject();
            writeStringFSArrayField(jg,F_PATTERN, toa.getPattern());
            writeStringField(jg,F_SPOTTING_RULE_NAME, toa.getSpottingRuleName());
            writeStringField(jg,F_TERM_KEY, toa.getTermKey());
            writeIntFSArrayField(jg,F_WORDS,toa.getWords());
            writeOffsets(jg, toa);
            jg.writeEndObject();
        }
        jg.writeEndArray();
    }

    private static void writeFixedExpressions(JsonGenerator jg, JCas jCas) throws IOException {
        jg.writeStartArray();
        FSIterator<Annotation> it = jCas.getAnnotationIndex(FixedExpression.type).iterator();
        while(it.hasNext()) {
            FixedExpression fe = (FixedExpression) it.next();
            jg.writeStartObject();
            writeOffsets(jg, fe);
            jg.writeEndObject();
        }
        jg.writeEndArray();
    }

    private static void writeIntFSArrayField(JsonGenerator jg, String fieldName, FSArray words) throws IOException {
        if(words == null)
            return;
        jg.writeArrayFieldStart(fieldName);

        for (int i = 0; i < words.size(); i++){
            WordAnnotation wa = (WordAnnotation) words.get(i);
            jg.writeStartArray();
            jg.writeNumber(wa.getBegin());
            jg.writeNumber(wa.getEnd());
            jg.writeEndArray();
        }
        jg.writeEndArray();
    }

    private static void writeIntField(JsonGenerator jg, String fieldName, Integer value) throws IOException {
        writeLongField(jg, fieldName, value.longValue());
    }

    private static void writeLongField(JsonGenerator jg, String fieldName, Long value) throws IOException {
        if(value == null)
            return;
        jg.writeNumberField(fieldName, value);
    }

    private static void writeStringFSArrayField(JsonGenerator jg, String fieldName, StringArray value) throws IOException {
        if(value == null)
            return;
        jg.writeStringField(fieldName, Joiner.on(" ").join(value.toArray()));
    }

    private static void writeBooleanField(JsonGenerator jg, String fieldName, Boolean value) throws IOException {
        if(value == null)
            return;
        jg.writeBooleanField(fieldName, value);
    }
    private static void writeStringField(JsonGenerator jg, String fieldName, String value) throws IOException {
        if(value == null)
            return;
        jg.writeStringField(fieldName, value);
    }

}
