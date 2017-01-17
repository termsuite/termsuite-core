package fr.univnantes.termsuite.engines.contextualizer;

import fr.univnantes.termsuite.framework.ConfigurationObject;
import fr.univnantes.termsuite.metrics.AssociationRate;
import fr.univnantes.termsuite.metrics.LogLikelihood;
import fr.univnantes.termsuite.model.OccurrenceType;

@ConfigurationObject
public class ContextualizerOptions {

	private int scope = 3;
	private int minimumCooccFrequencyThreshold = 2;
	private OccurrenceType coTermType = OccurrenceType.SINGLE_WORD;
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
	
	public OccurrenceType getCoTermType() {
		return coTermType;
	}
	
	public ContextualizerOptions setCoTermType(OccurrenceType coTermType) {
		this.coTermType = coTermType;
		return this;
	}
}
