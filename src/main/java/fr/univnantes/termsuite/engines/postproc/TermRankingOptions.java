package fr.univnantes.termsuite.engines.postproc;

import fr.univnantes.termsuite.framework.ConfigurationObject;
import fr.univnantes.termsuite.model.TermProperty;

@ConfigurationObject
public class TermRankingOptions {

	private TermProperty rankingProperty = TermProperty.SPECIFICITY;
	private boolean desc = true;
	
	public TermProperty getRankingProperty() {
		return rankingProperty;
	}
	public TermRankingOptions setRankingProperty(TermProperty rankingProperty) {
		this.rankingProperty = rankingProperty;
		return this;
	}
	
	public boolean isDesc() {
		return desc;
	}
	
	public TermRankingOptions setDesc(boolean desc) {
		this.desc = desc;
		return this;
	}
	
	
	
}
