package fr.univnantes.termsuite.api;

import fr.univnantes.termsuite.model.TermProperty;

public class TerminoFilterConfig {

	public static enum FilterType{THRESHHOLD, TOP_N};
	private FilterType filterType = FilterType.THRESHHOLD;
	private TermProperty filterProperty = TermProperty.FREQUENCY;
	private int topN = 500;
	private double threshhold = 2.0;
	private boolean keepVariants = false;

	public TerminoFilterConfig() {
		super();
	}

	public TerminoFilterConfig by(TermProperty p) {
		this.filterProperty = p;
		return this;
	}
	
	public TerminoFilterConfig keepOverTh(double threshhold) {
		this.filterType = FilterType.THRESHHOLD;
		this.threshhold = threshhold;
		return this;
	}
	
	public TerminoFilterConfig keepTopN(int n) {
		this.filterType = FilterType.TOP_N;
		this.topN = n;
		return this;
	}
	
	public TerminoFilterConfig keepVariants() {
		this.keepVariants = true;	
		return this;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public TermProperty getFilterProperty() {
		return filterProperty;
	}

	public int getTopN() {
		return topN;
	}

	public double getThreshhold() {
		return threshhold;
	}

	public boolean isKeepVariants() {
		return keepVariants;
	}
}
