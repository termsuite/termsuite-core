package fr.univnantes.termsuite.eval.bilangaligner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.alignment.BilingualAlignmentService;
import fr.univnantes.termsuite.alignment.RequiresSize2Exception;
import fr.univnantes.termsuite.alignment.TranslationCandidate;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Term;

public enum EvaluatedMethod {
	DISTRIBUTIONAL("distrib"),
	COMPOSITIONAL("compo"),
	SEMI_DISTRIBUTIONAL("semi-distrib"),
	DICO("dico"),
	NEOCLASSICAL("neo"),
	HYBRID("hybrid");

	private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatedMethod.class);
	
	private String dirName;
	
	private EvaluatedMethod(String dirName) {
		this.dirName = dirName;
	}

	public boolean acceptPair(BilingualAlignmentService aligner, Term source, Term target) {
		try {
			switch(this) {
			case DISTRIBUTIONAL: return source.isSingleWord() && target.isSingleWord();
			case NEOCLASSICAL: return aligner.canAlignNeoclassical(source) && target.isCompound() && target.getWords().get(0).getWord().getCompoundType() == CompoundType.NEOCLASSICAL;
			case COMPOSITIONAL: return aligner.canAlignCompositional(source) && target.isMultiWord();
			case SEMI_DISTRIBUTIONAL: return aligner.canAlignSemiDistributional(source) && target.isMultiWord();
			case DICO: return source.isSingleWord() && target.isSingleWord();
			case HYBRID: return true;
			default:
				throw new UnsupportedOperationException("Method not supported for " + this);
			}
		} catch (RequiresSize2Exception e) {
			LOGGER.warn("Terms of size > 2 temporary not supported by evaluator. Term: " + source);
			return false;
		}
	}

	public List<TranslationCandidate> align(BilingualAlignmentService aligner, Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		switch(this) {
		case DISTRIBUTIONAL: return aligner.alignDistributional(sourceTerm, nbCandidates, minCandidateFrequency);
		case NEOCLASSICAL: return aligner.alignNeoclassical(sourceTerm, nbCandidates, minCandidateFrequency);
		case COMPOSITIONAL: return aligner.alignCompositional(sourceTerm, nbCandidates, minCandidateFrequency);
		case SEMI_DISTRIBUTIONAL: return aligner.alignSemiDistributional(sourceTerm, nbCandidates, minCandidateFrequency);
		case DICO: return aligner.alignDico(sourceTerm, nbCandidates);
		case HYBRID: return aligner.alignSize2(sourceTerm, nbCandidates, minCandidateFrequency);
		default:
			throw new UnsupportedOperationException("Method not supported for " + this);
		}
	}
	
	@Override
	public String toString() {
		return this.dirName;
	}
}
