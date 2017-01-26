package fr.univnantes.termsuite.framework;

public class PipelineStats {
	
	private long indexingTime = -1l;
	private long totalTime = -1l;
	
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
}
