package fr.univnantes.termsuite.framework.pipeline;

import java.lang.reflect.Field;
import java.util.Optional;

import org.apache.uima.resource.SharedResourceObject;

import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.EngineInjector;
import fr.univnantes.termsuite.framework.Index;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.TermSuiteResource;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.uima.ResourceType;

public abstract class EngineRunner {

	protected Optional<EngineRunner> parent = Optional.empty();
	
	protected EngineDescription description;

	protected TermSuiteResourceManager resourceMgr;

	protected IndexService indexService;
	
	protected Injector injector;

	protected EngineInjector engineInjector;

	public EngineRunner(EngineDescription description, Injector injector, EngineRunner parent) {
		super();
		this.description = description;
		this.resourceMgr = injector.getInstance(TermSuiteResourceManager.class);
		this.indexService = injector.getInstance(IndexService.class);
		this.injector = injector;
		this.engineInjector = new EngineInjector(description.getEngineClass(), injector);
		this.parent = Optional.ofNullable(parent);
	}

	public void configure() {
		for(Field field:engineInjector.getAnnotatedFields(Resource.class, SharedResourceObject.class, TermSuiteResource.class)) {
			Resource annotation = field.getAnnotation(Resource.class);
			ResourceType resourceType = annotation.type();
			resourceMgr.registerResource(description.getEngineName(), resourceType);
		}
	}

	public abstract void run();
	
	protected void releaseResources() {
		resourceMgr.release(description.getEngineName());
	}

	protected void dropIndexes() {
		for(Field field:engineInjector.getAnnotatedFields(Index.class, TermIndex.class)) {
			Index annotation = field.getAnnotation(Index.class);
			TermIndexType type = annotation.type();
			indexService.dropIndex(type);
		}
	}
}
