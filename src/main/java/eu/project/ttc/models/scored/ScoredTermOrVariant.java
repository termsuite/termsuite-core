package eu.project.ttc.models.scored;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.utils.StringUtils;
import eu.project.ttc.utils.TermOccurrenceUtils;

public abstract class ScoredTermOrVariant {

	private List<TermOccurrence> occurrences;
	protected int frequency = Integer.MIN_VALUE;
	protected Term term;
	protected ScoredModel scoredModel;

	public ScoredTermOrVariant(ScoredModel scoredModel, Term t) {
		this.scoredModel = scoredModel;
		this.occurrences = Lists.newLinkedList(t.getOccurrences());
		this.term = t;
		reset();
	}

	public int removeOverlappingOccurrences(Iterable<TermOccurrence> otherOccurrences) {
		TermOccurrence currentSo;
		int cnt = 0;
		for(TermOccurrence so:otherOccurrences) {
			Iterator<TermOccurrence> it = occurrences.iterator();
			while(it.hasNext()) {
				currentSo = it.next();
				if(TermOccurrenceUtils.areOverlapping(so, currentSo)) {
					it.remove();
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	public Term getTerm() {
		return term;
	}
	
	public Collection<TermOccurrence> getOccurrences() {
		return Collections.unmodifiableCollection(this.occurrences);
	}
	
	public int getFrequency() {
		return this.occurrences.size();
	}

	
	public double getWR() {
		return getNormalizedFrequency()/getGeneralFrequency();
	}

	private double getNormalizedFrequency() {
		return (1000*(double)getFrequency()) / this.scoredModel.getTermIndex().getWordAnnotationsNum();
	}

	private double getGeneralFrequency() {
		return getTerm().getGeneralFrequencyNorm();
	}

	public double getWRLog() {
		return Math.log(1+getWR());
	}

	public void reset() {
		frequency = Integer.MIN_VALUE;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass()).add("term", this.term).toString();
	}
	
	public double getOrthographicScore() {
		return StringUtils.getOrthographicScore(getTerm().getLemma());
	}
	
}
