package fr.univnantes.termsuite.engines.postproc;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.utils.JsonConfigObject;

public class TermRankingOptions  extends JsonConfigObject {

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
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TermRankingOptions) {
			TermRankingOptions o = (TermRankingOptions)obj;
			return rankingProperty == o.rankingProperty
					&& desc == o.desc;
		} else return false;
	}
}
