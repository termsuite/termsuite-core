package fr.univnantes.termsuite.engines.splitter;

public class MorphologicalOptions {

	private double alpha;
	private double beta;
	private double gamma;
	private double delta;
	
	private int minComponentSize;
	private int maxNumberOfComponents;
	
	private double scoreThreshold;
	private double segmentSimilarityThreshold;
	
	private boolean prefixSplitterEnabled = true;
	private boolean derivationDetecterEnabled = true;
	private boolean nativeSplittingEnabled = true;
	
	public double getAlpha() {
		return alpha;
	}
	public double getBeta() {
		return beta;
	}
	public double getGamma() {
		return gamma;
	}
	public double getDelta() {
		return delta;
	}
	public int getMinComponentSize() {
		return minComponentSize;
	}
	public int getMaxNumberOfComponents() {
		return maxNumberOfComponents;
	}
	public double getScoreThreshold() {
		return scoreThreshold;
	}
	public double getSegmentSimilarityThreshold() {
		return segmentSimilarityThreshold;
	}

	public MorphologicalOptions setAlpha(double alpha) {
		this.alpha = alpha;
		return this;
	}

	public MorphologicalOptions setBeta(double beta) {
		this.beta = beta;
		return this;
	}

	public MorphologicalOptions setGamma(double gamma) {
		this.gamma = gamma;
		return this;
	}

	public MorphologicalOptions setDelta(double delta) {
		this.delta = delta;
		return this;
	}

	public MorphologicalOptions setMinComponentSize(int minComponentSize) {
		this.minComponentSize = minComponentSize;
		return this;
	}

	public MorphologicalOptions setMaxNumberOfComponents(int maxNumberOfComponents) {
		this.maxNumberOfComponents = maxNumberOfComponents;
		return this;
	}

	public MorphologicalOptions setScoreThreshold(double scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
		return this;
	}
	
	public MorphologicalOptions setSegmentSimilarityThreshold(double segmentSimilarityThreshold) {
		this.segmentSimilarityThreshold = segmentSimilarityThreshold;
		return this;
	}
	public boolean isPrefixSplitterEnabled() {
		return prefixSplitterEnabled;
	}
	
	public MorphologicalOptions setPrefixSplitterEnabled(boolean prefixSplitterEnabled) {
		this.prefixSplitterEnabled = prefixSplitterEnabled;
		return this;
	}
	
	public MorphologicalOptions setDerivationDetecterEnabled(boolean derivationDetecterEnabled) {
		this.derivationDetecterEnabled = derivationDetecterEnabled;
		return this;
	}
	
	public boolean isDerivationDetecterEnabled() {
		return this.derivationDetecterEnabled;
	}
	
	public MorphologicalOptions setNativeSplittingEnabled(boolean nativeSplittingEnabled) {
		this.nativeSplittingEnabled = nativeSplittingEnabled;
		return this;
	}
	
	public boolean isNativeSplittingEnabled() {
		return this.nativeSplittingEnabled;
	}
}
