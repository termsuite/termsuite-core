package eu.project.ttc.models.index.selectors;

import com.google.common.base.Preconditions;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public abstract class AbstractTermSelector implements TermSelector {

	private boolean requiredTermIndex;
	
	public AbstractTermSelector(boolean requiredTermIndex) {
		super();
		this.requiredTermIndex = requiredTermIndex;
	}

	@Override
	public boolean select(Term t) {
		String ERR_TERM_INDEX_REQUIRED = "TermIndex is required for selector %s. You must invoke the #select(TermIndex, Term).";
		Preconditions.checkState(
				!this.requiredTermIndex,
				ERR_TERM_INDEX_REQUIRED,
				this.getClass().getName());
		return select(null, t);
	}

	public abstract boolean select(TermIndex termIndex, Term t);
}
