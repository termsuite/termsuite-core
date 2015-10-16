package eu.project.ttc.models.index;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;

public abstract class TermMeasure {
	
	private TermIndex termIndex;
	private boolean dirty = true;
	
	public TermMeasure(TermIndex termIndex) {
		super();
		this.termIndex = termIndex;
		invalidate();
	}

	public void invalidate() {
		this.dirty = true;
		standardDev = Double.NaN;
	}

	private int num = 0;
	private double sum = 0d;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	private double avg = Double.NaN;
	private double standardDev = Double.NaN;
	
	public abstract double getValue(Term term);

	private void compute() {
		this.sum = 0d;
		this.num = 0;
		for(Term t:termIndex.getTerms()) {
			if(getValue(t) < this.min)
				this.min = getValue(t);
			if(getValue(t) > this.max)
				this.max = getValue(t);
			this.sum += getValue(t);
		}
		this.avg = this.sum / this.num;
		this.dirty = false;
	}
	
	public double getStandardDeviation() {
		if(this.standardDev == Double.NaN) {
			double sigmaSquare = 0;
			for(Term term:termIndex.getTerms()) 
				sigmaSquare+=Math.pow(getValue(term) - getAvg(), 2);
			standardDev = Math.sqrt(1.0/getNum() * sigmaSquare);
		} 
		return this.standardDev;
	}
	
	public double getZScore(Term t) {
		return (getValue(t) - getAvg())/getStandardDeviation();
	}
	
	public double getMin() {
		if(dirty)
			compute();
		return min;
	}

	public double getMax() {
		if(dirty)
			compute();
		return max;
	}

	public double getAvg() {
		if(dirty)
			compute();
		return avg;
	}

	public double getSum() {
		if(dirty)
			compute();
		return sum;
	}
	
	public int getNum() {
		if(dirty)
			compute();
		return num;
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
