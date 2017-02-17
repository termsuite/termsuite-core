package fr.univnantes.termsuite.engines.prepare;

import java.util.Set;
import java.util.stream.Collectors;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * An engine that sets for each {@link TermWord} it {@link TermWord#isSwt()} flag
 * 
 * @author Damien Cram
 *
 */
public class SWTFlagSetter extends SimpleEngine {
	
	@Override
	public void execute() {
		Set<String> swts = terminology.getTerms().stream()
				.filter(TermService::isSingleWord)
				.map(TermService::getGroupingKey)
				.collect(Collectors.toSet());
		for(TermService t:terminology.getTerms()) {
			for(TermWord tw:t.getWords()) 
				tw.setSwt(swts.contains(TermUtils.toGroupingKey(tw)));
		}
		
	}

}
