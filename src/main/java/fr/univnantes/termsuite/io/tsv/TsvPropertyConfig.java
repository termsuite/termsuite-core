package fr.univnantes.termsuite.io.tsv;

import fr.univnantes.termsuite.model.Property;

public class TsvPropertyConfig {
	
	private TsvPropertyType type = TsvPropertyType.BASE_TERM_OR_VARIATION_TARGET_TERM;
	private Property<?> property;

	public TsvPropertyConfig(TsvPropertyType type, Property<?> property) {
		this(property);
		this.type = type;
	}

	public TsvPropertyConfig(Property<?> property) {
		super();
		this.property = property;
	}
	
	public TsvPropertyType getType() {
		return type;
	}
	
	public Property<?> getProperty() {
		return property;
	}
}
