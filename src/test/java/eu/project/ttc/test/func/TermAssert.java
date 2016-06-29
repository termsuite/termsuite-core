package eu.project.ttc.test.func;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.util.Objects;

import com.google.common.base.Joiner;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class TermAssert extends AbstractAssert<TermAssert, Term> {

	protected TermAssert(Term actual) {
		super(actual, TermAssert.class);
	}

	public TermAssert hasGroupingKey(String gKey) {
		isNotNull();
		if (!Objects.areEqual(actual.getGroupingKey(), gKey))
			failWithMessage("Expected term's grouping key to be <%s> but was <%s>", gKey, actual.getGroupingKey());
		return this;
	}

	public TermAssert hasPropertyValue(TermProperty p, Object value) {
		isNotNull();
		if (!Objects.areEqual(p.getValue(actual), value))
			failWithMessage("Expected term's <%s> value to be <%s> but was <%s>", p, value, p.getValue(actual));
		return this;

	}

	public TermAssert hasFrequency(int f) {
		isNotNull();
		if (!Objects.areEqual(actual.getFrequency(), f))
			failWithMessage("Expected term's frequency key to be <%s> but was <%s>", f, actual.getFrequency());
		return this;
	}

	public TermAssert hasAtLeastNVariationsOfType(int atLeastN, VariationType... vType) {
		isNotNull();
		int nb = 0;
		for (TermVariation tv : actual.getVariations(vType))
			nb++;
		if (nb < atLeastN)
			failWithMessage(
					"Expected to find at least <%s> variations of type <%s> for term <%s>, but actually found <%s>",
					atLeastN, vType, actual.getGroupingKey(), nb);
		return this;
	}

	public TermAssert hasNVariationsOfType(int n, VariationType... vType) {
		isNotNull();
		int nb = 0;
		for (TermVariation tv : actual.getVariations(vType))
			nb++;
		if (nb != n)
			failWithMessage("Expected to find <%s> variations of type <%s> for term <%s>, but actually found <%s>", n,
					vType, actual.getGroupingKey(), nb);
		return this;
	}

	public TermAssert hasAtLeastNBasesOfType(int atLeastN, VariationType... vTypes) {
		isNotNull();
		int nb = 0;
		for (TermVariation tv : actual.getBases(vTypes))
			nb++;
		if (nb < atLeastN)
			failWithMessage("Expected to find at least <%s> bases <%s> for term <%s>, but actually found <%s>",
					atLeastN,
					(vTypes.length == 1 ? "of type " : "of any of these types ") + Joiner.on(" ").join(vTypes),
					actual.getGroupingKey(), nb);
		return this;
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getVariations() {
		return assertThat(actual.getVariations());
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getVariationsOfType(VariationType... types) {
		return assertThat(actual.getVariations(types));
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getBases() {
		return assertThat(actual.getBases());
	}
	
	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getBasesOfType(VariationType... types) {
		return assertThat(actual.getBases(types));
	}

	public TermAssert hasNBases(int expectedNumberOfBases) {
		if(actual.getBases().size() != expectedNumberOfBases)
			failWithMessage("Expected <%s> bases but got <%s> (<%s>)", 
					expectedNumberOfBases,
					actual.getBases().size(),
					actual.getBases());
		return this;
	}
	
	public TermAssert hasNVariations(int expectedNumberOfVariations) {
		if(actual.getBases().size() != expectedNumberOfVariations)
			failWithMessage("Expected <%s> variations but got <%s> (<%s>)", 
					expectedNumberOfVariations,
					actual.getVariations().size(),
					actual.getVariations());
		return this;
	}


}
