package eu.project.ttc.engines.exporter;

import com.google.common.collect.ComparisonChain;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class ScoredVariation implements Comparable<ScoredVariation> {
	
	private TermVariation variation;
	
	public double strictness;
	public double extensionGain;
	public double extensionSpec;
	
	public double getStrictness() {
		return strictness;
	}

	public double getExtensionGain() {
		return extensionGain;
	}
	
	public double getExtensionSpec() {
		return extensionSpec;
	}

	public ScoredVariation(TermVariation variation, double strictness, double extensionGain, double extensionSpec) {
		super();
		this.variation = variation;
		this.strictness = strictness;
		this.extensionGain = extensionGain;
		this.extensionSpec = extensionSpec;
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
				.compare(o.getStrictness(), getStrictness())
				.compare(o.getExtensionSpec(), getExtensionSpec())
				.compare(o.getExtensionGain(), getExtensionGain())
//				.compare(o1.getVariationType().getOrder(), o2.getVariationType().getOrder())
				.compare(o.getVariant().getWR(),getVariant().getWR())
				.result();
	}
	
	public String getLabel() {
		return String.format("str:%.2f,extG:%.2f,extZ:%-4.1f",
				getStrictness(),
				getExtensionGain(),
				getExtensionSpec()
			).trim();
	}
	
}
