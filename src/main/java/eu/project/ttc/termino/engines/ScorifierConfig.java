package eu.project.ttc.termino.engines;

public class ScorifierConfig {
	private double extensionSpecTh = 0.1;
	private double extensionGainTh = 0.1;
	private double variantIndependanceTh = 0.5;
	private double variationScoreTh = 0.25;
	private double orthographicScoreTh = 0.55;
	private double termIndependanceTh = 0.10;

	private ScorifierConfig() {
	}

	public double getExtensionSpecTh() {
		return extensionSpecTh;
	}

	public void setExtensionSpecTh(double extensionSpecTh) {
		this.extensionSpecTh = extensionSpecTh;
	}

	public double getExtensionGainTh() {
		return extensionGainTh;
	}

	public void setExtensionGainTh(double extensionGainTh) {
		this.extensionGainTh = extensionGainTh;
	}

	public double getVariantIndependanceTh() {
		return variantIndependanceTh;
	}

	public void setVariantIndependanceTh(double variantIndependanceTh) {
		this.variantIndependanceTh = variantIndependanceTh;
	}

	public double getVariationScoreTh() {
		return variationScoreTh;
	}

	public void setVariationScoreTh(double variationScoreTh) {
		this.variationScoreTh = variationScoreTh;
	}

	public double getOrthographicScoreTh() {
		return orthographicScoreTh;
	}

	public void setOrthographicScoreTh(double orthographicScoreTh) {
		this.orthographicScoreTh = orthographicScoreTh;
	}

	public double getTermIndependanceTh() {
		return termIndependanceTh;
	}

	public void setTermIndependanceTh(double termIndependanceTh) {
		this.termIndependanceTh = termIndependanceTh;
	}

	public static ScorifierConfig create(double variantIndependenceScoreThreshold, 
			double variantExtGainThreshold, 
			double variantExtSpecThreshold, 
			double variantScoreThreshold) {
		ScorifierConfig scorifierConfig = create();
		scorifierConfig.setExtensionGainTh(variantExtGainThreshold);
		scorifierConfig.setExtensionSpecTh(variantExtSpecThreshold);
		scorifierConfig.setVariantIndependanceTh(variantIndependenceScoreThreshold);
		scorifierConfig.setVariationScoreTh(variantScoreThreshold);
		return scorifierConfig;
	}
	public static ScorifierConfig create() {
		return new ScorifierConfig();
	}

}