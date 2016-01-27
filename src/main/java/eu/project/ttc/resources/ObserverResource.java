package eu.project.ttc.resources;

import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import eu.project.ttc.tools.TermSuiteResourceManager;
import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.utils.JCasUtils;

/**
 * An AE class intended to be placed in the pipeline before 
 * and after each AE's #process and #collectionCompleteProcess
 * methods in order to report progress and status to listeners. 
 * 
 * @author Damien Cram
 *
 */
public class ObserverResource implements SharedResourceObject {

	public static final String OBSERVER = "Observer";
	private static final String MSG_PROCESS = "Analyzing the corpus, document %s (%d/%d) ";

	private TermSuitePipelineObserver observer;
	private Map<String, SubTaskObserver> taskObservers = Maps.newHashMap();
	
	private int ccWorked = 0;

	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		this.observer = (TermSuitePipelineObserver)TermSuiteResourceManager
				.getInstance().get(aData.getUri().toString());

	}

	public void statusProcessProgress(JCas aJCas) {
		Optional<SourceDocumentInformation> sda = JCasUtils.getSourceDocumentAnnotation(aJCas);
		if(sda.isPresent()) {
			observer.status(
					((double)sda.get().getCumulatedDocumentSize())/sda.get().getCorpusSize(),
					0,
					String.format(MSG_PROCESS,
							sda.get().getUri(),
							sda.get().getDocumentIndex(),
							sda.get().getNbDocuments()
							));
		}
	}

	private void reportCollectionCompleteProgress(SubTaskObserver taskObserver, String statusline) {
		double normalizedTaskWeight = (double)taskObserver.taskWeight / getCCWeightSum();
		double ccProgress = (double)ccWorked/getCCWeightSum();
		observer.status(1d, ccProgress + normalizedTaskWeight * taskObserver.getRelativeProgress(), statusline);
	}

	public class SubTaskObserver {
		private String taskName;
		private long totalTaskWork;
		private int taskWeight;
		private long worked = 0;
		
		public SubTaskObserver(String taskName, int taskWeight) {
			super();
			this.taskName = taskName;
			this.taskWeight = taskWeight;
		}
		
		public void setTotalTaskWork(long totalTaskWork) {
			this.totalTaskWork = totalTaskWork;
		}

		public void work(long nbUnitWorked) {
			this.worked += nbUnitWorked;
			this.report(String.format("%s - %.2f %%", this.taskName, 100*this.getRelativeProgress()));
		}
		
		public void report(String statusMsg) {
			ObserverResource.this.reportCollectionCompleteProgress(this, statusMsg);
		}

		private double getRelativeProgress() {
			return ((double)this.worked)/Math.max(1,this.totalTaskWork);
		}
		
		@Override
		public int hashCode() {
			return this.taskName.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SubTaskObserver) 
				return this.taskName.equals(((SubTaskObserver) obj).taskName);
			else
				return false;
		}

		public void finish() {
			this.worked = this.totalTaskWork;
			report(this.taskName + " finished");
			ObserverResource.this.setTaskFinished(this);
		}
	}
	
	
	public SubTaskObserver subTask(String taskName, int totalTaskWork) {
		return new SubTaskObserver(taskName, totalTaskWork);
	}

	public void setTaskFinished(SubTaskObserver subTaskObserver) {
		ccWorked += subTaskObserver.taskWeight;
	}

	private int wCC = -1;
	private int getCCWeightSum() {
		if(wCC == -1) {
			
			wCC = 0;
			for(SubTaskObserver o:taskObservers.values())
				wCC += o.taskWeight;
			
			wCC = Math.max(this.wCC, 1);
		}
		return wCC;
	}

	
	public SubTaskObserver createTask(String taskName, int weightCollectionComplete) {
		Preconditions.checkArgument(!taskObservers.containsKey(taskName),
				"Observer already exists for task %s.", taskName);
		return taskObservers.put(taskName, new SubTaskObserver(taskName, weightCollectionComplete));		
	}

	public SubTaskObserver getTaskObserver(String taskName) {
		Preconditions.checkNotNull(taskObservers.get(taskName), "Unknown task: %s", taskName);
		return taskObservers.get(taskName);
	}
}
