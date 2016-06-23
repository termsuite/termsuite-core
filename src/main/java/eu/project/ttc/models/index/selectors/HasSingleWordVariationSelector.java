package eu.project.ttc.models.index.selectors;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.utils.TermUtils;

/**
 * 
 * Selects a term if any of its single-word sub-terms 
 * has a variation or a base of given type.
 * 
 * @author Damien Cram
 *
 */
public class HasSingleWordVariationSelector extends AbstractTermSelector {

	private VariationType variationType;
	
	public HasSingleWordVariationSelector(VariationType variationType) {
		super(true);
		this.variationType = variationType;
	}

	public VariationType getVariationType() {
		return variationType;
	}
	
	@Override
	public boolean select(TermIndex termIndex, Term term) {
		Term swt;
		for (TermWord termWord : term.getWords()) {
			swt = termIndex.getTermByGroupingKey(TermUtils.toGroupingKey(termWord));
			if (swt != null) {
				if(swt.getBases(this.variationType).iterator().hasNext()
					|| swt.getVariations(this.variationType).iterator().hasNext())
					return true;
			}
		}
		return false;
	}

}
