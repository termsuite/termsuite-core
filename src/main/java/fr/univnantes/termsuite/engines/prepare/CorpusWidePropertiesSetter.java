package fr.univnantes.termsuite.engines.prepare;

import javax.inject.Inject;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

/**
 * 
 * Sets {@link TermProperty#PILOT} and {@link TermProperty#DOCUMENT_FREQUENCY} properties.
 * 
 * @author Damien Cram
 *
 */
public class CorpusWidePropertiesSetter extends SimpleEngine {
	
	@Inject OccurrenceStore occurrenceStore;
	
	@Override
	public void execute() {
		for(Term t:terminology.getTerms()) {
			String pilot = occurrenceStore.getMostFrequentForm(t);
			t.setPilot(pilot == null ? t.getGroupingKey() : pilot);
			t.setDocumentFrequency(occurrenceStore.getDocuments(t).size());
		}
	}
}
