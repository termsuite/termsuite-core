package fr.univnantes.termsuite.framework;

import java.util.List;

import org.assertj.core.util.Lists;

public class TerminologyPipeline {

	private static class EngineDescription {
		Class<? extends TerminologyEngine> engineClass;
		Object[] parameters;
		public EngineDescription(Class<? extends TerminologyEngine> engineClass, Object... parameters) {
			super();
			this.engineClass = engineClass;
			this.parameters = parameters;
		}
	}

	private List<EngineDescription> engines = Lists.newArrayList();
	
	public void requireEngine(Class<? extends TerminologyEngine> engineClass) {
		if(!engines.stream()
				.filter(e -> e.engineClass.equals(engineClass))
				.findAny()
				.isPresent())
			engines.add(new EngineDescription(engineClass));
	}

	public void pipeEngine(Class<? extends TerminologyEngine> engineClass, Object... parameters) {
		engines.add(new EngineDescription(engineClass, parameters));
	}
}
