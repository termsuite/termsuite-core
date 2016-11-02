package eu.project.ttc.engines;

import java.util.List;

import eu.project.ttc.models.Form;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

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
