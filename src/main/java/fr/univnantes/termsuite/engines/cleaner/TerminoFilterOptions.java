package fr.univnantes.termsuite.engines.cleaner;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.model.TermProperty;

public class TerminoFilterOptions  {
	public static enum FilterType{THRESHOLD, TOP_N};

	private boolean enabled;
	
	@JsonProperty("type")
	private FilterType filterType = FilterType.THRESHOLD;
	
	@JsonProperty("property")
	private TermProperty filterProperty = TermProperty.FREQUENCY;

	@JsonProperty("top-n")
	private int topN = 500;

	@JsonProperty("threshold")
	private Number threshold = 2.0;
	
	@JsonProperty("keep-variants")
	private boolean keepVariants = false;
	
	@JsonProperty("max-variant-num")
	private int maxVariantNum = 25;

	public int getMaxVariantNum() {
		return maxVariantNum;
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
	
	public TerminoFilterOptions setMaxVariantNum(int maxNumberOfVariants) {
		this.maxVariantNum = maxNumberOfVariants;
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

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
