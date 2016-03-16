package eu.project.ttc.models.occstore;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eu.project.ttc.models.OccurrenceStore;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermSelector;

public class MemoryOccurrenceStore implements OccurrenceStore {

	private Multimap<Term, TermOccurrence> map = HashMultimap.create();
	
	@Override
	public Iterator<TermOccurrence> occurrenceIterator(Term term) {
		return getOccurrences(term).iterator();
	}

	@Override
	public Collection<TermOccurrence> getOccurrences(Term term) {
		return map.get(term);
	}

	@Override
	public void addOccurrence(Term term, TermOccurrence e) {
		map.put(term, e);
		
	}

	@Override
	public void addAllOccurrences(Term term, Collection<TermOccurrence> c) {
		map.putAll(term, c);
	}

	@Override
	public Type getStoreType() {
		return Type.MEMORY;
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public void flush() {
		// nothing to do
	}

	@Override
	public State getCurrentState() {
		return State.INDEXED;
	}

	@Override
	public void makeIndex() {
		// nothing to do
	}

	@Override
	public void removeTerm(Term t) {
		map.removeAll(t.getId());
	}

	@Override
	public void deleteMany(TermSelector selector) {
		Term t;
		for(Iterator<Term> it = map.keySet().iterator(); it.hasNext();) {
			t = it.next();
			if(selector.select(t))
				it.remove();
		}
			
		
	}
}
