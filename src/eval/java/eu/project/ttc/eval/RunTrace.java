package eu.project.ttc.eval;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.util.Lists;

public class RunTrace {

	private List<AlignmentTry> tries = Lists.newArrayList();
	private String runName;
	
	public RunTrace(String runName) {
		this.runName = runName;
	}

	public boolean newTry(AlignmentTry e) {
		return tries.add(e);
	}

	public String getRunName() {
		return runName;
	}

	public double getPrecision() {
		if(tries.isEmpty())
			return 0;
		else {
			return (double)successResults().count()/validResults().count();
		}
	}
	
	public Stream<AlignmentTry> validResults() {
		return tries.stream().filter(e -> e.isValid());
	}

	public Stream<AlignmentTry> invalidResults() {
		return tries.stream().filter(e -> !e.isValid());
	}

	public Stream<AlignmentTry> successResults() {
		return validResults().filter(e->e.isSuccess());		
	}

	public Stream<AlignmentTry> failedResults() {
		return validResults().filter(e->!e.isSuccess());		
	}

	
	public Stream<AlignmentTry> tries() {
		return tries.stream();
	}
}
