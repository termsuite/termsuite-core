package eu.project.ttc.eval;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.util.Lists;

import com.google.common.base.MoreObjects;

public class RunTrace {

	private List<AlignmentRecord> records = Lists.newArrayList();
	
	public boolean newTry(AlignmentRecord e) {
		return records.add(e);
	}

	public double getPrecision() {
		if(records.isEmpty())
			return 0;
		else {
			return (double)successResults().count()/validResults().count();
		}
	}
	
	public Stream<AlignmentRecord> validResults() {
		return records.stream().filter(e -> e.isValid());
	}

	public Stream<AlignmentRecord> invalidResults() {
		return records.stream().filter(e -> !e.isValid());
	}

	public Stream<AlignmentRecord> successResults() {
		return validResults().filter(e->e.isSuccess());		
	}

	public Stream<AlignmentRecord> failedResults() {
		return validResults().filter(e->!e.isSuccess());		
	}

	
	public Stream<AlignmentRecord> tries() {
		return records.stream();
	}
}
