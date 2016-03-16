package eu.project.ttc.models;

public class FrequencyUnderThreshholdSelector implements TermSelector {
	
	private int threshhold;

	public FrequencyUnderThreshholdSelector(int threshhold) {
		super();
		this.threshhold = threshhold;
	}
	
	public int getThreshhold() {
		return threshhold;
	}

	@Override
	public boolean select(Term t) {
		return t.getFrequency() < this.threshhold;
	}
}
