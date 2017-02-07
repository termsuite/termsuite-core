package fr.univnantes.termsuite.test.unit;

import static java.util.stream.Collectors.joining;

import java.util.Set;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;

import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.utils.VariationUtils;

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
					VariationUtils.toTagString(input),
					getRuleString(input),
					input.getTo());
		}
	};


	public static final Extractor<Relation, Tuple> VARIATION_FROM_TYPE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.getFrom(),
					VariationUtils.toTagString(input),
					input.getTo());
		}

	};
	public static final Extractor<Relation, Tuple> VARIATION_TYPE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(VariationUtils.toTagString(input),
					input.getTo());
		}
	};
	
	public static final Extractor<Relation, Tuple> RELATION_FROM_RULE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(input.getFrom(),
					getRuleString(input),
					input.getTo());
		}
	};

	public static final Extractor<Relation, Tuple> RELATION_TYPE_RULE_TO = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.getType(),
					getRuleString(input),
					input.getTo());
		}

	};

	public static final Extractor<Relation, String> RELATION_RULESTR = new Extractor<Relation, String>() {
		@Override
		public String extract(Relation input) {
			return getRuleString(input);
		}

	};
	
	@SuppressWarnings("unchecked")
	public static String getRuleString(Relation input) {
		if(input.isPropertySet(RelationProperty.VARIATION_RULES))
			return ((Set<String>)input.get(RelationProperty.VARIATION_RULES)).stream().collect(joining(","));
		else
			return "";
	}

	public static final Extractor<Relation, Tuple> RELATION_TOGKEY_RULE_TOFREQ = new Extractor<Relation, Tuple>() {
		@Override
		public Tuple extract(Relation input) {
			return new Tuple(
					input.getTo().getGroupingKey(),
					getRuleString(input),
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
