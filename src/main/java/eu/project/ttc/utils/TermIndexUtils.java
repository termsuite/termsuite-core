package eu.project.ttc.utils;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class TermIndexUtils {

	public static Set<TermVariation> getVariations(TermIndex termIndex) {
		Set<TermVariation> variations = Sets.newHashSet();
		for(Term t:termIndex.getTerms()) {
			for(TermVariation tv:t.getVariations())
				variations.add(tv);
			for(TermVariation tv:t.getBases())
				variations.add(tv);
		}
		return variations;
	}


	public static  Set<TermVariation> selectTermVariations(TermIndex termIndex, VariationType type, String ruleName) {
		Set<TermVariation> selected = Sets.newHashSet();
		for(TermVariation tv:selectTermVariations(termIndex, type))
			if(Objects.equal(ruleName, tv.getInfo()))
				selected.add(tv);
		return selected;
	}
	
	public static  Set<TermVariation> selectTermVariations(TermIndex termIndex, VariationType... types) {
		Set<TermVariation> selected = Sets.newHashSet();
		for(TermVariation tv:getVariations(termIndex))
			for(VariationType type:types)
				if(tv.getVariationType() == type)
					selected.add(tv);
		return selected;
	}


	public static Collection<Term> selectCompounds(TermIndex termIndex) {
		Set<Term> compounds = Sets.newHashSet();
		for(Term t:termIndex.getTerms())
			if(t.isSingleWord() && t.isCompound())
				compounds.add(t);
		return compounds;
	}
}
