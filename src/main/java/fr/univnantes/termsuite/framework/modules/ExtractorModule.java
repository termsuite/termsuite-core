package fr.univnantes.termsuite.framework.modules;

import java.util.Optional;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import fr.univnantes.termsuite.engines.gatherer.GroovyService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.TermHistory;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.filter.resources.FilterResource;

public class ExtractorModule extends AbstractModule {
	private Terminology terminology;
	private Optional<TermHistory> history = Optional.empty();

	public ExtractorModule(Terminology terminology) {
		Preconditions.checkNotNull(terminology, "Terminology cannot be null");
		this.terminology = terminology;
	}

	public ExtractorModule(Terminology terminology,
			TermHistory history) {
		this(terminology);
		this.history = Optional.ofNullable(history);
	}

	@Override
	protected void configure() {
		bind(new TypeLiteral<Optional<TermHistory>>(){}).toInstance(history);
		bind(FilterResource.class).to(DefaultFilterResource.class);
		bind(Terminology.class).toInstance(terminology);
		bind(Lang.class).toInstance(terminology.getLang());
		bind(OccurrenceStore.class).toInstance(terminology.getOccurrenceStore());
		bind(TerminologyService.class).in(Singleton.class);
		bind(GroovyService.class).in(Singleton.class);
	}
}
