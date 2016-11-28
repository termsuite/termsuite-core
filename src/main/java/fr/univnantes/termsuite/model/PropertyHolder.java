package fr.univnantes.termsuite.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class PropertyHolder<T extends Property<?>> {
	
	private static final String ERR_PROPERTY_CANNOT_BE_NULL = "Property cannot be null";
	private static final String ERR_VALUE_CANNOT_BE_NULL = "Value cannot be null for property %s";
	private static final String ERR_PROPERTY_NOT_SET = "Property %s not set";
	private static final String ERR_NOT_AN_INSTANCE = "Value <%s> is not an instance of range %s";
	
	protected Map<T, Comparable<?>> properties = new HashMap<>();
	
	public void setProperty(T property, Comparable<?> value) {
		Preconditions.checkNotNull(property, ERR_PROPERTY_CANNOT_BE_NULL);
		Preconditions.checkNotNull(value, ERR_VALUE_CANNOT_BE_NULL, property);
		Preconditions.checkArgument(property.getRange().isInstance(value), ERR_NOT_AN_INSTANCE, value, property.getRange());
		properties.put(property, value);
	}

	public Comparable<?> getPropertyValue(T property) {
		Preconditions.checkNotNull(isPropertySet(property), ERR_PROPERTY_NOT_SET, property);
		return properties.get(property);
	}

	public boolean isPropertySet(T property) {
		Preconditions.checkNotNull(property, ERR_PROPERTY_CANNOT_BE_NULL);
		return properties.containsKey(property);
	}

	public Comparable<?> getPropertyValueUnchecked(T property) {
		return properties.get(property);		
	}
	
	public String getPropertyStringValue(T property, String defaultValue) {
		return properties.containsKey(property) ?
				(String)properties.get(property)
					: defaultValue;
	}

	public String getPropertyStringValue(T property) {
		return (String)getPropertyValue(property);
	}

	public Double getPropertyDoubleValue(T property) {
		return (Double)getPropertyValue(property);
	}

	public Integer getPropertyIntegerValue(T property) {
		return (Integer)getPropertyValue(property);
	}

	public Long getPropertyLongValue(T property) {
		return (Long)getPropertyValue(property);
	}

	public Float getPropertyFloatValue(T property) {
		return (Float)getPropertyValue(property);
	}

	public Boolean getPropertyBooleanValue(T property) {
		return (Boolean)getPropertyValue(property);
	}
	
	private static final String ERR_PROPERTY_MUST_BE_NUMERIC = "Property %s must be numeric";
	private static final String ERR_UNEXPECTED_PROPERTY_RANGE = "Unexpected property range <%s> for property %s";
	public int compareNumericValue(T property, Double value) {
		Preconditions.checkArgument(property.isNumeric(),ERR_PROPERTY_MUST_BE_NUMERIC, property);
		if(property.getRange().equals(Double.class)) 
			return Double.compare(getPropertyDoubleValue(property), value);
		else if(property.getRange().equals(Integer.class)) 
			return Double.compare(getPropertyIntegerValue(property), value);
		else if(property.getRange().equals(Float.class)) 
			return Double.compare(getPropertyFloatValue(property), value);
		else if(property.getRange().equals(Long.class)) 
			return Double.compare(getPropertyLongValue(property), value);
		else 
			throw new IllegalArgumentException(String.format(
					ERR_UNEXPECTED_PROPERTY_RANGE, 
					property.getRange(), 
					property));
	}
	
	public int compareNumericValue(T property, Long value) {
		Preconditions.checkArgument(property.isNumeric(),ERR_PROPERTY_MUST_BE_NUMERIC, property);
		if(property.getRange().equals(Double.class)) 
			return Double.compare(getPropertyDoubleValue(property), value);
		else if(property.getRange().equals(Integer.class)) 
			return Long.compare(getPropertyIntegerValue(property), value);
		else if(property.getRange().equals(Float.class)) 
			return Double.compare(getPropertyFloatValue(property), value);
		else if(property.getRange().equals(Long.class)) 
			return Long.compare(getPropertyLongValue(property), value);
		else 
			throw new IllegalArgumentException(String.format(
					ERR_UNEXPECTED_PROPERTY_RANGE, 
					property.getRange(), 
					property));
	}

	public boolean isNumericValueGT(T property, Double value) {
		return compareNumericValue(property, value) > 0;
	}

	public boolean isNumericValueGTE(T property, Double value) {
		return compareNumericValue(property, value) >= 0;
	}

	public boolean isNumericValueLT(T property, Double value) {
		return compareNumericValue(property, value) < 0;
	}

	public boolean isNumericValueLTE(T property, Double value) {
		return compareNumericValue(property, value) <= 0;
	}
	
	public Map<T, Comparable<?>> getProperties() {
		return Collections.unmodifiableMap(this.properties);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PropertyHolder<?>) {
			PropertyHolder<?> ph = (PropertyHolder<?>) obj;
			return Objects.equal(properties, ph.properties);
		} else
			return false;
	}
}
