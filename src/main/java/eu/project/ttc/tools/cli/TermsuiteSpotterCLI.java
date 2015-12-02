package eu.project.ttc.tools.cli;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.tools.TermSuitePipeline;

import java.io.IOException;
/**
 * Created by smeoni on 26/11/15.
 */
public class TermsuiteSpotterCLI {

    public static void main(String[] args) throws IOException {

        TermSuitePipeline pipeline = TermSuitePipeline.create("fr")
                .setResourcePath("/home/smeoni/IdeaProjects/termsuite-core/src/main/resources/termsuite-resources.jar")
                .setCollection(
                        TermSuiteCollection.TXT,
                        "/home/smeoni/partage-Termith/disciplines/psychologie/phase7/", "UTF-8")
                .aeWordTokenizer()
                .setTreeTaggerHome("/home/smeoni/partage-Termith/scripts/logiciels/treetagger/")
                .aeTreeTagger()
                .aeUrlFilter()
                .aeStemmer()
                .aeRegexSpotter()
                .haeJsonCasExporter("/home/smeoni/Documents/test/test-Termsuite2/")
                .aeStopWordsFilter()
                .aeSpecificityComputer()
                .aeCompostSplitter() // morphological analysis
                .aeSyntacticVariantGatherer()
                .aeGraphicalVariantGatherer()
                .aeTermClassifier(TermProperty.FREQUENCY)
                .haeJsonExporter("terminology.json")
                .run();
        }

    }

