package eu.project.ttc.models.index.selectors;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public interface TermSelector {

	public boolean select(Term t);
	public boolean select(TermIndex termIndex, Term t);
}
