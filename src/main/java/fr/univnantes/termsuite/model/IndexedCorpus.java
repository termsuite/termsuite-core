package fr.univnantes.termsuite.model;

import fr.univnantes.termsuite.index.Terminology;

public class IndexedCorpus {
	
	private Terminology terminology;
	private OccurrenceStore occurrenceStore;
	
	public IndexedCorpus(Terminology terminology, OccurrenceStore occurrenceStore) {
		super();
		this.terminology = terminology;
		this.occurrenceStore = occurrenceStore;
	}
	
	public Terminology getTerminology() {
		return terminology;
	}
	
	public void setTerminology(Terminology terminology) {
		this.terminology = terminology;
	}
	
	public OccurrenceStore getOccurrenceStore() {
		return occurrenceStore;
	}
	
	public void setOccurrenceStore(OccurrenceStore occurrenceStore) {
		this.occurrenceStore = occurrenceStore;
	}
	
	@Override
	public String toString() {
		return IndexedCorpus.class.getSimpleName() + "["+this.getTerminology().getName()+"]";
	}
}
