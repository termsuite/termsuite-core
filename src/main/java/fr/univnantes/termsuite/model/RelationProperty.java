package fr.univnantes.termsuite.model;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Joiner;

public enum RelationProperty implements Property<Relation> {

	VARIATION_RANK("VariationRank", "vrank", "vrank", Integer.class),
	VARIATION_RULES("VariationRule", "vrules", "vrules", Set.class),
	DERIVATION_TYPE("DerivationType", "dtype", "dtype", String.class),
	GRAPHICAL_SIMILARITY("GraphSimilarity", "graphSim", "graphSim", Double.class),
	SEMANTIC_SIMILARITY("SemanticSimilarity", "semSim", "semSim", Double.class), 
	VARIANT_SCORE("Score", "vScore", "vScore", Double.class), 
	IS_DISTRIBUTIONAL("Distributional", "distrib", "distrib", Boolean.class),
	AFFIX_ORTHOGRAPHIC_SCORE("AffixOrthographicScore", "affOrtho", "affOrtho", Double.class),
	EXTENSION_SCORE("ExtensionScore", "extScore", "extScore", Double.class),
	AFFIX_SPEC("AffixSpec", "affSpec", "affSpec", Double.class),
	HAS_EXTENSION_AFFIX("HasExtensionAffix", "hasExtAffix", "hasExtAffix", Boolean.class),
	IS_EXTENSION("IsExtension", "isExt", "isExt", Boolean.class), 
	SOURCE_GAIN("SourceGain", "srcGain", "srcGain", Double.class),
	AFFIX_GAIN("AffixGain", "affGain", "affGain", Double.class),
	AFFIX_RATIO("AffixRatio", "affRatio", "affRatio", Double.class),
	AFFIX_SCORE("AffixScore", "affScore", "affScore", Double.class), 
	NORMALIZED_EXTENSION_SCORE("NormalizedExtensionScore", "normExtScore", "normExtScore", Double.class), 
	NORMALIZED_SOURCE_GAIN("NormalizedSourceGain", "normSrcGain", "normSrcGain", Double.class), 
	NORMALIZED_AFFIX_SCORE("NormalizedAffixScore", "normAffScore", "normAffScore", Double.class), 
	IS_INFERED("IsInfered", "isInfered", "isInfered", Boolean.class), 
	IS_GRAPHICAL("IsGraphical", "isGraph", "isGraph", Boolean.class), 
	IS_SEMANTIC("IsSemantic", "isSem", "isSem", Boolean.class), 
	IS_DERIVATION("IsDerivation", "isDeriv", "isDeriv", Boolean.class), 
	IS_PREFIXATION("IsPrefixation", "isPref", "isPref", Boolean.class), 
	IS_SYNTAGMATIC("IsSyntagmatic", "isSyntag", "isSyntag", Boolean.class), 
	IS_MORPHOLOGICAL("IsMorphological", "isMorph", "isMorph", Boolean.class), 
	VARIANT_BAG_FREQUENCY("VariantBagFrequency", "vBagFreq", "vBagFreq", Integer.class), 
	IS_DICO("Dico", "isDico", "isDico", Boolean.class), 
	SEMANTIC_SCORE("SemanticScore", "semScore", "semScore", Double.class), 
	
	;
	
	private PropertyHolderBase<RelationProperty, Relation> delegate;

	private RelationProperty(String propertyName, String propertyShortName, String jsonField, Class<?> range) {
		delegate = new PropertyHolderBase<>(propertyName, propertyShortName, jsonField, range);
	}
	

	@Override
	public String getPropertyName() {
		return delegate.getPropertyName();
	}


	@Override
	public String getJsonField() {
		return delegate.getJsonField();
	}


	@Override
	public Class<?> getRange() {
		return delegate.getRange();
	}


	@Override
	public String getShortName() {
		return delegate.getShortName();
	}


	@Override
	public boolean isNumeric() {
		return delegate.isNumeric();
	}


	@Override
	public boolean isDecimalNumber() {
		return delegate.isDecimalNumber();
	}


	@Override
	public Comparator<Relation> getComparator() {
		return delegate.getComparator(this);
	}


	@Override
	public Comparator<Relation> getComparator(boolean reverse) {
		return delegate.getComparator(this, reverse);
	}

	@Override
	public int compare(Relation o1, Relation o2) {
		return delegate.compare(this, o1, o2);
	}

	public static RelationProperty fromJsonString(String field) {
		return PropertyHolderBase.fromJsonString(RelationProperty.class, field);
	}


	public static RelationProperty forName(String name) {
		for(RelationProperty p:values()) {
			if(p.getPropertyName().equals(name) || p.getShortName().equals(name))
				return p;
		}
		throw new IllegalArgumentException(
				String.format(
						"Bad relation property name: %s. Allowed: %s", 
						name,
						Joiner.on(',').join(RelationProperty.values())
				)
		);
	}

	public static Optional<RelationProperty> forNameOptional(String str) {
		for(RelationProperty p:values()) {
			if(p.getPropertyName().equals(str) || p.getShortName().equals(str))
				return Optional.of(p);
		}
		return Optional.empty();
	}
}
