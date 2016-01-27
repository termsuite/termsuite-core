package eu.project.ttc.engines;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.ObserverResource.SubTaskObserver;

public class PipelineObserver extends JCasAnnotator_ImplBase {
	public static final String TASK_STARTED = "taskStarted";
	public static final String TASK_ENDED = "taskEnded";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;
	
	public static final String WEIGHT = "Weight";
	@ConfigurationParameter(name=WEIGHT, mandatory=false, defaultValue="1")
	private int weightCollectionComplete;

	public static final String HOOK = "Hook";
	@ConfigurationParameter(name=HOOK, mandatory=true)
	private String hook;

	public static final String TASK_NAME = "TaskName";
	@ConfigurationParameter(name=TASK_NAME, mandatory=true)
	private String taskName;

	private SubTaskObserver taskObserver;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		switch(hook) {
		case TASK_STARTED:
			taskObserver = observerResource.createTask(taskName, weightCollectionComplete);
			break;
		case TASK_ENDED:
			taskObserver = observerResource.getTaskObserver(taskName);
			break;
		default:
			throw new IllegalArgumentException("Unkown task hook" + hook);
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		this.observerResource.statusProcessProgress(aJCas);
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		if(hook.equals(TASK_ENDED))
			taskObserver.finish();
	}
}
