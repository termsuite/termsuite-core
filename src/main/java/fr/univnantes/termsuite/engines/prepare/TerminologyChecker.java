package fr.univnantes.termsuite.engines.prepare;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.Term;

/**
 * 
 * 
 * @author Damien Cram
 *
 */
public class TerminologyChecker extends SimpleEngine {
	@InjectLogger Logger logger;
	
	@Override
	public void execute() {
		List<Term> terms = terminology.terms().map(TermService::getTerm).collect(toList());
		Set<String> keys = terms.stream().map(Term::getGroupingKey).collect(toSet());
		Preconditions.checkState(keys.size() == terms.size(), "Got %s terms but got %s different grouping keys. Expected same number.",
				terms.size(),
				keys.size());
		logger.info("Terminology is valid");
	}
}
