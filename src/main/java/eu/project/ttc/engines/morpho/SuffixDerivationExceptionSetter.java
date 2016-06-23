package eu.project.ttc.engines.morpho;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.Lists;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.resources.TermIndexResource;
import fr.univnantes.julestar.uima.resources.MultimapFlatResource;

public class SuffixDerivationExceptionSetter extends JCasAnnotator_ImplBase {
	public static final String SUFFIX_DERIVATION_EXCEPTION = "SuffixDerivationExceptions";
	@ExternalResource(key=SUFFIX_DERIVATION_EXCEPTION, mandatory=true)
	private MultimapFlatResource exceptionsByDerivateExceptionForms;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		Term regularForm;
		for(Term derivateForm:termIndexResource.getTermIndex().getTerms()) {
			if(!derivateForm.isSingleWord())
				continue;
			List<TermVariation> toRem = Lists.newArrayList();
			for(String regularFormException:exceptionsByDerivateExceptionForms.getValues(derivateForm.getWords().get(0).getWord().getLemma())) {
				for(TermVariation tv:derivateForm.getBases(VariationType.DERIVES_INTO)) {
					regularForm = tv.getBase();
					if(regularForm.getWords().get(0).getWord().getLemma().equals(regularFormException)) 
						toRem.add(tv);
				}
			}
			for(TermVariation rem:toRem) {
				rem.getBase().removeTermVariation(rem);
			}
		}
	}
}
