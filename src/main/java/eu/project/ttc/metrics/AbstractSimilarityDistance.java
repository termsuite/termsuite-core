package eu.project.ttc.metrics;

import eu.project.ttc.models.ContextVector;


/**
 * 
 * An abstract implementation of {@link SimilarityDistance} that manages the 
 * similarity explanation.
 * 
 * @see SimilarityDistance
 * @see Explanation
 * @author Damien Cram
 *
 */
public abstract class AbstractSimilarityDistance implements SimilarityDistance {

	private int nbExplanation = 10;

	public AbstractSimilarityDistance() {
		super();
	}
	
	@Override
	public ExplainedValue getExplainedValue(
			ContextVector v1, ContextVector v2) {
		Explanation expl = new Explanation(nbExplanation);
		double value = getValue(v1, v2, expl);
		return new ExplainedValue(value, expl);
	}

	@Override
	public double getValue(ContextVector v1, ContextVector v2) {
		return getValue(v1, v2, Explanation.emptyExplanation());
	}
	
	protected abstract double getValue(ContextVector source, ContextVector target, Explanation explainedValue);
}
