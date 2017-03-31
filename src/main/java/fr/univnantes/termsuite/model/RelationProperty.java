package fr.univnantes.termsuite.model;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Joiner;

public enum RelationProperty implements Property<Relation> {

	VARIATION_RANK("VariationRank", "vrank", "vrank", Integer.class, "The rank of the variation among all variations starting from the same source term, when the relation is a variation."),
	
	VARIATION_RULES("VariationRule", "vrules", "vrules", Set.class, "The set of YAML variation rules that detected this pair of terms as a term variation, when the relation is a variation."),
	DERIVATION_TYPE("DerivationType", "dtype", "dtype", String.class, "The derivation type of the variation, when the relation is a variation."),
	GRAPHICAL_SIMILARITY("GraphSimilarity", "graphSim", "graphSim", Double.class, "The edition distance between the two terms of the relation."),
	VARIANT_SCORE("Score", "vScore", "vScore", Double.class, "The global variation score of the relation assigned by TermSuite post-processor engine, when the relation if a variation."), 
	AFFIX_GAIN("AffixGain", "affGain", "affGain", Double.class, "When the relation is a variation of type \"extension\", the "+TermProperty.FREQUENCY+" of the variant divided by the "+TermProperty.FREQUENCY+" of the affix term."),
	AFFIX_SPEC("AffixSpec", "affSpec", "affSpec", Double.class, "When the relation is a variation of type \"extension\", the "+TermProperty.SPECIFICITY+" of the affix term."),
	AFFIX_RATIO("AffixRatio", "affRatio", "affRatio", Double.class, "When the relation is a variation of type \"extension\", the "+TermProperty.FREQUENCY+" of the affix term divided by the "+TermProperty.FREQUENCY+" of the base term."),
	AFFIX_SCORE("AffixScore", "affScore", "affScore", Double.class, "When the relation is a variation of type \"extension\", the weighted average of `"+AFFIX_GAIN+"` and `"+AFFIX_RATIO+"`."), 
	NORMALIZED_AFFIX_SCORE("NormalizedAffixScore", "normAffScore", "normAffScore", Double.class, "When the relation is a variation of type \"extension\", the min-max normalization of `" + AFFIX_SCORE.getPropertyName() + "`."), 
	AFFIX_ORTHOGRAPHIC_SCORE("AffixOrthographicScore", "affOrtho", "affOrtho", Double.class, "When the relation is a variation of type \"extension\", the orthographic score of extension affix term."),
	EXTENSION_SCORE("ExtensionScore", "extScore", "extScore", Double.class, "When the relation is a variation of type \"extension\", the score of the extension affix term (combines `"+AFFIX_GAIN.getPropertyName()+"` and `"+AFFIX_GAIN.getPropertyName()+"`)."),
	NORMALIZED_EXTENSION_SCORE("NormalizedExtensionScore", "normExtScore", "normExtScore", Double.class, "When the relation is a variation of type \"extension\", the min-max normalization of `" + EXTENSION_SCORE.getPropertyName() + "`."), 
	HAS_EXTENSION_AFFIX("HasExtensionAffix", "hasExtAffix", "hasExtAffix", Boolean.class, "When the relation is a variation of type \"extension\", wether there is an affix term."),
	IS_EXTENSION("IsExtension", "isExt", "isExt", Boolean.class, "Wether this relation is an extension."), 
	VARIANT_BAG_FREQUENCY("VariantBagFrequency", "vBagFreq", "vBagFreq", Integer.class, "When the relation is a variation, the total of number of occurrences of the variant term and of variant's variant terms (order-2 variants)."), 
	SOURCE_GAIN("SourceGain", "srcGain", "srcGain", Double.class, "When the relation is a variation, the log10 of `"+VARIANT_BAG_FREQUENCY.getPropertyName()+"` divided by the "+TermProperty.FREQUENCY+" of the base term."),
	NORMALIZED_SOURCE_GAIN("NormalizedSourceGain", "normSrcGain", "normSrcGain", Double.class, "When the relation is a variation of type \"extension\", the linear normalization of `" + SOURCE_GAIN.getPropertyName() + "`."), 

	IS_INFERED("IsInfered", "isInfered", "isInfered", Boolean.class, "When the relation is a variation, wether it has been infered from two other base variations."), 
	IS_GRAPHICAL("IsGraphical", "isGraph", "isGraph", Boolean.class, "When the relation is a variation, wether there is a graphical similarity between the two terms."), 
	IS_DERIVATION("IsDerivation", "isDeriv", "isDeriv", Boolean.class, "When the relation is a variation, wether one term is the derivation of the other."), 
	IS_PREFIXATION("IsPrefixation", "isPref", "isPref", Boolean.class, "When the relation is a variation, wether one term is the prefix of the other."), 
	IS_SYNTAGMATIC("IsSyntagmatic", "isSyntag", "isSyntag", Boolean.class, "When the relation is a variation, wether it is a syntagmatic variation."), 
	IS_MORPHOLOGICAL("IsMorphological", "isMorph", "isMorph", Boolean.class, "When the relation is a variation, wether the variation implies morphosyntactic variations."), 

	IS_SEMANTIC("IsSemantic", "isSem", "isSem", Boolean.class, "When the relation is a variation, wether there is a semantic similarity between the two terms."), 
	IS_DISTRIBUTIONAL("Distributional", "distrib", "distrib", Boolean.class, "When the relation is a semantic relation, wheter the relation is of type \"distributional\", i.e. the variation has been found by context vector alignment."),
	SEMANTIC_SIMILARITY("SemanticSimilarity", "semSim", "semSim", Double.class, "When the relation is a semantic variation found by alignment, the similarity of the two context vectors of the two terms of the relation."), 
	IS_DICO("Dico", "isDico", "isDico", Boolean.class, "When the relation is a semantic relation, wheter the relation is of type \"dictionary\", i.e. the variation has been found with a synonymic dico."), 
	SEMANTIC_SCORE("SemanticScore", "semScore", "semScore", Double.class, "When the relation is a semantic variation, the score of pertinency of the variation. This property is set for all types of semantic variations, both from dico and distributional."), 
	
	;
	
	private PropertyHolderBase<RelationProperty, Relation> delegate;

	private RelationProperty(String propertyName, String propertyShortName, String jsonField, Class<?> range, String description) {
		delegate = new PropertyHolderBase<>(propertyName, propertyShortName, jsonField, range, description);
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
	public String getDescription() {
		return delegate.getDescription();
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
