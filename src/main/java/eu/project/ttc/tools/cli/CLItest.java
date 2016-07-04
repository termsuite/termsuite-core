package eu.project.ttc.tools.cli;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.readers.JsonCollectionReader;
/**
 * Created by smeoni on 27/05/16.
 */
class CLItest {
    public static void main(String[] args){
        TermSuitePipeline pipeline = TermSuitePipeline.create("fr")
                .setResourcePath("/home/smeoni/Documents/termsuite-resources")
                .setCollection(TermSuiteCollection.JSON, "/home/smeoni/IdeaProjects/termsuite-core/test-RS" , "UTF-8")
                .aeTermOccAnnotationImporter()
                .aeSpecificityComputer()
                .aeCompostSplitter() // morphological analysis
                .aeSyntacticVariantGatherer()
                .aeGraphicalVariantGatherer()
                .aeThresholdCleaner(TermProperty.FREQUENCY, 5)
                .aeTermClassifier(TermProperty.FREQUENCY)
                .aeTopNCleaner(TermProperty.WR, 10000)
                .aeContextualizer(3, false)
                .haeJsonExporter("terminology.json")
                .run();
    }
}