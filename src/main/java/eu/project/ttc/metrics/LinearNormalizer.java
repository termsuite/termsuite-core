package eu.project.ttc.metrics;

/**
 * 
 * Operates a linear normalization to range [0,1].
 * 
 * 	y = a.x + b
 * 
 * If the result of the linear function is greater 
 * (resp. lower) than 1 (resp. 0), its normalized value 
 * returned will be 1 (resp. 0)
 * 
 * @author Damien Cram
 *
 */
public class LinearNormalizer implements Normalizer {

	
	private double a;
	private double b;
	
	public LinearNormalizer(double a, double b) {
		super();
		this.a = a;
		this.b = b;
	}
	
	@Override
	public double normalize(double value) {
		return Math.min(1, Math.max(0,a * value  + b));
	}
}
