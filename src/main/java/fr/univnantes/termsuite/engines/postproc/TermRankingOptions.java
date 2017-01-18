package fr.univnantes.termsuite.engines.postproc;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.model.TermProperty;

public class TermRankingOptions {

	@JsonProperty("property")
	private TermProperty rankingProperty = TermProperty.SPECIFICITY;

	@JsonProperty("is-descending")
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
