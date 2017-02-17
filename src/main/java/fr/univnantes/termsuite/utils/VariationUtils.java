package fr.univnantes.termsuite.utils;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;

public class VariationUtils {
	private static final String MSG_TYPE_ERR = "Expected a %s, got: %s";

	public static final VariationType[] VARIANT_TAG_TYPES = new VariationType[]{
			VariationType.INFERENCE,
			VariationType.SYNTAGMATIC,
			VariationType.MORPHOLOGICAL,
			VariationType.GRAPHICAL,
			VariationType.SEMANTIC,
			VariationType.PREFIXATION,
			VariationType.DERIVATION,
	};

	public static void copyRelationPropertyIfSet(
			Relation prototypeRelation, 
			Relation targetRelation, 
			RelationProperty... propertiesToBeCopied) {
		for(RelationProperty p:propertiesToBeCopied) {
			if(prototypeRelation.isPropertySet(p))
				targetRelation.setProperty(
						p, 
						prototypeRelation.get(p));
		}
	}

	public static String toTagString(Relation variation) {
		Preconditions.checkArgument(variation.getType() == RelationType.VARIATION, MSG_TYPE_ERR, RelationType.VARIATION, variation);
		StringBuilder tagBuilder = new StringBuilder();
		for(VariationType vType:VARIANT_TAG_TYPES) {
			if(variation.isPropertySet(vType.getRelationProperty()) 
					&& variation.getBoolean(vType.getRelationProperty()))
				tagBuilder.append(vType.getLetter().toLowerCase());
		}
		return tagBuilder.toString();
	}
}
