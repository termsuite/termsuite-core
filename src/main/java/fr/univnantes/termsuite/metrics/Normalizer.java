package fr.univnantes.termsuite.metrics;

import java.util.ArrayList;
import java.util.Collection;

public interface Normalizer {
	public static boolean areAllNaNs(Iterable<? extends Number> numbers) {
		Collection<Number> valids = new ArrayList<>();
		int nbNan = 0;
		for(Number n:numbers) {
			if(Double.isNaN((double)n))
				nbNan++;				
			else
				valids.add(n);
		}
		return valids.isEmpty() && nbNan > 0;
	}

	public double normalize(double value);
}
