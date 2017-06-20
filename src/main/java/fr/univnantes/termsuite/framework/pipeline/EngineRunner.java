package fr.univnantes.termsuite.framework.pipeline;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.apache.uima.resource.SharedResourceObject;

import com.google.inject.Injector;

import fr.univnantes.termsuite.api.PipelineListener;
import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.Index;
import fr.univnantes.termsuite.framework.PipelineStats;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.TermSuiteResource;
import fr.univnantes.termsuite.framework.injector.EngineInjector;
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

	protected PipelineStats stats;
	
	protected PipelineListener listener;

	
	public EngineRunner(EngineDescription description, Injector injector, EngineRunner parent) {
		super();
		this.description = description;
		this.resourceMgr = injector.getInstance(TermSuiteResourceManager.class);
		this.indexService = injector.getInstance(IndexService.class);
		this.injector = injector;
		this.engineInjector = new EngineInjector(resourceMgr, indexService);
		this.parent = Optional.ofNullable(parent);
		this.stats = injector.getInstance(PipelineStats.class);
		this.listener = injector.getInstance(PipelineListener.class);
	}
	

	
	protected abstract List<SimpleEngineRunner> getSimpleEngines();
	
	protected EngineRunner getRootRunner() {
		if(parent.isPresent())
			return parent.get().getRootRunner();
		else
			return this;
	}
	
	protected int getTotalSimpleEngines() {
		return getRootRunner().getSimpleEngines().size();
	}


	public void configure() {
		for(Field field:engineInjector.getAnnotatedFields(description.getEngineClass(), Resource.class, SharedResourceObject.class, TermSuiteResource.class)) {
			Resource annotation = field.getAnnotation(Resource.class);
			ResourceType resourceType = annotation.type();
			resourceMgr.registerResource(description.getEngineName(), resourceType);
		}
	}

	public abstract EngineStats run();
	
	protected void releaseResources() {
		resourceMgr.release(description.getEngineName());
	}

	protected void dropIndexes() {
		for(Field field:engineInjector.getAnnotatedFields(description.getEngineClass(), Index.class, TermIndex.class)) {
			Index annotation = field.getAnnotation(Index.class);
			TermIndexType type = annotation.type();
			indexService.dropIndex(type);
		}
	}
}
