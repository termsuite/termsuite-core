package fr.univnantes.termsuite.test.unit;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Relation;

public class TermSuiteExtractors {
	
	public static final Extractor<Relation, Tuple> RELATION_FROM_TYPE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(input.getFrom(),
					input.getType(),
					input.getTo());
		}
	};

	public static final Extractor<Relation, Tuple> VARIATION_TYPE_RULE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.get(RelationProperty.VARIATION_TYPE),
					input.get(RelationProperty.VARIATION_RULE),
					input.getTo());
		}
	};


	public static final Extractor<Relation, Tuple> VARIATION_FROM_TYPE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.getFrom(),
					input.getPropertyValue(RelationProperty.VARIATION_TYPE),
					input.getTo());
		}

	};
	public static final Extractor<Relation, Tuple> VARIATION_TYPE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(input.getPropertyValue(RelationProperty.VARIATION_TYPE),
					input.getTo());
		}
	};
	
	public static final Extractor<Relation, Tuple> RELATION_FROM_RULE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(input.getFrom(),
					input.getPropertyValue(RelationProperty.VARIATION_RULE),
					input.getTo());
		}
	};

	public static final Extractor<Relation, Tuple> RELATION_TYPE_RULE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.getType(),
					input.getPropertyValue(RelationProperty.VARIATION_RULE),
					input.getTo());
		}

	};

	public static final Extractor<Relation, String> RELATION_RULESTR = new Extractor<Relation, String>() {
		@Override
		public String extract(Relation input) {
			return input.getPropertyStringValue(RelationProperty.VARIATION_RULE);
		}

	};

	public static final Extractor<Relation, Tuple> RELATION_TOGKEY_RULE_TOFREQ = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.getTo().getGroupingKey(),
					input.getPropertyValue(RelationProperty.VARIATION_RULE),
					input.getTo().getFrequency());
		}
	};

	public static final Extractor<Relation, Tuple> RELATION_DERIVTYPE_FROMGKEY_TOGKEY = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.getPropertyValue(RelationProperty.DERIVATION_TYPE),
					input.getFrom().getGroupingKey(),
					input.getTo().getGroupingKey()
					);
		}
	};

}
