package fr.univnantes.termsuite.uima;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import fr.univnantes.termsuite.utils.JCasUtils;

public class PipelineListenerAE extends JCasAnnotator_ImplBase {
	public static final String PIPELINE_ID = "PipelineId";
	@ConfigurationParameter(name = PIPELINE_ID)
	private String pipelineId;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if(JCasUtils.getSourceDocumentAnnotation(aJCas).get().getLastSegment())
			PipelineResourceMgrs.clearPipeline(pipelineId);
	}

}
