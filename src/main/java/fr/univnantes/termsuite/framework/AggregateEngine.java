package fr.univnantes.termsuite.framework;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateEngine extends Engine {

	public abstract void configure();
	
	private List<EngineDescription> childEngineDescriptions = new ArrayList<>();

	
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
