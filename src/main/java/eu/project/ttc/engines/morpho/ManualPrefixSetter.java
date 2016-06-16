package eu.project.ttc.engines.morpho;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.resources.ManualSegmentationResource;
import eu.project.ttc.resources.TermIndexResource;

public class ManualPrefixSetter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ManualPrefixSetter.class);
	
	public static final String PREFIX_EXCEPTIONS = "PrefixExceptions";
	@ExternalResource(key=PREFIX_EXCEPTIONS, mandatory=true)
	private ManualSegmentationResource prefixExceptions;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		Segmentation segmentation;
		for(Term swt:termIndexResource.getTermIndex().getTerms()) {
			if(!swt.isSingleWord())
				continue;
			Word word = swt.getWords().get(0).getWord();
			segmentation = prefixExceptions.getSegmentation(word.getLemma());
			if(segmentation != null) 
				if(segmentation.size() <= 1) {
					for(TermVariation tv:swt.getVariations(VariationType.IS_PREFIX_OF))
						swt.removeTermVariation(tv);
				} else {
					LOGGER.warn("Ignoring prefix exception {}->{} since non-expty prefix exceptions are not allowed.",
							word.getLemma(),
							segmentation);
				}
		}
	}

}
