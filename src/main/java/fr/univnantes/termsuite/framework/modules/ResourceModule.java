package fr.univnantes.termsuite.framework.modules;

import java.util.Optional;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;

public class ResourceModule extends AbstractModule {

	private Optional<ResourceConfig> resourceConfig = Optional.empty();
	
	public ResourceModule() {
	}

	public ResourceModule(ResourceConfig resourceConfig) {
		super();
		this.resourceConfig = Optional.ofNullable(resourceConfig);
	}

	@Override
	protected void configure() {
		bind(ResourceConfig.class).toInstance(resourceConfig.orElse(new ResourceConfig()));
		bind(TermSuiteResourceManager.class).in(Singleton.class);
	}
}
