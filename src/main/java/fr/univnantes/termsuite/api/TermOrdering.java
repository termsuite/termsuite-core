package fr.univnantes.termsuite.api;

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
}
