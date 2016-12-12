package fr.univnantes.termsuite.engines.gatherer;

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
	PREFIXATION("P", 5, "pref", true), 
	DERIVATION("D", 4, "deriv", true),
	SEMANTIC("H", 7, "syno", true),
	MORPHOLOGICAL("M", 1, "morph", true),
	SYNTAGMATIC("S", 3, "syn", true),
	GRAPHICAL("G", 2, "graph", false),
	;
	
	private String letter;
	private int order;
	private String shortName;
	private boolean directional;
	
	private VariationType(String letter, int order, String shortName, boolean directional) {
		this.letter = letter;
		this.order = order;
		this.directional = directional;
		this.shortName = shortName;
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

}
