package fr.univnantes.termsuite.framework;

public abstract class AggregateTerminologyEngine extends TerminologyEngine {

	public abstract void configurePipeline(TerminologyPipeline pipeline);
	
	@Override
	public void execute() {
		
	}
}
