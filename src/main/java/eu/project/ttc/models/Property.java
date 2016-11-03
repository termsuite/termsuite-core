package eu.project.ttc.models;

public interface Property<T> {

	public Class<?> getRange();

	public String getPropertyName();

	public boolean isDecimalNumber();

	public String getShortName();

	public boolean isNumeric();

	public int compare(T o1, T o2);
}
