package eu.project.ttc.readers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Joiner;
import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.types.TermOccAnnotation;
import org.apache.uima.cas.*;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import java.io.*;

import static eu.project.ttc.readers.JsonCasConstants.*;

/**
 * Created by smeoni on 27/05/16.
 */
public class TermSuiteJsonCasSerializer {

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
        jg.writeEndObject();
        jg.flush();
        writer.close();
    }

    private static void writeSDI(JsonGenerator jg, JCas jCas) throws IOException {
        SourceDocumentInformation sdi =  (SourceDocumentInformation)jCas.getAnnotationIndex(SourceDocumentInformation.type).iterator().next();
        jg.writeStartObject();
        writeStringField(jg,F_URI,sdi.getUri());
        writeIntField(jg,F_OFFSET_IN_SOURCE,sdi.getOffsetInSource());
        writeIntField(jg,F_DOCUMENT_INDEX,sdi.getDocumentIndex());
        writeIntField(jg,F_NB_DOCUMENTS,sdi.getNbDocuments());
        writeIntField(jg,F_DOCUMENT_SIZE,sdi.getDocumentSize());
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
            writeOffsets(jg, toa);
            jg.writeEndObject();
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
