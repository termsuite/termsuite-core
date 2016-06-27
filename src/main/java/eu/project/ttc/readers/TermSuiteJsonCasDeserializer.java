package eu.project.ttc.readers;

import com.fasterxml.jackson.core.*;
import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.NavigableMap;;
import java.util.TreeMap;

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

            SourceDocumentInformation sdi = (SourceDocumentInformation) cas.createAnnotation(cas.getJCas().getCasType(SourceDocumentInformation.type), 0, 0);
            WordAnnotation wa = (WordAnnotation) cas.createAnnotation(cas.getJCas().getCasType(WordAnnotation.type), 0, 0);
            TermOccAnnotation toa = (TermOccAnnotation) cas.createAnnotation(cas.getJCas().getCasType(TermOccAnnotation.type), 0, 0);
            NavigableMap<Integer, WordAnnotation> waTreeMap = new TreeMap<>();
            boolean inSdi = false;
            boolean inWa = false;
            boolean inToa = false;
            boolean inCoveredText = false;

            while ((token=parser.nextToken()) != null)
            {
                try{

                    if (inSdi){

                        if (token == JsonToken.END_OBJECT) {
                            inSdi = false;
                        }
                        else {
                            FillSdi(parser,token,sdi);
                        }
                    }

                    else if (inWa){
                        if (token == JsonToken.END_ARRAY) {
                            inWa = false;
                        }
                        else if (token == JsonToken.END_OBJECT) {
                            waTreeMap.put(wa.getBegin(),(WordAnnotation) wa.clone());
                            wa.addToIndexes();
                            wa = (WordAnnotation) cas.createAnnotation(cas.getJCas().getCasType(WordAnnotation.type), 0, 0);
                        }
                        FillWordAnnotations(parser, token, wa);
                    }

                    else if (inToa){
                        if (token == JsonToken.END_ARRAY) {
                            inToa = false;
                        }
                        else if (token == JsonToken.END_OBJECT) {
                            FillWords(toa,waTreeMap,cas);
                            toa.addToIndexes();
                            toa = (TermOccAnnotation) cas.createAnnotation(cas.getJCas().getCasType(TermOccAnnotation.type), 0, 0);
                        }
                        FillTermOccAnnotations(parser, token, toa, cas);
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
                    else if ("covered_text".equals(parser.getParsingContext().getCurrentName())) {
                        inCoveredText = true;
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

    private static void FillWords(TermOccAnnotation toa, NavigableMap<Integer, WordAnnotation> waTreeMap, CAS cas) throws CASException {
        int begin = toa.getBegin();
        int end = toa.getEnd();
        Collection<WordAnnotation> subWaCollection = waTreeMap.subMap(begin,true,end,true).values();
        WordAnnotation[] waArray = subWaCollection.toArray(new WordAnnotation[subWaCollection.size()]);
        FSArray fs = (FSArray) cas.createArrayFS(subWaCollection.size());
        for (int i = 0; i < waArray.length; i++){
            fs.set(i,(WordAnnotation) waArray[i].clone());
        }
        toa.setWords(fs);
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
