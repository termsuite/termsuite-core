package fr.univnantes.termsuite.model;

import java.util.Comparator;

public interface Property<T> {

	public Class<?> getRange();

	public String getPropertyName();

	public boolean isDecimalNumber();

	public String getShortName();

	public boolean isNumeric();

	public int compare(T o1, T o2);

	public String getJsonField();
	
	public Comparator<T> getComparator();
	public Comparator<T> getComparator(boolean reverse);
	
	static boolean isNumeric(Class<?> cls) {
		return cls.equals(Integer.class)
				|| cls.equals(Double.class)
				|| cls.equals(Float.class)
				|| cls.equals(Long.class)
				;
	}

	static boolean isDecimalNumber(Class<?> cls) {
		return cls.equals(Double.class)
				|| cls.equals(Float.class)
				;
	}
}
