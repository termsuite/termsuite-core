package eu.project.ttc.models;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;

public enum RelationProperty implements Property<TermRelation> {
//	INFO("Information", "info", "info", String.class),
	VARIATION_RULE("VariationRule", "vrule", "vrule", String.class),
	DERIVATION_TYPE("DerivationType", "dtype", "dtype", String.class),
	SIMILARITY("Similarity", "sim", "sim", Double.class),
	VARIANT_SCORE("Score", "vscore", "vscore", Double.class), 
	IS_DISTRIBUTIONAL("Distributional", "distrib", "distrib", Boolean.class);

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
