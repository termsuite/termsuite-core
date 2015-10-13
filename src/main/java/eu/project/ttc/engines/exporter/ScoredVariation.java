package eu.project.ttc.engines.exporter;

import com.google.common.collect.ComparisonChain;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class ScoredVariation implements Comparable<ScoredVariation> {
	
	private TermVariation variation;
	
	public double strictnessScore;
	public double extensionGain;
	public double extensionSpec;
	public double frequencyScore;
	
	public double getStrictnessScore() {
		return strictnessScore;
	}

	public double getExtensionGainScore() {
		return extensionGain;
	}
	
	public double getExtensionSpecScore() {
		return extensionSpec;
	}

	public double getFrequencyScore() {
		return frequencyScore;
	}
	
	public ScoredVariation(TermVariation variation, double strictness, double extensionGain, double extensionSpec, double frequency) {
		super();
		this.variation = variation;
		this.strictnessScore = strictness;
		this.extensionGain = extensionGain;
		this.extensionSpec = extensionSpec;
		this.frequencyScore = frequency;
	}

	public VariationType getVariationType() {
		return variation.getVariationType();
	}

	public Term getVariant() {
		return variation.getVariant();
	}

	public Term getBase() {
		return variation.getBase();
	}

	@Override
	public int compareTo(ScoredVariation o) {
		return ComparisonChain.start()
				.compare(o.getVariationScore(), getVariationScore())
				.compare(o.getStrictnessScore(), getStrictnessScore())
				.compare(o.getExtensionScore(), getExtensionScore())
				.compare(o.getExtensionSpecScore(), getExtensionSpecScore())
				.compare(o.getExtensionGainScore(), getExtensionGainScore())
//				.compare(o1.getVariationType().getOrder(), o2.getVariationType().getOrder())
				.compare(o.getVariant().getWR(),getVariant().getWR())
				.result();
	}
	
	public double getVariationScore() {
		if(strictnessScore == 100d) {
			return 90 + 0.1*frequencyScore;
		} else {
			return 0.75*getExtensionScore() + 0.25*frequencyScore;
		}
	}

	public String getLabel() {
//		return String.format("%s -- str:%2.0f,extG:%2.0f,extSpec:%2.0f,extScore:%2.0f,freqScore:%2.0f,varScore:%2.0f",
//				this.variation.getLabel(),
//				getStrictnessScore(),
//				getExtensionGainScore(),
//				getExtensionSpecScore(),
//				getExtensionScore(),
//				getFrequencyScore(),
//				getVariationScore()
//			).trim();
		return String.format("%2.0f",
				getVariationScore()
			).trim();
	}

	public double getExtensionScore() {
		double root = 2d;
		double w = 3d; // gain weight
		return Math.pow((w*Math.pow(getExtensionGainScore(), root) + Math.pow(getExtensionSpecScore(), root))/(1+w),1d/root);
//		return Math.pow((Math.pow(getExtensionSpec(), root) + Math.pow(getExtensionGain(), root))/2,1d/root);
	}
	
}
