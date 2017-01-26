package fr.univnantes.termsuite.framework;

import fr.univnantes.termsuite.framework.pipeline.EngineStats;

public class PipelineStats {
	
	private long indexingTime = -1l;
	private long totalTime = -1l;
	private EngineStats engineStats;

	public long getIndexingTime() {
		return indexingTime;
	}

	public void setIndexingTime(long indexingTime) {
		this.indexingTime = indexingTime;
	}
	
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	
	public long getTotalTime() {
		return totalTime;
	}

	public EngineStats getEngineStats() {
		return engineStats;
	}

	public void setEngineStats(EngineStats engineStats) {
		this.engineStats = engineStats;
	}
}
