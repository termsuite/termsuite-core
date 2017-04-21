package fr.univnantes.termsuite.model;

import java.util.Comparator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

public class PropertyHolderBase<U extends Enum<U> & Property<T>, T extends PropertyHolder<U>>  {
	private static final String MSG_RANGE_NOT_COMPARABLE = "Range of property %s is not comparable";

	private String propertyName;
	private String jsonField;
	private Class<?> range;
	private String description;
	
	public PropertyHolderBase(String propertyName, String jsonField, Class<?> range, String description) {
		super();
		this.propertyName = propertyName;
		this.jsonField = jsonField;
		this.range = range;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getPropertyName() {
		return propertyName;
	}

	public String getJsonField() {
		return jsonField;
	}
	
	public Class<?> getRange() {
		return range;
	}
	
	public boolean isNumeric() {
		return range.equals(Integer.class)
				|| range.equals(Double.class)
				|| range.equals(Float.class)
				|| range.equals(Long.class)
				;
	}

	public boolean isDecimalNumber() {
		return range.equals(Double.class)
				|| range.equals(Float.class)
				;
	}
	
	public Comparator<T> getComparator(U property) {
		return getComparator(property, false);
	}
	
	public int compare(U property, T o1, T o2) {
		if(o1.getPropertyValueUnchecked(property) == o2.getPropertyValueUnchecked(property))
			return 0;
		else if(o1.getPropertyValueUnchecked(property)==null)
			return -1;
		else if(o2.getPropertyValueUnchecked(property)==null)
			return 1;
		else
			return ComparisonChain.start()
				.compare(
						(Comparable<?>)o1.getPropertyValueUnchecked(property), 
						(Comparable<?>)o2.getPropertyValueUnchecked(property))
				.result();
	}

	
	public Comparator<T> getComparator(U property, final boolean reverse) {
		checkComparable(getRange(), this);
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return reverse ? property.compare(o2, o1) : property.compare(o1, o2) ;
			}
		};
	}

	public static void checkComparable(Class<?> cls, Object instance) {
		Preconditions.checkArgument(Comparable.class.isAssignableFrom(cls), MSG_RANGE_NOT_COMPARABLE, instance);
	}
	

	@SuppressWarnings("unchecked")
	public static <U extends Enum<U> & Property<?>, T extends PropertyHolder<U>> U fromJsonString(Class<U> cls, String field) {
		Preconditions.checkNotNull(field);
		for(Enum<?> p:cls.getEnumConstants())
			if(((U)p).getJsonField().equals(field))
				return (U)p;
		throw new IllegalArgumentException("No "+ cls.getSimpleName() +" property with such json field: " + field);
	}
}
