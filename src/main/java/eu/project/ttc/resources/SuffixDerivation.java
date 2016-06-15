package eu.project.ttc.resources;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import eu.project.ttc.utils.StringUtils;

public class SuffixDerivation {
	private static final String MSG_CANNOT_APPLY_DERIVATION = "Cannot operate the derivation %s on string %s";

	private String fromSuffix;
	private String toSuffix;
	
	public SuffixDerivation(String fromSuffix, String toSuffix) {
		super();
		Preconditions.checkNotNull(fromSuffix);
		Preconditions.checkNotNull(toSuffix);
		this.fromSuffix = fromSuffix;
		this.toSuffix = toSuffix;
	}
	
	public String getFromSuffix() {
		return fromSuffix;
	}
	
	public String getToSuffix() {
		return toSuffix;
	}
	
	public String derivate(String fromString) {
		Preconditions.checkArgument(canDerivate(fromString),
				MSG_CANNOT_APPLY_DERIVATION,
				this,
				fromString);
		return StringUtils.replaceLast(fromString, fromSuffix, toSuffix);
	}

	public boolean canDerivate(String fromString) {
		return fromString.endsWith(fromSuffix);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("fromSuffix", fromSuffix)
				.add("toSuffix", toSuffix)
				.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SuffixDerivation) {
			SuffixDerivation sd = (SuffixDerivation) obj;
			return fromSuffix.equals(sd.fromSuffix)
					&& toSuffix.equals(sd.toSuffix);
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.fromSuffix, this.toSuffix);
	}

	public Object getType() {
		return "";
	}
}
