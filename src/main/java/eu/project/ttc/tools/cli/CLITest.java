package eu.project.ttc.tools.cli;

import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.readers.JsonCollectionReader;
/**
 * Created by smeoni on 27/05/16.
 */
class TermSuiteTest {
    public static void main(String[] args){
        TermSuitePipeline pipeline = TermSuitePipeline.create("fr")
                .setResourcePath("/home/smeoni/partage-Termith/scripts/logiciels/termsuite-resources")
                .setCollection(TermSuiteCollection.JSON, "/home/smeoni/IdeaProjects/termsuite-core/test" , "UTF-8")
                .haeTermsuiteJsonCasExporter("test2")
                .run();
    }
}
