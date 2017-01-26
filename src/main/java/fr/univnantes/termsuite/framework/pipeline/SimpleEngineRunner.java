package fr.univnantes.termsuite.framework.pipeline;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;

import fr.univnantes.termsuite.SimpleEngine;
import fr.univnantes.termsuite.framework.EngineDescription;

public class SimpleEngineRunner extends EngineRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEngineRunner.class);

	public SimpleEngineRunner(EngineDescription description, Injector injector, EngineRunner parent) {
		super(description, injector, parent);
	}

	@Override
	public EngineStats run() {

		SimpleEngine engine = (SimpleEngine)injector.getInstance(description.getEngineClass());
		engineInjector.injectName(engine, description.getEngineName());
		engineInjector.injectParameters(engine, description.getParameters());
		engineInjector.injectResources(engine);
		engineInjector.injectIndexes(engine);
		LOGGER.info("Running SimpleEngine {}", description.getEngineName());
		Stopwatch sw = Stopwatch.createStarted();
		engine.execute();
		sw.stop();
		LOGGER.debug("{} finished in {} ms", description.getEngineName(), sw.elapsed(TimeUnit.MILLISECONDS));
		engineInjector.injectNullIndexes(engine);
		engineInjector.injectNullResources(engine);

		releaseResources();
		dropIndexes();
		
		return new EngineStats(description.getEngineName(), sw.elapsed(TimeUnit.MILLISECONDS));
	}
}
