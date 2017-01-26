package fr.univnantes.termsuite;

import java.util.Optional;

import javax.inject.Inject;

import fr.univnantes.termsuite.framework.Engine;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.utils.TermHistory;

public abstract class SimpleEngine extends Engine {

	@Inject 
	protected TerminologyService terminology;

	protected Optional<TermHistory> history = Optional.empty();

	public abstract void execute();
}
