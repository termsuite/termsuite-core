package eu.project.ttc.test.func;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.util.Lists;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class TermIndexAssert extends AbstractAssert<TermIndexAssert, TermIndex> {

	protected TermIndexAssert(TermIndex actual) {
		super(actual, TermIndexAssert.class);
	}

	public TermIndexAssert containsVariation(String baseGroupingKey, VariationType type, String variantGroupingKey) {
		List<TermVariation> potentialVariations = Lists.newArrayList();
		Set<TermVariation> sameType = Sets.newHashSet();
		
		for(TermVariation tv:getVariations()) {
			if(tv.getBase().getGroupingKey().equals(baseGroupingKey)
					&& tv.getVariant().getGroupingKey().equals(variantGroupingKey)) {
				potentialVariations.add(tv);
				if(tv.getVariationType() == type)
					return this;
			}
			if(type == tv.getVariationType())
				sameType.add(tv);
		}
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(baseGroupingKey).getVariations(type)));
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(variantGroupingKey).getBases(type)));
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(baseGroupingKey).getVariations()));
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(variantGroupingKey).getBases()));
		potentialVariations.addAll(sameType);
		
		failWithMessage("No such variation <%s--%s--%s> found in term index. Closed variations: <%s>", 
				baseGroupingKey, type, variantGroupingKey,
				Joiner.on(", ").join(potentialVariations.subList(0, Ints.min(10, potentialVariations.size())))
				);
		return this;
	}
	
	public TermIndexAssert containsVariation(String baseGroupingKey, VariationType type, String variantGroupingKey, Object info) {
		List<TermVariation> potentialVariations = Lists.newArrayList();
		Set<TermVariation> sameType = Sets.newHashSet();
		for(TermVariation tv:getVariations()) {
			if(tv.getBase().getGroupingKey().equals(baseGroupingKey)
					&& tv.getVariant().getGroupingKey().equals(variantGroupingKey)) {
				potentialVariations.add(tv);
				if(tv.getVariationType() == type && Objects.equal(info, tv.getInfo()))
					return this;
			}
			if(type == tv.getVariationType())
				sameType.add(tv);
		}
		
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(baseGroupingKey).getVariations(type)));
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(variantGroupingKey).getBases(type)));
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(baseGroupingKey).getVariations()));
		potentialVariations.addAll(Sets.newHashSet(actual.getTermByGroupingKey(variantGroupingKey).getBases()));
		potentialVariations.addAll(sameType);
		
		failWithMessage("No such variation <%s--%s[%s]--%s> found in term index. Closed variations: <%s>", 
				baseGroupingKey, type, 
				info,
				variantGroupingKey,
				Joiner.on(", ").join(potentialVariations)
				);
		return this;
	}


	private Collection<TermVariation> getVariations() {
		Set<TermVariation> termVariations = Sets.newHashSet();
		for(Term t:actual.getTerms()) {
			for(TermVariation v:t.getVariations())
				termVariations.add(v);
		}
		return termVariations;
		
	}

	public TermIndexAssert hasNVariationsOfType(int expected, VariationType type) {
		int cnt = 0;
		for(TermVariation tv:getVariations()) {
			if(tv.getVariationType() == type)
				cnt++;
		}
	
		if(cnt != expected)
			failWithMessage("Expected <%s> variations of type <%s>. Got: <%s>", expected, type, cnt);
		
		return this;
	}

	public AbstractIterableAssert<?, ? extends Iterable<? extends TermVariation>, TermVariation> getVariationsHavingObject(Object object) {
		Set<TermVariation> variations = Sets.newHashSet();
		for(TermVariation v:getVariations())
			if(Objects.equal(v.getInfo(), object))
				variations.add(v);
		return assertThat(variations);
	}

}
