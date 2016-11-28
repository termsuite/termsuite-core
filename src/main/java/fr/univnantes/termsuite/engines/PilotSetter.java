package fr.univnantes.termsuite.engines;

import java.util.List;

import fr.univnantes.termsuite.model.Form;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;

public class PilotSetter {
	public void set(TermIndex index) {
		for(Term t:index.getTerms()) {
			List<Form> forms = index.getOccurrenceStore().getForms(t);
			if(forms.isEmpty())
				t.setPilot(t.getLemma());
			else
				t.setPilot(forms.get(0).getText());
		}
	}
}
