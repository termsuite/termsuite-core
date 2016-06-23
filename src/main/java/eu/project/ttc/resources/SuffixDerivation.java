package eu.project.ttc.resources;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import eu.project.ttc.models.TermWord;
import eu.project.ttc.utils.StringUtils;

public class SuffixDerivation {
	private static String TYPE_FORMAT = "%s %s";
	private static final String MSG_CANNOT_APPLY_DERIVATION = "Cannot operate the derivation %s on string %s";

	private String derivateFormPattern;
	private String regularFormPattern;
	private String derivateFormSuffix;
	private String regularFormSuffix;
	private String type;

	
	public SuffixDerivation(String derivateFormPattern, String regularFormPattern, String derivateFormSuffix, String regularFormSuffix) {
		super();
		Preconditions.checkNotNull(derivateFormSuffix);
		Preconditions.checkNotNull(regularFormSuffix);
		this.derivateFormSuffix = derivateFormSuffix;
		this.regularFormSuffix = regularFormSuffix;
		this.derivateFormPattern = derivateFormPattern;
		this.regularFormPattern = regularFormPattern;
		this.type = String.format(TYPE_FORMAT, regularFormPattern, derivateFormPattern);
	}
	
	public String getFromPattern() {
		return derivateFormPattern;
	}
	
	public String getToPattern() {
		return regularFormPattern;
	}
	
	public String getDerivateSuffix() {
		return derivateFormSuffix;
	}
	
	public String getRegularSuffix() {
		return regularFormSuffix;
	}
	
	public TermWord getBaseForm(TermWord derivateForm) {
		Preconditions.checkArgument(isKnownDerivate(derivateForm),
				MSG_CANNOT_APPLY_DERIVATION,
				this,
				derivateForm);
		return TermWord.create(
				StringUtils.replaceLast(derivateForm.getWord().getLemma(), derivateFormSuffix, regularFormSuffix),
				this.regularFormPattern);
	}

	public boolean isKnownDerivate(TermWord candidateDerivateTermWord) {
		return candidateDerivateTermWord.getSyntacticLabel().equals(derivateFormPattern)
				&& candidateDerivateTermWord.getWord().getLemma().endsWith(derivateFormSuffix);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("from", derivateFormPattern + ":"+derivateFormSuffix)
				.add("to", regularFormPattern + ":"+regularFormSuffix)
				.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SuffixDerivation) {
			SuffixDerivation sd = (SuffixDerivation) obj;
			return derivateFormSuffix.equals(sd.derivateFormSuffix)
					&& regularFormSuffix.equals(sd.regularFormSuffix)
					&& derivateFormPattern.equals(sd.derivateFormPattern)
					&& regularFormPattern.equals(sd.regularFormPattern);
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.derivateFormSuffix, this.regularFormSuffix, this.derivateFormPattern, this.regularFormPattern);
	}

	public Object getType() {
		return type;
	}
}
