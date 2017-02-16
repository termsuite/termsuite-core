package fr.univnantes.termsuite.framework.modules;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import fr.univnantes.termsuite.framework.service.ImporterService;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.OccurrenceStore;

public class ImporterModule extends AbstractModule {
	
	private int maxSize = -1;
	private Terminology terminology;
	private OccurrenceStore occurrenceStore;
	
	public ImporterModule(IndexedCorpus corpus, int maxSize) {
		super();
		this.maxSize = maxSize;
		this.terminology = corpus.getTerminology();
		this.occurrenceStore = corpus.getOccurrenceStore();
	}

	@Override
	protected void configure() {
		bind(ImporterService.class).in(Singleton.class);
		bind(Integer.class).annotatedWith(Names.named("maxSize")).toInstance(maxSize);
		bind(TerminologyService.class).toInstance(new TerminologyService(terminology));
		bind(IndexService.class).toInstance(new IndexService(terminology));
		bind(OccurrenceStore.class).toInstance(occurrenceStore);
	}
}
