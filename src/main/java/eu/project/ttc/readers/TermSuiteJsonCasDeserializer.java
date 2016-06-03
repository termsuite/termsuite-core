package eu.project.ttc.readers;

import com.fasterxml.jackson.core.*;
import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.WordAnnotation;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;

import java.io.IOException;
import java.io.InputStream;

import static eu.project.ttc.readers.JsonCasConstants.*;

/**
 * Created by smeoni on 27/05/16.
 */
public class TermSuiteJsonCasDeserializer {
    public static void deserialize(InputStream inputStream, CAS cas) {
        try {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(inputStream);
            JsonToken token;
            JsonStreamContext ancestor;
            JsonStreamContext parent;

            SourceDocumentInformation sdi = (SourceDocumentInformation) cas.createAnnotation(cas.getJCas().getCasType(SourceDocumentInformation.type), 0, 0);
            WordAnnotation wa = (WordAnnotation) cas.createAnnotation(cas.getJCas().getCasType(WordAnnotation.type), 0, 0);
            boolean inSdi = false;
            boolean inWa = false;

            while ((token=parser.nextToken()) != null)
            {
                try{
                    parent = parser.getParsingContext().getParent();

                    if (inSdi){

                        if (token == JsonToken.END_OBJECT)
                            inSdi = false;
                        else
                            FillSdi(parser,token,sdi);
                    }

                    else if (inWa){
                        if (token == JsonToken.END_ARRAY)
                            inWa = false;
                        else if (token == JsonToken.END_OBJECT) {
                            wa.addToIndexes();
                            wa = (WordAnnotation) cas.createAnnotation(cas.getJCas().getCasType(WordAnnotation.type), 0, 0);
                        }
                        FillWordAnnotations(parser, token, wa);
                    }

                    else if ("sdi".equals(parser.getParsingContext().getCurrentName())) {
                        inSdi = true;
                    }

                    else if ("word_annotations".equals(parser.getParsingContext().getCurrentName())) {
                        inWa = true;
                    }

                    else if ("term_occ_annotations".equals(parser.getParsingContext().getCurrentName())) {
                        break;
                    }
                }
                catch (java.lang.NullPointerException e) {
                    e.printStackTrace();
                    System.out.print(parser.getParsingContext().getCurrentName());
                }
            }
            sdi.addToIndexes();
        } catch (IOException | CASException e) {
            e.printStackTrace();
        }
    }
    private static void FillWordAnnotations(JsonParser parser, JsonToken token, WordAnnotation wa) throws IOException {
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
                    wa.setDegree(parser.nextTextValue());
                    break;
                case F_LABELS :
                    wa.setDegree(parser.nextTextValue());
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

    private static void FillSdi(JsonParser parser , JsonToken token, SourceDocumentInformation sdi) throws IOException {
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
}
