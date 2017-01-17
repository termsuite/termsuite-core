package fr.univnantes.termsuite.engines.prepare;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

/**
 * 
 * Sets {@link TermProperty#PILOT} and {@link TermProperty#DOCUMENT_FREQUENCY} properties.
 * 
 * @author Damien Cram
 *
 */
public class CorpusWidePropertiesSetter extends TerminologyEngine {
	
	@Override
	public void execute() {
		for(Term t:terminology.getTerms()) {
			String pilot = terminology.getOccurrenceStore().getMostFrequentForm(t);
			t.setPilot(pilot == null ? t.getGroupingKey() : pilot);
			t.setDocumentFrequency(terminology.getOccurrenceStore().getDocuments(t).size());
		}
	}
}
