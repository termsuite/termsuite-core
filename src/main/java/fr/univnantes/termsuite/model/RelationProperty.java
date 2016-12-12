package fr.univnantes.termsuite.model;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;

import fr.univnantes.termsuite.engines.gatherer.VariationType;

public enum RelationProperty implements Property<TermRelation> {
	VARIATION_RANK("VariationRank", "vrank", "vrank", Integer.class),
	VARIATION_RULE("VariationRule", "vrule", "vrule", String.class),
	VARIATION_TYPE("VariationRuleType", "vruleType", "vruleType", VariationType.class),
	DERIVATION_TYPE("DerivationType", "dtype", "dtype", String.class),
	SIMILARITY("Similarity", "sim", "sim", Double.class),
	VARIANT_SCORE("Score", "vScore", "vScore", Double.class), 
	IS_DISTRIBUTIONAL("Distributional", "distrib", "distrib", Boolean.class),
	AFFIX_ORTHOGRAPHIC_SCORE("AffixOrthographicScore", "affOrtho", "affOrtho", Double.class),
	EXTENSION_SCORE("ExtensionScore", "extScore", "extScore", Double.class),
	AFFIX_SPEC("AffixSpec", "affSpec", "affSpec", Double.class),
	STRICTNESS("Strictness", "strictness", "strictness", Double.class),
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
	;

	private String propertyName;
	private String propertyShortName;
	private String jsonField;
	private Class<?> range;

	private RelationProperty(String propertyName, String propertyShortName, String propertyJsonName, Class<?> range) {
		this.propertyName = propertyName;
		this.propertyShortName = propertyShortName;
		this.jsonField = propertyJsonName;
		this.range = range;
	}

	@Override
	public Class<?> getRange() {
		return range;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public boolean isDecimalNumber() {
		return Property.isDecimalNumber(range);
	}

	@Override
	public String getShortName() {
		return propertyShortName;
	}

	@Override
	public boolean isNumeric() {
		return Property.isNumeric(range);
	}

	

	@Override
	public int compare(TermRelation o1, TermRelation o2) {
		return ComparisonChain.start()
				.compare(
						o1.getPropertyValueUnchecked(this), 
						o2.getPropertyValueUnchecked(this))
				.result();
	}


	@Override
	public String getJsonField() {
		return jsonField;
	}

	public static RelationProperty fromJsonField(String field) {
		for(RelationProperty p:values())
			if(p.jsonField.equals(field))
				return p;
		return null;
	}

	@Override
	public Comparator<TermRelation> getComparator() {
		return getComparator(false);
	}

	@Override
	public Comparator<TermRelation> getComparator(boolean reverse) {
		return new Comparator<TermRelation>() {
			@Override
			public int compare(TermRelation o1, TermRelation o2) {
				return reverse ? 
						RelationProperty.this.compare(o2, o1) :
							RelationProperty.this.compare(o1, o2)
									;
			}
		};
	}
}
