package eu.project.ttc.align;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import eu.project.ttc.metrics.Explanation;
import eu.project.ttc.metrics.IExplanation;
import eu.project.ttc.models.Term;

public class TranslationCandidate implements Comparable<TranslationCandidate> {
	private List<TranslationCandidate> subCandidates;

	private Term term;
	private int rank = -1;
	private double score;

	private AlignmentMethod method;
	private Object sourceTerm;
	private IExplanation explanation = Explanation.emptyExplanation();

	void setScore(double score) {
		this.score = score;
	}

	void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	TranslationCandidate(AlignmentMethod method, Term targetTerm, double score, Object sourceTerm,
			TranslationCandidate... subCandidates) {
		super();
		this.method = method;
		this.term = targetTerm;
		this.score = score;
		this.sourceTerm = sourceTerm;
		this.subCandidates = Lists.newArrayList(subCandidates);
	}

	TranslationCandidate(AlignmentMethod method, Term targetTerm, double score, Term sourceTerm,
			IExplanation explanation) {
		this(method, targetTerm, score, sourceTerm);
		this.explanation = explanation;
	}

	void setExplanation(IExplanation explanation) {
		this.explanation = explanation;
	}

	@Override
	public int compareTo(TranslationCandidate o) {
		return ComparisonChain.start().compare(o.score, score).compare(term, o.term).result();
	}

	public double getScore() {
		return score;
	}

	public Term getTerm() {
		return term;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TranslationCandidate)
			return Objects.equal(((TranslationCandidate) obj).score, this.score)
					&& Objects.equal(((TranslationCandidate) obj).term, this.term);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(term, score);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(this.term.getGroupingKey()).addValue(this.method)
				.add("s", String.format("%.2f", this.score)).toString();
	}

	/**
	 * 
	 * Not necessarily an instance of {@link Term}. Can also be a list of terms.
	 * 
	 * @return
	 */
	public Object getSourceTerm() {
		return sourceTerm;
	}

	public IExplanation getExplanation() {
		return explanation;
	}

	public List<TranslationCandidate> getSubCandidates() {
		return subCandidates;
	}

	public AlignmentMethod getMethod() {
		return method;
	}
}