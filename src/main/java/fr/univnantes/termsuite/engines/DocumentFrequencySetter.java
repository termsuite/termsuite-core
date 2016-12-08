package fr.univnantes.termsuite.engines;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.TermOccurrence;

public class DocumentFrequencySetter {
	public void set(Terminology index) {
		for(Term t:index.getTerms()) {
			long dFreq = index.getOccurrenceStore().getOccurrences(t).stream()
					.map(TermOccurrence::getSourceDocument)
					.distinct()
					.count();
			t.setDocumentFrequency((int)dFreq);
		}
	}
}
