package fr.univnantes.termsuite.framework.pipeline;

import com.google.inject.Injector;

import fr.univnantes.termsuite.SimpleEngine;
import fr.univnantes.termsuite.framework.EngineDescription;

public class SimpleEngineRunner extends EngineRunner {

	public SimpleEngineRunner(EngineDescription description, Injector injector, EngineRunner parent) {
		super(description, injector, parent);
	}

	@Override
	public void run() {
		SimpleEngine engine = (SimpleEngine)injector.getInstance(description.getEngineClass());
		engineInjector.injectName(engine, description.getEngineName());
		engineInjector.injectParameters(engine, description.getParameters());
		engineInjector.injectResources(engine);
		engineInjector.injectIndexes(engine);
		engine.execute();
		engineInjector.injectNullIndexes(engine);
		engineInjector.injectNullResources(engine);

		releaseResources();
		dropIndexes();
	}
}
