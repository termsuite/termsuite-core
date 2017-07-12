package fr.univnantes.termsuite.framework.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.AggregateEngine;
import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.TermSuiteFactory;

public class AggregateEngineRunner extends EngineRunner {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AggregateEngineRunner.class);
	
	private List<EngineRunner> children;
	
	public AggregateEngineRunner(EngineDescription description, Injector injector, EngineRunner parent) {
		super(description, injector, parent);
	}

	@Override
	public void configure() {
		children = new ArrayList<>();
		AggregateEngine engine;
		engine = (AggregateEngine) injector.getInstance(description.getEngineClass());
		engineInjector.injectName(engine, description.getEngineName());
		engineInjector.injectParameters(engine, description.getParameters());
		LOGGER.debug("Configuring {}", engine.getEngineName());
		engine.configure();
		for(EngineDescription description:engine.getEngineDescriptions()) {
			EngineRunner createEngineRunner = TermSuiteFactory.createEngineRunner(description, injector, this);
			createEngineRunner.configure();
			children.add(createEngineRunner);
		}
	}
	
	@Override
	public EngineStats run() {
		LOGGER.info("Running Aggregate Engine {}", description.getEngineName());
		Stopwatch sw = Stopwatch.createStarted();
		List<EngineStats> childStats = new ArrayList<>();
		for(EngineRunner child:children) {
			childStats.add(child.run());
		}
		sw.stop();
		
		return new EngineStats(
				description.getEngineName(), 
				sw.elapsed(TimeUnit.MILLISECONDS),
				childStats
			);
	}

	@Override
	protected List<SimpleEngineRunner> getSimpleEngines() {
		List<SimpleEngineRunner> simpleEngines = new ArrayList<>();
		for(EngineRunner child:children)
			simpleEngines.addAll(child.getSimpleEngines());
		return simpleEngines;
	}
}
