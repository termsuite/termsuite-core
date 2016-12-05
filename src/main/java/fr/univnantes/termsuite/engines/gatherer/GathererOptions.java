package fr.univnantes.termsuite.engines.gatherer;

public class GathererOptions {
	private boolean semanticGathererEnabled = false;
	
	private boolean derivationGathererEnabled = true;
	
	private boolean prefixationGathererEnabled = true;
	
	private boolean morphologicalGathererEnabled = true;
	
	// gathering is polynomial within a class
	private int maxClassSize = 2000;
	
	// 10 millions
	private int maxNumberOfComparisons = 1000000;

	public boolean isSemanticGathererEnabled() {
		return semanticGathererEnabled;
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
}
