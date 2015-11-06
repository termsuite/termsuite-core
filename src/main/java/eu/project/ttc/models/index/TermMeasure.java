package eu.project.ttc.models.index;

import java.util.Comparator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;

public abstract class TermMeasure {
	
	private static final String MSG_NOT_COMPUTED = "This term measure must be computed first.";
	
	private TermIndex termIndex;
	private boolean computed = false;
	
	public TermMeasure(TermIndex termIndex) {
		super();
		this.termIndex = termIndex;
	}

	private int totalSpottedTerms = 0;
	private double sum = 0d;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	private double avg = Double.NaN;
	private double standardDev = Double.NaN;
	
	public abstract double getValue(Term term);

	private void checkComputed() {
		Preconditions.checkState(computed, MSG_NOT_COMPUTED);
	}
	public void compute() {
		Preconditions.checkState(termIndex.getTerms().size() > 0, "Cannot compute a measure ono an empty TermIndex");
		this.sum = 0d;
		int num = 0;
		for(Term t:termIndex.getTerms()) {
			num++;
			if(getValue(t) < this.min)
				this.min = getValue(t);
			if(getValue(t) > this.max)
				this.max = getValue(t);
			this.sum += getValue(t);
		}
		
		/*
		 * WARNING!
		 * 
		 * If totalSpottedTerms is not set, we set it as num, but
		 * aggregated measure will be impacted and no normalization
		 * will actually work. Normalization should always be operated 
		 * over the same spotting rules, without prior filtering.
		 */
		if(termIndex.getSpottedTermsNum() > 0)
			totalSpottedTerms = termIndex.getSpottedTermsNum();
		else 
			totalSpottedTerms = num;
		
		this.avg = this.sum / this.totalSpottedTerms;
		
		// compute standard deviation
		double sigmaSquare = 0;
		for(Term term:termIndex.getTerms()) 
			sigmaSquare+=Math.pow(getValue(term) - this.avg, 2);
		this.standardDev = Math.sqrt(1.0/this.totalSpottedTerms * sigmaSquare);
		this.computed = true;
	}
	
	public double getStandardDeviation() {
		checkComputed();
		return this.standardDev;
	}
	
	public double getZScore(Term t) {
		checkComputed();
		return (getValue(t) - getAvg())/getStandardDeviation();
	}
	
	public double getMin() {
		checkComputed();
		return min;
	}

	public double getMax() {
		checkComputed();
		return max;
	}

	public double getAvg() {
		checkComputed();
		return avg;
	}

	public double getSum() {
		checkComputed();
		return sum;
	}
	
	public int getTotalSpottedTerms() {
		return totalSpottedTerms;
	}
	
	public Comparator<Term> getTermComparator(final boolean reverse) {
		return new Comparator<Term>() {
			@Override
			public int compare(Term o1, Term o2) {
				return ComparisonChain.start()
						.compare(reverse ? getValue(o2) : getValue(o1), reverse ? getValue(o1) : getValue(o2))
						.compare(o1.getGroupingKey(), o2.getGroupingKey())
						.result();
			}
		};
	}

	public Comparator<? super TermOccurrence> getOccurrenceComparator(final boolean reverse) {
		return new Comparator<TermOccurrence>() {
			@Override
			public int compare(TermOccurrence o1, TermOccurrence o2) {
				return ComparisonChain.start()
					.compare(reverse ? getValue(o2.getTerm()) : getValue(o1.getTerm()), reverse ? getValue(o1.getTerm()) : getValue(o2.getTerm()))
					.compare(o1.getSourceDocument().getUrl(), o2.getSourceDocument().getUrl())
					.compare(o1.getBegin(), o2.getBegin())
					.compare(o2.getEnd(), o1.getEnd())
					.result();
			}
		};
	}
}
