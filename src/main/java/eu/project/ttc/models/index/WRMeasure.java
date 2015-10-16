package eu.project.ttc.models.index;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public class WRMeasure extends TermMeasure {
	
	public WRMeasure(TermIndex termIndex) {
		super(termIndex);
	}

	@Override
	public double getValue(Term term) {
		return term.getFrequencyNorm() / term.getGeneralFrequencyNorm();
	}
}
