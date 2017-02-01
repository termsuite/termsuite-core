package fr.univnantes.termsuite.framework;

import java.util.Comparator;
import java.util.function.Predicate;

import com.google.common.collect.Ordering;

import fr.univnantes.termsuite.metrics.HarmonicMean;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Relation;

public class Relations {
	
	public static Comparator<Relation> relFreqHmean() {
		final HarmonicMean harmonicMean = new HarmonicMean();
		return Ordering
				.natural()
				.reverse()
				.onResultOf(r ->
					harmonicMean.mean(r.getFrom().getFrequency(), r.getTo().getFrequency())
				);
	}
	
	private static class BooleanPropertyPredicate implements Predicate<Relation> {
		private RelationProperty property;
		private boolean logicalNot;
		
		public BooleanPropertyPredicate(RelationProperty property) {
			this(property, false);
		}

		public BooleanPropertyPredicate(RelationProperty property, boolean logicalNot) {
			super();
			this.property = property;
			this.logicalNot = logicalNot;
		}

		@Override
		public boolean test(Relation t) {
			boolean b = t.isPropertySet(property)
					&& t.getPropertyBooleanValue(property);
			return logicalNot ? !b : b;
		}
	}

	public static Predicate<Relation> IS_MORPHOLOGICAL = new BooleanPropertyPredicate(RelationProperty.IS_MORPHOLOGICAL);
	public static Predicate<Relation> IS_DERIVATION = new BooleanPropertyPredicate(RelationProperty.IS_DERIVATION);
	public static Predicate<Relation> IS_PREFIXATION = new BooleanPropertyPredicate(RelationProperty.IS_PREFIXATION);
	public static Predicate<Relation> IS_SYNTAGMATIC = new BooleanPropertyPredicate(RelationProperty.IS_SYNTAGMATIC);
	public static Predicate<Relation> IS_GRAPHICAL = new BooleanPropertyPredicate(RelationProperty.IS_GRAPHICAL);
	public static Predicate<Relation> IS_SEMANTIC = new BooleanPropertyPredicate(RelationProperty.IS_SEMANTIC);
	public static Predicate<Relation> IS_INFERENCE = new BooleanPropertyPredicate(RelationProperty.IS_INFERED);
	public static Predicate<Relation> NOT_INFERED = new BooleanPropertyPredicate(RelationProperty.IS_INFERED, true);


}
