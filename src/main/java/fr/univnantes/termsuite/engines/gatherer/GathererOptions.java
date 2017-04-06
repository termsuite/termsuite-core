package fr.univnantes.termsuite.engines.gatherer;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.metrics.Cosine;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.utils.JsonConfigObject;

public class GathererOptions  extends JsonConfigObject  {

	@JsonProperty("enabled")
	private boolean enabled = true;

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

	@JsonProperty("merger-enabled")
	private boolean mergerEnabled = true;

	@JsonProperty("semantic-dico-only")
	private boolean semanticDicoOnly = false;

	@JsonProperty("semantic-similarity-th")
	private double semanticSimilarityThreshold = 0.3;
	
	@JsonProperty("semantic-nb-candidates")
	private int semanticNbCandidates = 5;

	@JsonProperty("semantic-similarity-distance")
	private Class<? extends SimilarityDistance> semanticSimilarityDistance = Cosine.class;
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof GathererOptions) {
			GathererOptions o = (GathererOptions)obj;
			return semanticEnabled == o.semanticEnabled
					&& graphicalEnabled == o.graphicalEnabled
					&& graphicalSimilarityThreshold == o.graphicalSimilarityThreshold
					&& maxClassSize == o.maxClassSize
					&& maxNumberOfComparisons == o.maxNumberOfComparisons
					&& mergerEnabled == o.mergerEnabled
					&& semanticSimilarityThreshold == o.semanticSimilarityThreshold
					&& semanticNbCandidates == o.semanticNbCandidates
					&& semanticSimilarityDistance.equals(o.semanticSimilarityDistance)
					;
		} else return false;
	}

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

	public GathererOptions setMergerEnabled(boolean mergerEnabled) {
		this.mergerEnabled = mergerEnabled;
		return this;
	}
	
	public boolean isTermMergerEnabled() {
		return this.mergerEnabled;
	}

	public double getSemanticSimilarityThreshold() {
		return semanticSimilarityThreshold;
	}

	public GathererOptions setSemanticSimilarityThreshold(double semanticSimilarityThreshold) {
		this.semanticSimilarityThreshold = semanticSimilarityThreshold;
		return this;
	}

	public int getSemanticNbCandidates() {
		return semanticNbCandidates;
	}

	public GathererOptions setSemanticNbCandidates(int semanticNbCandidates) {
		this.semanticNbCandidates = semanticNbCandidates;
		return this;
	}

	public Class<? extends SimilarityDistance> getSemanticSimilarityDistance() {
		return semanticSimilarityDistance;
	}

	public GathererOptions setSemanticSimilarityDistance(Class<? extends SimilarityDistance> semanticSimilarityDistance) {
		this.semanticSimilarityDistance = semanticSimilarityDistance;
		return this;
	}

	public boolean isMergerEnabled() {
		return mergerEnabled;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setSemanticDicoOnly(boolean semanticDicoOnly) {
		this.semanticDicoOnly = semanticDicoOnly;
	}
	
	public boolean isSemanticDicoOnly() {
		return semanticDicoOnly;
	}
}
