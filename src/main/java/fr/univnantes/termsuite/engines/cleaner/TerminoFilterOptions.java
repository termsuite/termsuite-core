package fr.univnantes.termsuite.engines.cleaner;

import fr.univnantes.termsuite.model.TermProperty;

public class TerminoFilterOptions {

	public static enum FilterType{THRESHOLD, TOP_N};
	private FilterType filterType = FilterType.THRESHOLD;
	private TermProperty filterProperty = TermProperty.FREQUENCY;
	private int topN = 500;
	private Number threshold = 2.0;
	private boolean keepVariants = false;
	private int maxNumberOfVariants = 25;

	public int getMaxNumberOfVariants() {
		return maxNumberOfVariants;
	}
	
	public TerminoFilterOptions() {
		super();
	}

	public TerminoFilterOptions by(TermProperty p) {
		this.filterProperty = p;
		return this;
	}
	
	public TerminoFilterOptions keepOverTh(Number threshold) {
		this.filterType = FilterType.THRESHOLD;
		this.threshold = threshold;
		return this;
	}
	
	public TerminoFilterOptions setMaxNumberOfVariants(int maxNumberOfVariants) {
		this.maxNumberOfVariants = maxNumberOfVariants;
		return this;
	}
	
	public TerminoFilterOptions keepTopN(int n) {
		this.filterType = FilterType.TOP_N;
		this.topN = n;
		return this;
	}
	
	/**
	 * Alias for {@link #setKeepVariants(<code>true</code>)}
	 * 
	 * @return
	 */
	public TerminoFilterOptions keepVariants() {
		return setKeepVariants(true);
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

	public Number getThreshold() {
		return threshold;
	}

	public boolean isKeepVariants() {
		return keepVariants;
	}

	public TerminoFilterOptions setKeepVariants(boolean keepVariants) {
		this.keepVariants = keepVariants;
		return this;
	}
}
