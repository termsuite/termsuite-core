package eu.project.ttc.readers;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by smeoni on 26/05/16.
 */
public class JsonCollectionReader extends AbstractTermSuiteCollectionReader{

    @Override
    protected void fillCas(CAS cas, File file) throws IOException, CollectionException {
        TermSuiteJsonCasDeserializer.deserialize(new FileInputStream(file), cas);
    }
    @Override
    protected String getDocumentText(String uri, String encoding) throws IOException {
        throw new IllegalStateException("AbstractTermSuiteCollectionReader#getDocumentText() Should not be invoked on this Reader.");
    }
}