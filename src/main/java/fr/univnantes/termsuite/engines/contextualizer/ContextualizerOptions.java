package fr.univnantes.termsuite.engines.contextualizer;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.utils.JsonConfigObject;

public class ContextualizerOptions   extends JsonConfigObject {

	private boolean enabled = false;
	
	@JsonProperty("scope")
	private int scope = 3;
	
	@JsonProperty("coocc-frequency-th")
	private int minimumCooccFrequencyThreshold = 2;

	@JsonProperty("association-rate")
	private Class<? extends AssociationRate> associationRate = LogLikelihood.class;

	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ContextualizerOptions) {
			ContextualizerOptions o = (ContextualizerOptions)obj;
			return enabled == o.enabled
					&& scope == o.scope
					&& minimumCooccFrequencyThreshold == o.minimumCooccFrequencyThreshold
					&& associationRate.equals(o.associationRate)
					;
		} else return false;
	}


	
	public Class<? extends AssociationRate> getAssociationRate() {
		return associationRate;
	}
	
	public ContextualizerOptions setAssociationRate(Class<? extends AssociationRate> associationRate) {
		this.associationRate = associationRate;
		return this;
	}
	
	public int getScope() {
		return scope;
	}
	
	public ContextualizerOptions setScope(int scope) {
		this.scope = scope;
		return this;
	}
	
	public int getMinimumCooccFrequencyThreshold() {
		return minimumCooccFrequencyThreshold;
	}
	
	public ContextualizerOptions setMinimumCooccFrequencyThreshold(int minimumCooccFrequencyThreshold) {
		this.minimumCooccFrequencyThreshold = minimumCooccFrequencyThreshold;
		return this;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
