package eu.project.ttc.utils;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public class TermIndexUtils {

	public static double getMax(TermIndex termIndex, TermProperty p) {
		double max = Double.MIN_VALUE;
		for(Term t:termIndex.getTerms()) {
			if(p.getDoubleValue(t) > max)
				max = p.getDoubleValue(t);
		}
		return max;
	}
}
