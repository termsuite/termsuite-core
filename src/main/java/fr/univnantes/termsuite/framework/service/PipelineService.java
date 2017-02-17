package fr.univnantes.termsuite.framework.service;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class PipelineService {

	private List<String> engineNames = new ArrayList<>();
	
	public void registerEngineName(String engineName) {
		Preconditions.checkState(
				!engineNames.contains(engineName), 
				"An engine with name %s is already registered in the pipeline.",
				engineName);
		engineNames.add(engineName);
	}
}
