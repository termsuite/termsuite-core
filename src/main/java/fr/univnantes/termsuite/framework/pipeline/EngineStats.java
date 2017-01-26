package fr.univnantes.termsuite.framework.pipeline;

import java.util.List;

public class EngineStats {
	
	private String engineName;
	private long duration;
	private boolean simpleEngine = true;
	private List<EngineStats> childStats;

	public EngineStats(String engineName, long duration, List<EngineStats> childStats) {
		super();
		this.engineName = engineName;
		this.duration = duration;
		this.simpleEngine = false;
		this.childStats = childStats;
	}

	public EngineStats(String engineName, long duration) {
		super();
		this.engineName = engineName;
		this.duration = duration;
	}

	public String getEngineName() {
		return engineName;
	}

	public long getDuration() {
		return duration;
	}

	public boolean isSimpleEngine() {
		return simpleEngine;
	}

	public List<EngineStats> getChildStats() {
		return childStats;
	}
	
	
	@Override
	public String toString() {
		return toString(0);
	}

	private String toString(int nbTabs) {
		String s = String.format("%s[%dms] %s", 
				tabs(nbTabs),
				duration,
				engineName);
		if(simpleEngine)
			return s;
		else {
			for(EngineStats stats:childStats)
				s+="\n" + stats.toString(nbTabs+1);
			return s;
		}
	}

	private String tabs(int nbTabs) {
		StringBuilder builder= new StringBuilder();
		for(int i = 0; i< nbTabs ; i++)
			builder.append("\t");
		return builder.toString();
	}
}
