package fr.univnantes.termsuite.engines.gatherer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GathererOptions  {
	@JsonProperty("semantic-enabled")
	private boolean semanticEnabled = false;
	
	@JsonProperty("graphical-enabled")
	private boolean graphicalEnabled = true;
	
	@JsonProperty("graphical-similarity-th")
	private double graphicalSimilarityThreshold = 1d;

	// gathering is polynomial within a class
	@JsonProperty("max-class-size")
	private int maxClassSize = 2000;
	
	// 10 millions
	@JsonProperty("max-num-comparisons")
	private int maxNumberOfComparisons = 1000000;
	
	public boolean isSemanticEnabled() {
		return semanticEnabled;
	}

	public GathererOptions setGraphicalSimilarityThreshold(double graphicalSimilarityThreshold) {
		this.graphicalSimilarityThreshold = graphicalSimilarityThreshold;
		return this;
	}
	
	public double getGraphicalSimilarityThreshold() {
		return graphicalSimilarityThreshold;
	}
	
	public GathererOptions setSemanticEnabled(boolean semanticGathererEnabled) {
		this.semanticEnabled = semanticGathererEnabled;
		return this;
	}

	public int getMaxClassSize() {
		return maxClassSize;
	}

	public GathererOptions setMaxClassSize(int maxClassSize) {
		this.maxClassSize = maxClassSize;
		return this;
	}

	public int getMaxNumberOfComparisons() {
		return maxNumberOfComparisons;
	}

	public GathererOptions setMaxNumberOfComparisons(int maxNumberOfComparisons) {
		this.maxNumberOfComparisons = maxNumberOfComparisons;
		return this;
	}
	
	public GathererOptions setGraphicalEnabled(boolean graphicalGathererEnabled) {
		this.graphicalEnabled = graphicalGathererEnabled;
		return this;
	}
	
	public boolean isGraphicalEnabled() {
		return graphicalEnabled;
	}
}
