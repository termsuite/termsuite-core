package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.termsuite.framework.ConfigurationObject;
import fr.univnantes.termsuite.metrics.EditDistance;

@ConfigurationObject
public class GathererOptions {
	private boolean semanticGathererEnabled = false;
	
	private boolean derivationGathererEnabled = true;
	
	private boolean prefixationGathererEnabled = true;
	
	private boolean morphologicalGathererEnabled = true;

	private boolean graphicalGathererEnabled = true;
	
	// gathering is polynomial within a class
	private int maxClassSize = 2000;
	
	// 10 millions
	private int maxNumberOfComparisons = 1000000;
	
	private double graphicalSimilarityThreshold = 1d;

	private Class<? extends EditDistance> graphicalEditDistance;

	public boolean isSemanticGathererEnabled() {
		return semanticGathererEnabled;
	}

	public GathererOptions setGraphicalSimilarityThreshold(double graphicalSimilarityThreshold) {
		this.graphicalSimilarityThreshold = graphicalSimilarityThreshold;
		return this;
	}
	
	public double getGraphicalSimilarityThreshold() {
		return graphicalSimilarityThreshold;
	}
	
	public GathererOptions setSemanticGathererEnabled(boolean semanticGathererEnabled) {
		this.semanticGathererEnabled = semanticGathererEnabled;
		return this;
	}

	public boolean isDerivationGathererEnabled() {
		return derivationGathererEnabled;
	}

	public GathererOptions setDerivationGathererEnabled(boolean derivationGathererEnabled) {
		this.derivationGathererEnabled = derivationGathererEnabled;
		return this;
	}

	public boolean isPrefixationGathererEnabled() {
		return prefixationGathererEnabled;
	}

	public GathererOptions setPrefixationGathererEnabled(boolean prefixationGathererEnabled) {
		this.prefixationGathererEnabled = prefixationGathererEnabled;
		return this;
	}

	public boolean isMorphologicalGathererEnabled() {
		return morphologicalGathererEnabled;
	}

	public GathererOptions setMorphologicalGathererEnabled(boolean morphologicalGathererEnabled) {
		this.morphologicalGathererEnabled = morphologicalGathererEnabled;
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
	
	public GathererOptions setGraphicalGathererEnabled(boolean graphicalGathererEnabled) {
		this.graphicalGathererEnabled = graphicalGathererEnabled;
		return this;
	}
	
	public boolean isGraphicalGathererEnabled() {
		return graphicalGathererEnabled;
	}
	
	public GathererOptions setGraphicalEditDistance(Class<? extends EditDistance> editDistance) {
		this.graphicalEditDistance = editDistance;
		return this;
	}
	
	public Class<? extends EditDistance> getGraphicalEditDistance() {
		return graphicalEditDistance;
	}
}
