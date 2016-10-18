package eu.project.ttc.eval;

import java.util.Optional;

import eu.project.ttc.engines.BilingualAligner.AlignmentMethod;
import eu.project.ttc.models.Term;

public class AlignmentTry {
	
	private boolean valid;	
	
	private Optional<Boolean> success = Optional.empty();	
	private Optional<Integer> targetTermCandidatePosition = Optional.empty();	
	private Optional<String> comment = Optional.empty();	
	private Optional<AlignmentMethod> method = Optional.empty();	

	
	private Optional<Term> sourceTerm = Optional.empty();	
	private Optional<Term> targetTerm = Optional.empty();	


	/*
	 * In case terms are not found in sources/target terminos
	 */
	private Optional<String> sourceLemma = Optional.empty();	
	private Optional<String> targetLemma = Optional.empty();	

	public AlignmentTry() {
		super();
	}
	
	public AlignmentTry(Term sourceTerm, Term targetTerm) {
		super();
		
		this.sourceTerm = Optional.of(sourceTerm);
		this.sourceLemma = Optional.of(sourceTerm.getLemma());
		this.targetTerm = Optional.of(targetTerm);
		this.targetLemma = Optional.of(targetTerm.getLemma());
	}

	
	public boolean isValid() {
		return valid;
	}

	public AlignmentTry setValid(boolean validTest) {
		this.valid = validTest;
		return this;
	}

	public AlignmentTry setMethod(AlignmentMethod method) {
		this.method = Optional.of(method);
		return this;
	}
	
	public AlignmentMethod getMethod() {
		return method.get();
	}
	
	public boolean isSuccess() {
		return success.get();
	}

	public AlignmentTry setSuccess(boolean targetTermFound) {
		this.success = Optional.of(targetTermFound);
		return this;
	}

	public int getTargetTermCandidatePosition() {
		return targetTermCandidatePosition.get();
	}

	public AlignmentTry setTargetTermCandidatePosition(int targetTermCandidatePosition) {
		this.targetTermCandidatePosition = Optional.of(targetTermCandidatePosition);
		return this;
	}

	public String getComment() {
		return comment.get();
	}

	public AlignmentTry setComment(String comment) {
		this.comment = Optional.of(comment);
		return this;
	}

	public TermType getSourceTermType() {
		return TermType.ofTerm(sourceTerm.get());
	}

	public TermType getTargetTermType() {
		return TermType.ofTerm(targetTerm.get());
	}

	public String toOneLineString() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%-25s %-25s %s", 
				toString(isValid()),
				toString(this.success),
				toString(this.sourceTerm.isPresent() ? this.getSourceTermType() : "-"),
				toString(this.targetTerm.isPresent() ? this.getTargetTermType() : "-"),
				toString(this.method),
				sourceLemma.orElse("-"),
				targetLemma.orElse("-"),
				this.comment.orElse("-")
			);
	}

	private String toString(Optional<?> value) {
		if(value.isPresent())
			return toString(value.get());
		else
			return "-";
	}

	private String toString(Boolean value) {
		return value ? "1" : "0";
	}

	private String toString(AlignmentMethod method) {
		return method.getShortName();
	}

	private String toString(TermType type) {
		return type.getTermTypeName();
	}
	
	private String toString(Object object) {
		if(object instanceof Boolean)
			return toString((Boolean)object);
		else if(object instanceof AlignmentMethod)
			return toString((AlignmentMethod)object);
		if(object instanceof TermType)
			return toString((TermType)object);
		else
			return object.toString();		
	}

	public static String toOneLineHeaders() {
		return "V\tR\tSt\tTt\tM\tComment";
	}
	
	public AlignmentTry setTargetLemma(String targetLemma) {
		this.targetLemma = Optional.of(targetLemma);
		return this;
	}
	
	public AlignmentTry setSourceLemma(String sourceLemma) {
		this.sourceLemma = Optional.of(sourceLemma);
		return this;
	}

}
