package fr.univnantes.termsuite.framework;

import java.lang.reflect.Field;
import java.util.Optional;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.engines.gatherer.GroovyService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.utils.TermHistory;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.filter.resources.FilterResource;

public class ExtractorModule extends AbstractModule {
	private Terminology terminology;
	private Optional<ResourceConfig> resourceConfig = Optional.empty();
	private Optional<TermHistory> history = Optional.empty();
	private TermSuiteResourceManager resourceMgr;


	public ExtractorModule(Terminology terminology,
			ResourceConfig resourceConfig, 
			TermHistory history) {
		super();
		Preconditions.checkNotNull(terminology, "Terminology cannot be null");
		this.terminology = terminology;
		this.resourceConfig = Optional.ofNullable(resourceConfig);
		this.resourceMgr = new TermSuiteResourceManager(terminology.getLang(), resourceConfig);
		this.history = Optional.ofNullable(history);
	}

	@Override
	protected void configure() {
		bind(new TypeLiteral<Optional<TermHistory>>(){}).toInstance(history);
		bind(ResourceConfig.class).toInstance(resourceConfig.orElse(new ResourceConfig()));
		bind(FilterResource.class).to(DefaultFilterResource.class);
		bind(Terminology.class).toInstance(terminology);
		bind(OccurrenceStore.class).toInstance(terminology.getOccurrenceStore());
		bind(TerminologyService.class).in(Singleton.class);
		bind(GroovyService.class).in(Singleton.class);
		bind(TermSuiteResourceManager.class).toInstance(resourceMgr);
		bindListener(Matchers.any(), resourceInjectableListener);
	}
	
	private TypeListener resourceInjectableListener = new TypeListener() {
		@Override
		public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
			Class<? super I> cls = type.getRawType();
			while (cls != null) {
				for (Field field : cls.getDeclaredFields()) {
					if(field.isAnnotationPresent(Resource.class)) {
						Resource annotation = field.getAnnotation(Resource.class);
						ResourceType type2 = annotation.type();
						encounter.register(new ResourceInjector<I>(
								field, 
								type2,
								resourceMgr));
					}
				}
				cls = cls.getSuperclass();
			}
		}
	};
}
