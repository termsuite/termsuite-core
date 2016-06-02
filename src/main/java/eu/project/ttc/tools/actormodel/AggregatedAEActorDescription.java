package eu.project.ttc.tools.actormodel;

import java.util.Collections;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import com.google.common.collect.Lists;

public class AggregatedAEActorDescription {
	
	private List<AEActorDescription> aeActorDescriptions = Lists.newArrayList(); 
	
	public void aggregateAE(AnalysisEngineDescription aeDescription, int nbThreads) {
		this.aeActorDescriptions.add(new AEActorDescription(aeDescription, nbThreads));		
	}
	
	
	public List<AEActorDescription> getAeActorDescriptions() {
		return Collections.unmodifiableList(this.aeActorDescriptions);
	}
}
