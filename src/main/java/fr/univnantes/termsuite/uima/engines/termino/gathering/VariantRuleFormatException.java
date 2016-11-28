package fr.univnantes.termsuite.uima.engines.termino.gathering;

public class VariantRuleFormatException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private VariantRule rule;

	public VariantRuleFormatException(String message, VariantRule rule) {
		super(message);
		this.rule = rule;
	}

	public VariantRuleFormatException(VariantRule rule) {
		super("Invalid variant rule format for rule " + rule.getName());
		this.rule = rule;
	}
	
	public VariantRule getRule() {
		return rule;
	}
}
