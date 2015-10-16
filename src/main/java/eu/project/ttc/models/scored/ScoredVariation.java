package eu.project.ttc.models.scored;

import com.google.common.base.Objects;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.resources.ScoredModel;
import eu.project.ttc.utils.TermUtils;

public class ScoredVariation extends ScoredTermOrVariant {
	
	private static final double THRESHOLD_EXTENSION_GAIN = 0.333333d;

	private TermVariation variation;
	
	private ScoredTerm extensionAffix;
	private boolean extensionAffixSet = false;

	
//	public double strictnessScore = Double.MIN_VALUE;
//	public double extensionGain = Double.MIN_VALUE;
//	public double extensionSpec = Double.MIN_VALUE;
//	public double frequencyScore;
	
	
	public ScoredVariation(ScoredModel scoredModel, TermVariation tv) {
		super(scoredModel, tv.getVariant());
		this.variation = tv;
	}
	
	public ScoredTerm getExtensionAffix() {
		if(!extensionAffixSet) {
			try {
				Term rawTerm = TermUtils.getExtensionAffix(
						scoredModel.getTermIndex(),
						getBase().getTerm(), 
						getVariant().getTerm());
				this.extensionAffix = scoredModel.getAdapter(rawTerm);
			} catch(IllegalStateException e) {
				// not an extension
				this.extensionAffix = null;
			}
			extensionAffixSet = true;
//			System.out.format("Ext affix for term (%s,%s) is %s\n", 
//					getBase().getTerm(),
//					getVariant().getTerm(),
//					extensionAffix
//					);
		}
		return this.extensionAffix;
	}
	
	public ScoredTerm getVariant() {
		return this.scoredModel.getAdapter(this.variation.getVariant());
	}

	public ScoredTerm getBase() {
		return this.scoredModel.getAdapter(this.variation.getBase());
	}


	public String getLabel() {
		return String.format("S:%2.0f,E:%2.0f(G:%2.0f/WR:%2.0f),F:%2.0f,V:%2.0f",
				100*getStrictnessScore(),
				100*getExtensionScore(),
				100*getExtensionGainScore(),
				100*getExtensionSpecScore(),
				100*getFrequencyScore(),
				100*getVariationScore()
			).trim();
//		return String.format("%2.0f",
//				getVariationScore()
//			).trim();
	}

	/* *************************************
	 * Scores
	 * **************************************
	 */
	
	public double getStrictnessScore() {
		return TermUtils.getStrictness(variation.getVariant(), getBase().getTerm());
	}

	public double getExtensionGainScore() {
		if(getExtensionAffix() == null || getExtensionAffix().getFrequency() == 0)
			return THRESHOLD_EXTENSION_GAIN;
		else
			return ((double)getFrequency())/getExtensionAffix().getFrequency();
	}
	
	public double getExtensionSpecScore() {
		if(getExtensionAffix() == null)
			return 0;
		else
			return  getExtensionAffix().getWRLog() / getBase().getMaxExtensionAffixWRLog();
	}
	
	public double getExtensionScore() {
		double root = 2d;
		double w = 3d; // gain weight
		return Math.pow((w*Math.pow(getExtensionGainScore(), root) + Math.pow(getExtensionSpecScore(), root))/(1+w),1d/root);
//		return Math.pow((Math.pow(getExtensionSpec(), root) + Math.pow(getExtensionGain(), root))/2,1d/root);
	}

	public double getFrequencyScore() {
		return (double)getFrequency()/getBase().getMaxVariationFrequency();
	}


	public double getVariationScore() {
		if(getStrictnessScore() == 1d) {
			return 0.9 + 0.1*getFrequencyScore();
		} else {
			return 0.75*getExtensionScore() + 0.25*getFrequencyScore();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ScoredTerm) {
			ScoredVariation sv = (ScoredVariation) obj;
			return Objects.equal(this.getTerm(), sv.getTerm())
					&& Objects.equal(this.getVariant(),sv.getVariant());
		} else
			return false;
	}
}
