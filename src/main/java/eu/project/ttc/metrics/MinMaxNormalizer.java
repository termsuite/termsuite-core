package eu.project.ttc.metrics;

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
	
	public MinMaxNormalizer(double sourceMin, double sourceMax) {
		super();
		this.sourceMin = sourceMin;
		this.sourceMax = sourceMax;
		Preconditions.checkArgument(sourceMin < sourceMax, "Requires sourceMin < sourceMax. Got sourceMin=" + sourceMin + " and sourceMax=" + sourceMax);
	}

	
	public MinMaxNormalizer(Iterable<? extends Number> numbers) {
		sourceMin = Double.MAX_VALUE;
		sourceMax = -Double.MAX_VALUE;
		for(Number n:numbers) {
			if((double)n<sourceMin)
				sourceMin = (double)n;
			if((double)n>sourceMax)
				sourceMax = (double)n;			
		}
		if(sourceMin>sourceMax)
			throw new IllegalArgumentException("Requires at least one number in source vector. Got: " + numbers);
		if(sourceMin==sourceMax)
			throw new IllegalArgumentException("Requires at least two strictly different numbers in source vector. Got: " + numbers);
	}
	
	
	
	@Override
	public double normalize(double value) {
		return (value - sourceMin) / (sourceMax - sourceMin);
	}
}
