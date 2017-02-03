package fr.univnantes.termsuite.framework.service;

import static fr.univnantes.termsuite.model.RelationProperty.AFFIX_GAIN;
import static fr.univnantes.termsuite.model.RelationProperty.AFFIX_ORTHOGRAPHIC_SCORE;
import static fr.univnantes.termsuite.model.RelationProperty.AFFIX_RATIO;
import static fr.univnantes.termsuite.model.RelationProperty.AFFIX_SCORE;
import static fr.univnantes.termsuite.model.RelationProperty.AFFIX_SPEC;
import static fr.univnantes.termsuite.model.RelationProperty.DERIVATION_TYPE;
import static fr.univnantes.termsuite.model.RelationProperty.EXTENSION_SCORE;
import static fr.univnantes.termsuite.model.RelationProperty.IS_DICO;
import static fr.univnantes.termsuite.model.RelationProperty.IS_DISTRIBUTIONAL;
import static fr.univnantes.termsuite.model.RelationProperty.IS_INFERED;
import static fr.univnantes.termsuite.model.RelationProperty.IS_SYNTAGMATIC;
import static fr.univnantes.termsuite.model.RelationProperty.NORMALIZED_AFFIX_SCORE;
import static fr.univnantes.termsuite.model.RelationProperty.NORMALIZED_EXTENSION_SCORE;
import static fr.univnantes.termsuite.model.RelationProperty.NORMALIZED_SOURCE_GAIN;
import static fr.univnantes.termsuite.model.RelationProperty.SEMANTIC_SCORE;
import static fr.univnantes.termsuite.model.RelationProperty.SEMANTIC_SIMILARITY;
import static fr.univnantes.termsuite.model.RelationProperty.SOURCE_GAIN;
import static fr.univnantes.termsuite.model.RelationProperty.VARIANT_BAG_FREQUENCY;
import static fr.univnantes.termsuite.model.RelationProperty.VARIANT_SCORE;
import static fr.univnantes.termsuite.model.RelationProperty.VARIATION_RANK;
import static fr.univnantes.termsuite.model.RelationProperty.VARIATION_RULE;
import static fr.univnantes.termsuite.model.RelationProperty.VARIATION_TYPE;

import java.util.Optional;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;

public class RelationService {

	private TerminologyService terminologyService;
	private Relation relation;
	
	public RelationService(TerminologyService terminologyService, Relation relation) {
		super();
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(terminologyService);
		this.terminologyService = terminologyService;
		this.relation = relation;
	}
	
	public Relation getRelation() {
		return relation;
	}
	
	public TermService getFrom() {
		return terminologyService.asTermService(relation.getFrom());
	}

	public TermService getTo() {
		return terminologyService.asTermService(relation.getTo());
	}
	
	public boolean notExtension() {
		return !isExtension();
	}
	
	public boolean isExtension() {
		return relation.getType() == RelationType.HAS_EXTENSION || (relation.isPropertySet(RelationProperty.IS_EXTENSION)
				&& relation.getBoolean(RelationProperty.IS_EXTENSION));
	}
	
	public boolean isVariation() {
		return relation.getType() == RelationType.VARIATION;
	}
	
	@Override
	public String toString() {
		return relation.toString();
	}
	
	@Override
	public int hashCode() {
		return relation.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RelationService) {
			return ((RelationService) obj).relation.equals(relation);
		} else
			return relation.equals(obj);
	}

	public RelationType getType() {
		return this.relation.getType();
	}

	public int getRank() {
		return relation.getInteger(RelationProperty.VARIATION_RANK);
	}

	public boolean isPropertySet(RelationProperty p) {
		return relation.isPropertySet(p);
	}

	public void setProperty(RelationProperty p, Comparable<?> value) {
		relation.setProperty(p, value);
	}

	private static final String MSG_NOT_AN_EXTENSION = "This reltaion is not an extension: %s";

	public Optional<TermService> getExtensionAffix(Term term) {
		Preconditions.checkState(isExtension(), MSG_NOT_AN_EXTENSION, relation);
		return terminologyService.getExtensionAffix(term, relation.getTo());
	}

	public Comparable<?> get(RelationProperty property) {
		return relation.get(property);
	}
	
	public boolean isMorphological() {
		return relation.isPropertySet(RelationProperty.IS_MORPHOLOGICAL) 
				&& relation.getBoolean(RelationProperty.IS_MORPHOLOGICAL);
	}

	public boolean notMorphological() {
		return !isMorphological();
	}

	public boolean isSemantic() {
		return (relation.isPropertySet(RelationProperty.IS_SEMANTIC) 
				&& relation.getBoolean(RelationProperty.IS_SEMANTIC));
	}

	public boolean notSemantic() {
		return !isSemantic();
	}
	
	public boolean isGraphical() {
		return relation.isPropertySet(RelationProperty.IS_GRAPHICAL) 
					&& relation.getBoolean(RelationProperty.IS_GRAPHICAL);
	}
	
	public boolean notGraphical() {
		return !isGraphical();
	}
	
	public boolean isDerivation() {
		return relation.isPropertySet(RelationProperty.IS_DERIVATION) 
					&& relation.getBoolean(RelationProperty.IS_DERIVATION);
	}

	public boolean notDerivation() {
		return ! isDerivation();
	}
	
	public boolean isPrefixation() {
		return relation.isPropertySet(RelationProperty.IS_PREFIXATION) 
					&& relation.getBoolean(RelationProperty.IS_PREFIXATION);
	}
	
	public boolean hasExtensionAffix() {
		return relation.isPropertySet(RelationProperty.HAS_EXTENSION_AFFIX)
				&&	relation.getBoolean(RelationProperty.HAS_EXTENSION_AFFIX);
	}
	
	public boolean notPrefixation() {
		return !isPrefixation();
	}

	public double getGraphicalSimilarity() {
		return (double)get(RelationProperty.GRAPHICAL_SIMILARITY);
	}

	public boolean getBooleanIfSet(RelationProperty p) {
		return relation.isPropertySet(p) && relation.getBoolean(p);
	}

	public String getString(RelationProperty p) {
		return isPropertySet(p) ? (String)get(p) : null;
	}
	
	public Double getDouble(RelationProperty p) {
		return isPropertySet(p) ? (Double)get(p) : null;
	}
	
	public Integer getInteger(RelationProperty p) {
		return isPropertySet(p) ? (Integer)get(p) : null;
	}
	
	public Boolean getBoolean(RelationProperty p) {
		return isPropertySet(p) ? (Boolean)get(p) : null;
	}
	
	
	public Integer getVariationRank() {
		return relation.getIntegerUnchecked(VARIATION_RANK);
	}


	public String getVariationRule() {
		return relation.getStringUnchecked(VARIATION_RULE);
	}

	public VariationType getVariationType() {
		return (VariationType)get(VARIATION_TYPE);
	}

	public String getDerivationType() {
		return relation.getStringUnchecked(DERIVATION_TYPE);
	}

	public Double getSemanticSimilarity() {
		return relation.getDoubleUnchecked(SEMANTIC_SIMILARITY);
	}

	public Double getVariantScore() {
		return relation.getDoubleUnchecked(VARIANT_SCORE);
	}

	public boolean isDistributional() {
		return relation.isPropertySet(IS_DISTRIBUTIONAL) && relation.getBooleanUnchecked(IS_DISTRIBUTIONAL);
	}

	public Double getAffixOrthographicScore() {
		return relation.getDoubleUnchecked(AFFIX_ORTHOGRAPHIC_SCORE);
	}

	public Double getExtensionScore() {
		return relation.getDoubleUnchecked(EXTENSION_SCORE);
	}

	public Double getAffixSpec() {
		return relation.getDoubleUnchecked(AFFIX_SPEC);
	}

	public Double getSourceGain() {
		return relation.getDoubleUnchecked(SOURCE_GAIN);
	}

	public Double getAffixGain() {
		return relation.getDoubleUnchecked(AFFIX_GAIN);
	}

	public Double getAffixRatio() {
		return relation.getDoubleUnchecked(AFFIX_RATIO);
	}

	public Double getAffixScore() {
		return relation.getDoubleUnchecked(AFFIX_SCORE);
	}

	public Double getNormalizedExtensionScore() {
		return relation.getDoubleUnchecked(NORMALIZED_EXTENSION_SCORE);
	}

	public Double getNormalizedSourceGain() {
		return relation.getDoubleUnchecked(NORMALIZED_SOURCE_GAIN);
	}

	public Double getNormalizedAffixScore() {
		return relation.getDoubleUnchecked(NORMALIZED_AFFIX_SCORE);
	}

	public boolean isInfered() {
		return relation.isPropertySet(IS_INFERED) && relation.getBoolean(IS_INFERED);
	}

	public boolean notInfered() {
		return !isInfered();
	}

	public boolean notSyntagmatic() {
		return !isSyntagmatic();
	}

	public boolean isSyntagmatic() {
		return relation.isPropertySet(IS_SYNTAGMATIC) && relation.getBooleanUnchecked(IS_SYNTAGMATIC);
	}

	public Integer getVariantBagFrequency() {
		return relation.getIntegerUnchecked(VARIANT_BAG_FREQUENCY);
	}

	public boolean isDico() {
		return relation.isPropertySet(IS_DICO) && relation.getBooleanUnchecked(IS_DICO);
	}

	public Double getSemanticScore() {
		return relation.getDoubleUnchecked(SEMANTIC_SCORE);
	}

	public boolean isVariationOfType(VariationType type) {
		if(isPropertySet(RelationProperty.VARIATION_TYPE) && getVariationType() == type)
			return true;
		else if(isPropertySet(type.getRelationProperty()) && getBoolean(type.getRelationProperty()))
			return true;
		else
			return false;
	}

}
