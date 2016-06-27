package eu.project.ttc.engines.exporter;

import eu.project.ttc.readers.TermSuiteJsonCasSerializer;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by Simon Meoni on 02/06/16.
 */
public class TermsuiteJsonCasExporter extends CasExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonCasExporter.class);
    public static final String OUTPUT_DIRECTORY = "OutputDirectory";

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        String name = this.getExportFilePath(aJCas, "json");
        File file = new File(this.directoryFile, name);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(fos, "UTF-8");
            TermSuiteJsonCasSerializer.serialize(writer,aJCas);

        } catch (IOException e) {
            LOGGER.error("Failure while serializing " + name + "\nCaused by"
                    + e.getClass().getCanonicalName() + ":" + e.getMessage(), e);
        }
    }
}
