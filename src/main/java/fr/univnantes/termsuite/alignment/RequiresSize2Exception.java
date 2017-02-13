package fr.univnantes.termsuite.alignment;

import java.util.List;

import com.google.common.base.Joiner;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class RequiresSize2Exception extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private Term term;
	private List<Term> swtTerms;
	
	public RequiresSize2Exception(Term term, List<Term> swtTerms) {
		super();
		this.term = term;
		this.swtTerms = swtTerms;
	}

	@Override
	public String getMessage() {
		return String.format(BilingualAlignmentService.MSG_REQUIRES_SIZE_2_LEMMAS, 
			term, 
			Joiner.on(TermSuiteConstants.COMMA).join(swtTerms)
			);
	}
}