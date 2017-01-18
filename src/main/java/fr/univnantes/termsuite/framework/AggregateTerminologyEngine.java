package fr.univnantes.termsuite.framework;

import java.util.List;

import org.assertj.core.util.Lists;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public abstract class AggregateTerminologyEngine extends TerminologyEngine {

	public abstract void configure();
	
	private List<EngineDescription> engineDescriptions = Lists.newArrayList();

	
	protected EngineDescription pipe(Class<? extends TerminologyEngine> engineClass, Object... parameters) {
		EngineDescription desc = new EngineDescription(engineClass, parameters);
		engineDescriptions.add(desc);
		return desc;
	}

	private List<TerminologyEngine> engines;
	
	public void initialize(Module extractorModule) {
		getLogger().debug("Initializing {}", this);
		configure();
		engines = Lists.newArrayList();
		for(EngineDescription desc:engineDescriptions) {
			EngineParameterModule engineParameterModule = new EngineParameterModule(desc);
			
			Module childModule = Modules.combine(engineParameterModule, extractorModule);
			Injector engineInjector = Guice.createInjector(childModule);
			TerminologyEngine childEngine = engineInjector.getInstance(desc.getEngineClass());
			engines.add(childEngine);
		}
		
		for(TerminologyEngine child:engines)
			if(child instanceof AggregateTerminologyEngine)
				((AggregateTerminologyEngine)child).initialize(extractorModule);
	}
	
	@Override
	public void execute() {
		for(TerminologyEngine engine:engines)
			engine.execute();
	}
}
