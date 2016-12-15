package fr.univnantes.termsuite.framework;

import java.util.Comparator;

import com.google.common.collect.Ordering;

import fr.univnantes.termsuite.metrics.HarmonicMean;
import fr.univnantes.termsuite.model.TermRelation;

public class RelationComparators {
	
	public static Comparator<TermRelation> relFreqHmean() {
		final HarmonicMean harmonicMean = new HarmonicMean();
		return Ordering
				.natural()
				.reverse()
				.onResultOf(r ->
					harmonicMean.mean(r.getFrom().getFrequency(), r.getTo().getFrequency())
				);
	}

}
