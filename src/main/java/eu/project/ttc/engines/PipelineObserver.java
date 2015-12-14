package eu.project.ttc.engines;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import eu.project.ttc.resources.ObserverResource;

public class PipelineObserver extends JCasAnnotator_ImplBase {

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;
	
	public static final String WEIGHT_COLLECTION_COMPLETE = "WeightCollectionComplete";
	@ConfigurationParameter(name=WEIGHT_COLLECTION_COMPLETE, mandatory=false, defaultValue="1")
	private int weightCollectionComplete;

	public static final String MSG_PROCESS = "MessageProcess";
	@ConfigurationParameter(name=MSG_PROCESS, mandatory=false)
	private String msgProcess;

	public static final String MSG_COLLECTION_COMPLETE = "MessageCollectionComplete";
	@ConfigurationParameter(name=MSG_COLLECTION_COMPLETE, mandatory=true)
	private String msgCollectionComplete;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		this.observerResource.initWeights(weightCollectionComplete);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		this.observerResource.statusProcess(msgProcess, aJCas);
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		this.observerResource.statusCollectionComplete(weightCollectionComplete, msgCollectionComplete);
	}
}
