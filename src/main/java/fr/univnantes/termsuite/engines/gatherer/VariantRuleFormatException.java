package fr.univnantes.termsuite.engines.gatherer;

public class VariantRuleFormatException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private VariantRule rule;
	private String ruleName;
	
	public VariantRuleFormatException(String message, VariantRule rule) {
		super(message);
		this.rule = rule;
	}
	
	public VariantRuleFormatException(String message, String ruleName) {
		super(message);
		this.ruleName = ruleName;
	}


	public VariantRuleFormatException(VariantRule rule) {
		super("Invalid variant rule format for rule " + rule.getName());
		this.rule = rule;
	}
	
	public VariantRule getRule() {
		return rule;
	}
	
	public String getRuleName() {
		return ruleName == null ? rule.getName() : ruleName;
	}
}
