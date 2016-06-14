package eu.project.ttc.engines;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.ImmutableList;

import eu.project.ttc.engines.morpho.Segmentation;
import eu.project.ttc.models.Component;
import eu.project.ttc.models.Word;
import eu.project.ttc.resources.ManualMorphologyResource;
import eu.project.ttc.resources.TermIndexResource;

public class ManualMorphoSetter extends JCasAnnotator_ImplBase {
//	private static final Logger LOGGER = LoggerFactory.getLogger(ManualCompositionSetter.class);
	
	public static final String MANUAL_MORPHOLOGY_LIST = "ManualCompositionList";
	@ExternalResource(key=MANUAL_MORPHOLOGY_LIST, mandatory=true)
	private ManualMorphologyResource manualCompositions;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	private static final List<Component> EMPTY_COMPOSITION = ImmutableList.of();
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		Segmentation segmentation;
		for(Word word:termIndexResource.getTermIndex().getWords()) {
			segmentation = manualCompositions.getSegmentation(word.getLemma());
			if(segmentation != null) 
				if(segmentation.size() <= 1)
					word.setComposition(EMPTY_COMPOSITION);
		}
	}

}
