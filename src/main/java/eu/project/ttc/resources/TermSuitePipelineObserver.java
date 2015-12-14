package eu.project.ttc.resources;

import java.util.List;

import com.google.common.collect.Lists;

import eu.project.ttc.tools.PipelineListener;

public class TermSuitePipelineObserver {
	
	private List<PipelineListener> listeners = Lists.newArrayList();
	

	private int globalProcessWeight = 1;
	private int globalCCWeight = 1;
	private int sum = globalCCWeight + globalProcessWeight;

	public TermSuitePipelineObserver(int globalProcessWeight, int globalCCWeight) {
		super();
		this.globalProcessWeight = globalProcessWeight;
		this.globalCCWeight = globalCCWeight;
		this.sum = globalProcessWeight + globalCCWeight;
	}

	public boolean registerListener(PipelineListener e) {
		return listeners.add(e);
	}

	public boolean removeListener(PipelineListener l) {
		return listeners.remove(l);
	}

	public void status(double processProgress, double ccProgress, String statusline) {
		
		for(PipelineListener l:listeners) {
			double absProgress = (processProgress * globalProcessWeight) + (ccProgress * globalCCWeight);
			l.statusUpdated(absProgress/sum, statusline);
		}
	}
}
