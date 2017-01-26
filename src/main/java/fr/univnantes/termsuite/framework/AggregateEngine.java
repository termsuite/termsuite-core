package fr.univnantes.termsuite.framework;

import java.util.List;

import org.assertj.core.util.Lists;

public abstract class AggregateEngine extends Engine {

	public abstract void configure();
	
	private List<EngineDescription> childEngineDescriptions = Lists.newArrayList();

	
	protected EngineDescription pipe(String engineName, Class<? extends Engine> engineClass, Object... parameters) {
		EngineDescription childDesc = new EngineDescription(engineName, engineClass, parameters);
		pipelineService.registerEngineName(childDesc.getEngineName());
		childEngineDescriptions.add(childDesc);
		return childDesc;
	}
	
	protected EngineDescription pipe(Class<? extends Engine> engineClass, Object... parameters) {
		return pipe(engineClass.getSimpleName(), engineClass, parameters);
	}

	public List<EngineDescription> getEngineDescriptions() {
		return childEngineDescriptions;
	}
}
