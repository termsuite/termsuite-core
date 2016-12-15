package fr.univnantes.termsuite.metrics;

import java.util.Collection;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

public class HarmonicMean implements Mean {
	private static String ERR_EMPTY = "Cannot compute mean of an empty set";

	@Override
	public double mean(Collection<Double> values) {
		return mean(Doubles.toArray(values));
	}

	@Override
	public double mean(double... values) {
		Preconditions.checkArgument(values.length > 0, ERR_EMPTY);
		double sum = 0d;
		for(double v:values)
			sum+=1d/v;
		return (double)values.length/sum;
	}

}
