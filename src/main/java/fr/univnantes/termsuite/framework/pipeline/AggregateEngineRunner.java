package fr.univnantes.termsuite.framework.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public void run() {
		LOGGER.info("Running Aggregate Engine {}", description.getEngineName());
		for(EngineRunner child:children) {
			child.run();
		}
	}
}
