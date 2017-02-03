package fr.univnantes.termsuite.api;

import java.util.Comparator;

import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

public class TermOrdering extends Ordering<TermProperty, Term, TermOrdering>{
	public static TermOrdering natural() {
		return new TermOrdering()
				.asc(TermProperty.RANK)
				.desc(TermProperty.SPECIFICITY)
				.desc(TermProperty.FREQUENCY)
				.asc(TermProperty.GROUPING_KEY)
				;
	}

	public Comparator<? super TermService> toTermServiceComparator() {
		final Comparator<Term> termComp = toComparator();
		Comparator<TermService> comparator = new Comparator<TermService>() {
			@Override
			public int compare(TermService o1, TermService o2) {
				return termComp.compare(o1.getTerm(), o2.getTerm());
			}
		};
		return comparator;
	}
}
