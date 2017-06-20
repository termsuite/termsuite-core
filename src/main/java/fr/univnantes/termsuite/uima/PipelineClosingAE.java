package fr.univnantes.termsuite.uima;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import fr.univnantes.termsuite.api.PipelineListener;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.utils.JCasUtils;

public class PipelineClosingAE extends JCasAnnotator_ImplBase {

	public static final String PIPELINE_ID = "PipelineId";
	@ConfigurationParameter(name = PIPELINE_ID)
	private String pipelineId;
	
	private PipelineListener listener;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.listener = PipelineResourceMgrs
				.getResourceMgr(pipelineId)
				.getResource(PipelineListener.class);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		SourceDocumentInformation sdi = JCasUtils.getSourceDocumentAnnotation(aJCas).get();
		double progress = 
				sdi.getCorpusSize() > 0 ?
					(double)sdi.getCumulatedDocumentSize() / sdi.getCorpusSize() 
					: (double)sdi.getDocumentIndex() / sdi.getNbDocuments() ;
	
		this.listener.statusUpdated(progress, String.format(
			"Processing document %d/%d [.2f%] %s",
			sdi.getDocumentIndex(),
			sdi.getNbDocuments(),
			progress,
			sdi.getUri()));
	}
}
