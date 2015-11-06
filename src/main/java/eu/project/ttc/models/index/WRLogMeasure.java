package eu.project.ttc.models.index;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public class WRLogMeasure extends TermMeasure {
	
	public WRLogMeasure(TermIndex termIndex) {
		super(termIndex);
	}

	@Override
	public double getValue(Term term) {
		return Math.log10(1 + term.getFrequencyNorm() / term.getGeneralFrequencyNorm());
	}
}
