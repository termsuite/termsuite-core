package eu.project.ttc.engines.morpho;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.TermIndexResource;

public class PrefixSplitter extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(PrefixSplitter.class);

	public static final String TASK_NAME = "Morphosyntactic analysis (prefix)";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	
	

}
