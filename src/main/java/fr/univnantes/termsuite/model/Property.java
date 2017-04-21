package fr.univnantes.termsuite.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public interface Property<T> {
	public Class<?> getRange();

	public String getPropertyName();

	public boolean isDecimalNumber();

	public boolean isNumeric();

	public int compare(T o1, T o2);

	public String getJsonField();

	public Comparator<T> getComparator();
	public Comparator<T> getComparator(boolean reverse);
	public String getDescription();
	
	
	public static <T> Property<T> forName(String name, Property<T>[] values) {
		if(forNameOptional(name, values).isPresent())
			return forNameOptional(name, values).get();
		else {
			List<String> allowed = new ArrayList<>();
			for(Property<T> p:values) {
				allowed.add(String.format("%s[%s]", 
						p.getPropertyName(),
						p.getJsonField()));
			}
			
			throw new IllegalArgumentException(
					String.format(
							"No such property name: %s. Allowed: %s", 
							name,
							Joiner.on(',').join(allowed)
							)
					);
		}

	}
	
	public static <T> Optional<Property<T>> forNameOptional(String name, Property<T>[] values) {
		Optional<Property<T>> p = forNameStrictCase(name, values);
		if(p.isPresent())
			return p;
		else
			return forNameIgnoreCase(name, values);
	}
	
	public static <T> Optional<Property<T>> forNameStrictCase(String name, Property<T>[] values) {
		Preconditions.checkNotNull(name);
		for(Property<T> p:values) {
			if(p.getPropertyName().equals(name))
				return Optional.of(p);
		}
		for(Property<T> p:values) {
			if(p.getJsonField().equals(name))
				return Optional.of(p);
		}
		return Optional.empty();
	}

	public static <T> Optional<Property<T>> forNameIgnoreCase(String name, Property<T>[] values) {
		Preconditions.checkNotNull(name);
		for(Property<T> p:values) {
			if(p.getPropertyName().equalsIgnoreCase(name))
				return Optional.of(p);
		}
		for(Property<T> p:values) {
			if(p.getJsonField().equalsIgnoreCase(name))
				return Optional.of(p);
		}
		return Optional.empty();
	}
}
