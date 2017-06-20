package fr.univnantes.termsuite.uima;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.utils.JCasUtils;

public class PipelineListenerAE extends JCasAnnotator_ImplBase {
	public static final String PIPELINE_ID = "PipelineId";
	@ConfigurationParameter(name = PIPELINE_ID)
	private String pipelineId;

	private AtomicInteger index;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		index = new AtomicInteger(0);
	}
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		int currentIndex = index.incrementAndGet();
		PipelineListener listener = PipelineResourceMgrs.getResourceMgr(pipelineId).getResourceOrNull(PipelineListener.class);
		SourceDocumentInformation sdi = JCasUtils.getSourceDocumentAnnotation(aJCas).get();
		if(listener != null) {
			double progress = 0d;
			if(sdi.getNbDocuments() != -1 && sdi.getNbDocuments() != 0)
				progress = (double)currentIndex/sdi.getNbDocuments();
			else if(sdi.getDocumentSize() != -1 && sdi.getCorpusSize() != -1 && sdi.getCorpusSize() != 0)
				progress = (double)sdi.getDocumentSize()/sdi.getCorpusSize();
				
			listener.statusUpdated(
					progress, 
					String.format("Processing document %s", sdi.getUri()));
		}
		if(sdi.getLastSegment())
			PipelineResourceMgrs.clearPipeline(pipelineId);
	}

}
