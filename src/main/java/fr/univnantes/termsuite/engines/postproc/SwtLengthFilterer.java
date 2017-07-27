package fr.univnantes.termsuite.engines.postproc;

import java.util.stream.Collectors;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.resources.PostProcessorOptions;

public class SwtLengthFilterer extends SimpleEngine {
	@InjectLogger Logger logger;

	@Parameter
	private PostProcessorOptions config;

	@Override
	public void execute() {
		String msg = "Removing swt because its length < " + config.getMinSwtLength();
		int cnt = 0;
		for(TermService term:terminology.terms()
				.filter(t -> t.isSingleWord() && t.getWords().get(0).getWord().getLemma().length() < config.getMinSwtLength())
				.collect(Collectors.toSet())
				) {
			watchTermRemoval(term, msg);
			terminology.removeTerm(term);
			cnt++;
		}
		logger.debug("Removed {} SWTs having a length lower than {}", cnt, config.getMinSwtLength());
	}

	
	private void watchTermRemoval(TermService term, String msg) {
		if(history.isPresent() && history.get().isTermWatched(term.getTerm()))
			history.get().saveEvent(
					term, 
					this.getClass(), 
					msg);
	}

}
