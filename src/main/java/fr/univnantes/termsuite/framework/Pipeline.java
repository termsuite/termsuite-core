package fr.univnantes.termsuite.framework;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.framework.pipeline.EngineStats;
import fr.univnantes.termsuite.framework.service.IndexService;

public class Pipeline {
	@Inject
	private IndexService indexService;
	
	@Inject
	PipelineStats stats;
	
	private EngineRunner runner;
	
	public PipelineStats run() {
		Stopwatch sw = Stopwatch.createStarted();
		runner.configure();
		EngineStats engineStats = runner.run();
		sw.stop();
		stats.setIndexingTime(indexService.getIndexingTime().elapsed(TimeUnit.MILLISECONDS));
		stats.setTotalTime(sw.elapsed(TimeUnit.MILLISECONDS));
		stats.setEngineStats(engineStats);
		return stats;
	}


	public Pipeline setRunner(EngineRunner runner) {
		this.runner = runner;
		return this;
	}
}
