package fr.univnantes.termsuite.api;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.cleaner.TerminologyCleaner;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.TermProperty;

public class TerminologyFilterer {

	private TerminoFilterOptions options;
	public TerminologyFilterer(TerminoFilterOptions options) {
		super();
		Preconditions.checkNotNull(options);
		this.options = options;
		this.options.setEnabled(true);
	}
	
	public TerminologyFilterer by(TermProperty p) {
		options.by(p);
		return this;
	}

	public TerminologyFilterer setProperty(TermProperty p) {
		options.setProperty(p);
		return this;
	}

	public TerminologyFilterer keepOverTh(Number threshold) {
		options.keepOverTh(threshold);
		return this;
	}

	public TerminologyFilterer setMaxVariantNum(int maxNumberOfVariants) {
		options.setMaxVariantNum(maxNumberOfVariants);
		return this;
	}

	public TerminologyFilterer keepTopN(int n) {
		options.keepTopN(n);
		return this;
	}

	public TerminologyFilterer keepVariants() {
		options.keepVariants();
		return this;
	}

	public TerminologyFilterer setKeepVariants(boolean keepVariants) {
		options.setKeepVariants(keepVariants);
		return this;
	}

	public void filter(Terminology terminology) {
		filter(TermSuiteFactory.createIndexedCorpus(terminology, TermSuiteFactory.createEmptyOccurrenceStore(terminology.getLang())));
	}

	public void filter(IndexedCorpus corpus) {
		TermSuiteFactory.createEngineRunner(TerminologyCleaner.class, corpus, null, null, options)
			.run();
	}
}
