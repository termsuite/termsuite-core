package fr.univnantes.termsuite.engines.morpho;

public class NativeSplitterOptions {

	private double alpha;
	private double beta;
	private double gamma;
	private double delta;
	
	private int minComponentSize;
	private int maxNumberOfComponents;
	
	private double scoreThreshold;
	private double segmentSimilarityThreshold;
	
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

	public NativeSplitterOptions setAlpha(double alpha) {
		this.alpha = alpha;
		return this;
	}

	public NativeSplitterOptions setBeta(double beta) {
		this.beta = beta;
		return this;
	}

	public NativeSplitterOptions setGamma(double gamma) {
		this.gamma = gamma;
		return this;
	}

	public NativeSplitterOptions setDelta(double delta) {
		this.delta = delta;
		return this;
	}

	public NativeSplitterOptions setMinComponentSize(int minComponentSize) {
		this.minComponentSize = minComponentSize;
		return this;
	}

	public NativeSplitterOptions setMaxNumberOfComponents(int maxNumberOfComponents) {
		this.maxNumberOfComponents = maxNumberOfComponents;
		return this;
	}

	public NativeSplitterOptions setScoreThreshold(double scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
		return this;
	}
	
	public NativeSplitterOptions setSegmentSimilarityThreshold(double segmentSimilarityThreshold) {
		this.segmentSimilarityThreshold = segmentSimilarityThreshold;
		return this;
	}
	
	
}
