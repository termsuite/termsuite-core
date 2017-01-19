package fr.univnantes.termsuite.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.util.Lists;

public abstract class AggregateTerminologyEngine extends TerminologyEngine {

	public abstract void configure();
	
	private List<EngineDescription> engineDescriptions = Lists.newArrayList();
	private List<TerminologyEngine> engines;
	
	protected EngineDescription pipe(String engineName, Class<? extends TerminologyEngine> engineClass, Object... parameters) {
		EngineDescription desc = new EngineDescription(engineName, engineClass, parameters);
		engineDescriptions.add(desc);
		return desc;
	}
	
	protected EngineDescription pipe(Class<? extends TerminologyEngine> engineClass, Object... parameters) {
		return pipe(engineClass.getSimpleName(), engineClass, parameters);
	}

	public List<EngineDescription> getEngineDescriptions() {
		return engineDescriptions;
	}
	
	@Override
	public void execute() {
		for(TerminologyEngine engine:engines)
			engine.execute();
	}

	public void setEngines(Collection<TerminologyEngine> children) {
		this.engines = new ArrayList<>(children);
	}
}
