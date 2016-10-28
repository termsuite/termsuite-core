package eu.project.ttc.engines;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;

public class DocumentFrequencySetter {
	public void set(TermIndex index) {
		for(Term t:index.getTerms()) {
			long dFreq = index.getOccurrenceStore().getOccurrences(t).stream()
					.map(TermOccurrence::getSourceDocument)
					.distinct()
					.count();
			t.setDocumentFrequency((int)dFreq);
		}
	}
}
