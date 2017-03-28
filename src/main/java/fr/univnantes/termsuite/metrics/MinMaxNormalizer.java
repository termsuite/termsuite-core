package fr.univnantes.termsuite.metrics;

import com.google.common.base.Preconditions;

/**
 * 
 * Operates a min-max uniform normalization
 * 
 * @author Damien Cram
 *
 */
public class MinMaxNormalizer implements Normalizer {

	private double sourceMin;
	private double sourceMax;
	private boolean allNans = false;
	
	public MinMaxNormalizer(double sourceMin, double sourceMax) {
		super();
		this.sourceMin = sourceMin;
		this.sourceMax = sourceMax;
		Preconditions.checkArgument(sourceMin < sourceMax, "Requires sourceMin < sourceMax. Got sourceMin=" + sourceMin + " and sourceMax=" + sourceMax);
	}

	
	public MinMaxNormalizer(Iterable<? extends Number> numbers) {
		allNans = Normalizer.areAllNaNs(numbers);
		
		if(!allNans) {
			sourceMin = Double.MAX_VALUE;
			sourceMax = -Double.MAX_VALUE;
			for(Number n:numbers) {
				double current = (double)n;
				if(current<sourceMin)
					sourceMin = current;
				if(current>sourceMax)
					sourceMax = current;
			}
			if(sourceMin>sourceMax)
				throw new IllegalArgumentException("Requires at least one number in source vector. Got: " + numbers);
			if(sourceMin==sourceMax)
				throw new IllegalArgumentException("Requires at least two strictly different numbers in source vector. Got: " + numbers);
		}
	}


	@Override
	public double normalize(double value) {
		if(allNans)
			return value;
			else
		return 
			(value - sourceMin) / (sourceMax - sourceMin);
	}
}
