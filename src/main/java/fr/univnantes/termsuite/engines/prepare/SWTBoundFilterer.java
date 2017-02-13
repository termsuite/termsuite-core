package fr.univnantes.termsuite.engines.prepare;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Collectors;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.Term;

/**
 * 
 * An engine that filters out all {@link Term}s having a SWT at
 * its beginning or at its end.
 * 
 * @author Damien Cram
 *
 */
public class SWTBoundFilterer extends SimpleEngine {
	
	@Override
	public void execute() {
		Set<TermService> swtBoundeds = terminology.getTerms().stream()
				.filter(t-> 
					!(t.getWords().get(0).isSwt() && t.getWords().get(t.getWords().size() - 1).isSwt())
				)
				.collect(Collectors.toSet());
		terminology.removeTerms(swtBoundeds.stream().map(TermService::getTerm).collect(toSet()));
	}
}
