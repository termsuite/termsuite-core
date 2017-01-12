package fr.univnantes.termsuite.framework;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import fr.univnantes.termsuite.framework.service.CorpusService;
import fr.univnantes.termsuite.framework.service.CorpusServiceImpl;
import fr.univnantes.termsuite.framework.service.PreprocessorService;
import fr.univnantes.termsuite.framework.service.PreprocessorServiceImpl;

public class TermSuiteModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Object.class).annotatedWith(Resource.class).toProvider(TermSuiteResourceProvider.class);
		bind(CorpusService.class).to(CorpusServiceImpl.class).in(Singleton.class);
		bind(PreprocessorService.class).to(PreprocessorServiceImpl.class).in(Singleton.class);
	}

	public <T> T createEngine(Class<T> engineClass) {
		T engine;
		try {
			engine = engineClass.newInstance();
			
			return engine;
		} catch (Exception e) {
			throw new TermSuiteFrameworkException();
		}

	}
	
}
