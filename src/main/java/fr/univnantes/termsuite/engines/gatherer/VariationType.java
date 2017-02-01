package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermRelation;

/**
 * 
 * When a {@link TermRelation} is a variation, this enum
 * represents the variation type.
 * 
 * @author Damien Cram
 * 
 */
public enum VariationType implements PropertyValue {
	PREFIXATION("P", 5, "pref", true, RelationProperty.IS_PREFIXATION), 
	DERIVATION("D", 4, "deriv", true, RelationProperty.IS_DERIVATION),
	SEMANTIC("H", 7, "syno", true, RelationProperty.IS_SEMANTIC),
	MORPHOLOGICAL("M", 1, "morph", true, RelationProperty.IS_MORPHOLOGICAL),
	SYNTAGMATIC("S", 3, "syn", true, RelationProperty.IS_SYNTAGMATIC),
	GRAPHICAL("G", 2, "graph", false, RelationProperty.IS_GRAPHICAL),
	INFERENCE("I", 8, "infer", true, RelationProperty.IS_INFERED),
	;
	
	private String letter;
	private int order;
	private String shortName;
	private boolean directional;
	private RelationProperty relationProperty;
	
	
	private VariationType(String letter, int order, String shortName, boolean directional, RelationProperty relationProperty) {
		this.letter = letter;
		this.order = order;
		this.directional = directional;
		this.shortName = shortName;
		this.relationProperty = relationProperty;
	}

	public String getLetter() {
		return letter;
	}

	public int getOrder() {
		return order;
	}

	public String getShortName() {
		return shortName;
	}

	public boolean isDirectional() {
		return directional;
	}

	@Override
	public String getSerializedString() {
		return this.shortName;
	}

	public RelationProperty getRelationProperty() {
		return relationProperty;
	}
}
