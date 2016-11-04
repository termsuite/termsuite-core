package eu.project.ttc.test.unit;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;

import eu.project.ttc.models.RelationProperty;
import eu.project.ttc.models.TermRelation;

public class TermSuiteExtractors {
	
	public static final Extractor<TermRelation, Tuple> RELATION_FROM_TYPE_TO = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(input.getFrom(),
					input.getType(),
					input.getTo());
		}
	};

	public static final Extractor<TermRelation, Tuple> RELATION_FROM_RULE_TO = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(input.getFrom(),
					input.getPropertyValue(RelationProperty.VARIATION_RULE),
					input.getTo());
		}
	};

	public static final Extractor<TermRelation, Tuple> RELATION_TYPE_RULE_TO = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(
					input.getType(),
					input.getPropertyValue(RelationProperty.VARIATION_RULE),
					input.getTo());
		}

	};

	public static final Extractor<TermRelation, String> RELATION_RULESTR = new Extractor<TermRelation, String>() {
		@Override
		public String extract(TermRelation input) {
			return input.getPropertyStringValue(RelationProperty.VARIATION_RULE);
		}

	};

	public static final Extractor<TermRelation, Tuple> RELATION_TOGKEY_RULE_TOFREQ = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(
					input.getTo().getGroupingKey(),
					input.getPropertyValue(RelationProperty.VARIATION_RULE),
					input.getTo().getFrequency());
		}
	};

	public static final Extractor<TermRelation, Tuple> RELATION_DERIVTYPE_FROMGKEY_TOGKEY = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(
					input.getPropertyValue(RelationProperty.DERIVATION_TYPE),
					input.getFrom().getGroupingKey(),
					input.getTo().getGroupingKey()
					);
		}
	};

}
