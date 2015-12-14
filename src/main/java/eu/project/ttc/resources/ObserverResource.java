package eu.project.ttc.resources;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import com.google.common.base.Optional;

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
	
	private TermSuitePipelineObserver observer;
	
	private int wCC = 0;
	private double progressCC = 0;

	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		this.observer = (TermSuitePipelineObserver)TermSuiteResourceManager
				.getInstance().get(aData.getUri().toString());

	}

	public void initWeights(int ccWeight) {
		wCC += ccWeight;
	}

	public void statusProcess(String msgProcess, JCas aJCas) {
		Optional<SourceDocumentInformation> sda = JCasUtils.getSourceDocumentAnnotation(aJCas);
		if(sda.isPresent()) {
			report(
					((double)sda.get().getCumulatedDocumentSize())/sda.get().getCorpusSize(),
					0d,
					String.format("Analyzing the corpus, document %s (%d/%d) ",
							sda.get().getUri(),
							sda.get().getDocumentIndex(),
							sda.get().getNbDocuments()
							));
		}
	}

	private void report(double processProgress, double ccProgress, String statusline) {
		observer.status(processProgress, ccProgress, statusline);
	}

	public void statusCollectionComplete(int weight, String msgCollectionComplete) {
		if(weight == 0)
			return;
		this.progressCC += weight;
		
		report(1d, ((double) this.progressCC)/getCCWeightSum(), msgCollectionComplete);
	}

	private int getCCWeightSum() {
		return Math.max(this.wCC, 1);
	}
}
