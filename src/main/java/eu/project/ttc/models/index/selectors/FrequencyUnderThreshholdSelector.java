package eu.project.ttc.models.index.selectors;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public class FrequencyUnderThreshholdSelector extends AbstractTermSelector {
	
	private int threshhold;

	public FrequencyUnderThreshholdSelector(int threshhold) {
		super(false);
		this.threshhold = threshhold;
	}
	
	public int getThreshhold() {
		return threshhold;
	}

	@Override
	public boolean select(TermIndex termIndex, Term t) {
		return t.getFrequency() < this.threshhold;
	}
}
