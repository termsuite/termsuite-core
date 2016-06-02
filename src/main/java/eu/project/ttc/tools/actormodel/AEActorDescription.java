package eu.project.ttc.tools.actormodel;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

public class AEActorDescription {
	
	private AnalysisEngineDescription aeDescription;

	private int nbThreads;

	public AEActorDescription(AnalysisEngineDescription aeDescription, int nbThreads) {
		super();
		this.aeDescription = aeDescription;
		this.nbThreads = nbThreads;
	}

	public int getNbThreads() {
		return nbThreads;
	}

	public void setNbThreads(int nbThreads) {
		this.nbThreads = nbThreads;
	}
	
	
	public AnalysisEngineDescription getAeDescription() {
		return aeDescription;
	}
	
	public void setAeDescription(AnalysisEngineDescription aeDescription) {
		this.aeDescription = aeDescription;
	}
}
