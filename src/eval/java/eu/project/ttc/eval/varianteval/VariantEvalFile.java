package eu.project.ttc.eval.varianteval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.project.ttc.models.Term;

public class VariantEvalFile {
	
	private List<String> baseTermKeys;
	
	private Map<String, String> variantKeys = new HashMap<>();
	
	public boolean containsBaseTerm(Term baseTerm) {
		return baseTermKeys.contains(baseTerm);
	}
	
	public boolean containsVariation(Term baseTerm, Term baseVariation) {
		return variantKeys.containsKey(baseTerm.getGroupingKey())
				&& variantKeys
						.get(baseTerm.getGroupingKey())
						.equals(baseVariation.getGroupingKey());
	}
}
