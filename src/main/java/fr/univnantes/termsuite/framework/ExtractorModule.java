package fr.univnantes.termsuite.framework;

import javax.inject.Singleton;

import org.apache.uima.resource.SharedResourceObject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.api.TerminologyExtractorOptions;
import fr.univnantes.termsuite.model.Terminology;

public class ExtractorModule extends AbstractModule {
	private Terminology terminology;
	private ResourceConfig resourceConfig;
	private TerminologyExtractorOptions options;

	public ExtractorModule(Terminology terminology, TerminologyExtractorOptions options,
			ResourceConfig resourceConfig) {
		this.terminology = terminology;
		this.resourceConfig = resourceConfig;
		this.options = options;
	}

	@Override
	protected void configure() {
		bind(SharedResourceObject.class)
			.annotatedWith(Resource.class)
			.toProvider(TermSuiteResourceProvider.class);
	}
	
	@Provides @Singleton
	public TerminologyService provideTerminologyService() {
		return new TerminologyService(terminology);
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
