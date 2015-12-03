package eu.project.ttc.engines.exporter;

import java.io.File;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import eu.project.ttc.types.WordAnnotation;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by smeoni on 01/12/15.
 */

public class JsonCasExporter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonCasExporter.class);


    public static final String OUTPUT_DIRECTORY = "OutputDirectory";
    @ConfigurationParameter(name = OUTPUT_DIRECTORY, mandatory=true)
    protected String toDirectoryPath;


    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
    }
    protected String getExportFilePath(JCas cas) {
        AnnotationIndex<Annotation> index = cas.getAnnotationIndex(SourceDocumentInformation.type);
        FSIterator<Annotation> iterator = index.iterator();
        if (iterator.hasNext()) {
            SourceDocumentInformation annotation = (SourceDocumentInformation) iterator.next();
            File file = new File(annotation.getUri());
            String name = file.getName();
            int i = name.lastIndexOf('.');
            if (i == -1) {
                return name + ".json";
            } else {
                return name.substring(0, i) + ".json";
            }
        } else {
            return null;
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    /*
     *  Cette méthode est appelée par le framework UIMA
     *  pour chaque document  de ta collection (corpus).
     *
     *  Tu peux générer ton fichier compagnon dans cette méthode.
     *  (Je te donne l'astuce pour retrouver le nom et le chemin du fichier
     *  de ton corpus correspondant au CAS passé en paramètre de cette
     *  méthode plus tard)
     */

        FSIterator<Annotation> it =  aJCas.getAnnotationIndex().iterator();
        Annotation a;
        JsonFactory jsonFactory = new JsonFactory();
        String name = this.getExportFilePath(aJCas);
        try {
            FileWriter writer = new FileWriter(this.toDirectoryPath+name);
            LOGGER.debug("Writing " + this.toDirectoryPath+name);
            JsonGenerator jg = jsonFactory.createGenerator(writer);
            jg.useDefaultPrettyPrinter();
            jg.writeStartObject();
            jg.writeStringField("file",name);
            jg.writeArrayFieldStart("tag");
            while(it.hasNext()) {
                a = it.next();
                if (a instanceof WordAnnotation) {
                    jg.writeStartObject();
                    WordAnnotation wordAnno = (WordAnnotation) a;
                    jg.writeStringField("pos",wordAnno.getTag());
                    jg.writeStringField("lemma",wordAnno.getLemma());
                    jg.writeNumberField("begin",wordAnno.getBegin());
                    jg.writeNumberField("end",wordAnno.getEnd());
                    jg.writeEndObject();
                }
            }
            jg.writeEndArray();
            jg.writeEndObject();
            jg.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.warn("Failure while serializing " + name + "\nCaused by"
                    + e.getClass().getCanonicalName() + ":" + e.getMessage());
            e.printStackTrace();
        }
    }
}

