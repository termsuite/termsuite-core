package fr.univnantes.termsuite.metrics;

import java.util.Collection;

public interface Mean {
	public double mean(double... values);
	public double mean(Collection<Double> values);
}
