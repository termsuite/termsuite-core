package eu.project.ttc.readers;

import eu.project.ttc.types.WordAnnotation;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;

import java.io.InputStream;
import java.net.URI;

/**
 * Created by smeoni on 27/05/16.
 */
public class TermSuiteJsonCasDeserializer {
    public static void deserialize(InputStream inputStream, CAS cas) {
        /**try {
            /**String uriStr;
            WordAnnotation wa = (WordAnnotation)cas.createAnnotation(cas.getJCas().getCasType(WordAnnotation.type),0,0);
            wa.setTag("sfyeorifyoe");
            wa.setLemma("sfyeorifyoe")
        } catch (CASException e) {
            e.printStackTrace();
        }**/
    }
}
