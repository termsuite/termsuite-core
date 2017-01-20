package fr.univnantes.termsuite.engines.contextualizer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContextualizerOptions  {

	private boolean enabled = false;
	
	@JsonProperty("scope")
	private int scope = 3;
	
	@JsonProperty("coocc-frequency-th")
	private int minimumCooccFrequencyThreshold = 2;

	@JsonProperty("association-rate")
	private Class<? extends AssociationRate> associationRate = LogLikelihood.class;

	
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
