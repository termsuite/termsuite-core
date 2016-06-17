package eu.project.ttc.resources;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import eu.project.ttc.models.TermWord;
import eu.project.ttc.utils.StringUtils;

public class SuffixDerivation {
	private static String TYPE_FORMAT = "%s %s";
	private static final String MSG_CANNOT_APPLY_DERIVATION = "Cannot operate the derivation %s on string %s";

	private String fromPattern;
	private String toPattern;
	private String fromSuffix;
	private String toSuffix;
	private String type;

	
	public SuffixDerivation(String fromPattern, String toPattern, String fromSuffix, String toSuffix) {
		super();
		Preconditions.checkNotNull(fromSuffix);
		Preconditions.checkNotNull(toSuffix);
		this.fromSuffix = fromSuffix;
		this.toSuffix = toSuffix;
		this.fromPattern = fromPattern;
		this.toPattern = toPattern;
		this.type = String.format(TYPE_FORMAT, fromPattern, toPattern);
	}
	
	public String getFromPattern() {
		return fromPattern;
	}
	
	public String getToPattern() {
		return toPattern;
	}
	
	public String getFromSuffix() {
		return fromSuffix;
	}
	
	public String getToSuffix() {
		return toSuffix;
	}
	
	public TermWord derivate(TermWord from) {
		Preconditions.checkArgument(canDerivate(from),
				MSG_CANNOT_APPLY_DERIVATION,
				this,
				from);
		return TermWord.create(
				StringUtils.replaceLast(from.getWord().getLemma(), fromSuffix, toSuffix),
				this.toPattern);
	}

	public boolean canDerivate(TermWord from) {
		return from.getSyntacticLabel().equals(fromPattern)
				&& from.getWord().getLemma().endsWith(fromSuffix);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("from", fromPattern + ":"+fromSuffix)
				.add("to", toPattern + ":"+toSuffix)
				.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SuffixDerivation) {
			SuffixDerivation sd = (SuffixDerivation) obj;
			return fromSuffix.equals(sd.fromSuffix)
					&& toSuffix.equals(sd.toSuffix)
					&& fromPattern.equals(sd.fromPattern)
					&& toPattern.equals(sd.toPattern);
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.fromSuffix, this.toSuffix, this.fromPattern, this.toPattern);
	}

	public Object getType() {
		return type;
	}
}
