package fr.univnantes.termsuite.framework;

import java.lang.reflect.Field;
import java.util.Optional;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.engines.gatherer.GroovyService;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.utils.TermHistory;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.filter.resources.FilterResource;

public class ExtractorModule extends AbstractModule {
	private Terminology terminology;
	private ResourceConfig resourceConfig;
	private Optional<TermHistory> history = Optional.empty();
	private TermSuiteResourceManager resourceMgr;

	public ExtractorModule(Terminology terminology,
			ResourceConfig resourceConfig) {
		this.terminology = terminology;
		this.resourceConfig = resourceConfig;
		this.resourceMgr = new TermSuiteResourceManager(terminology.getLang(), resourceConfig);
	}

	public ExtractorModule(Terminology terminology,
			ResourceConfig resourceConfig, TermHistory history) {
		this(terminology, resourceConfig);
		this.history = Optional.ofNullable(history);
	}

	@Override
	protected void configure() {
		bind(new TypeLiteral<Optional<TermHistory>>(){}).toInstance(history);
		bind(Lang.class).toInstance(terminology.getLang());
		bind(ResourceConfig.class).toInstance(resourceConfig);
		bind(FilterResource.class).to(DefaultFilterResource.class);
		bind(Terminology.class).toInstance(terminology);
		bind(ExtractorPipelineLifecycle.class).in(Singleton.class);
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
