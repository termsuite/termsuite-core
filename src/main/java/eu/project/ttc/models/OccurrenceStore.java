package eu.project.ttc.models;

import java.util.Collection;
import java.util.Iterator;

public interface OccurrenceStore {
	public static enum Type {MEMORY, FILE, MONGODB}
	public static enum State{COLLECTING,INDEXING,INDEXED}

	public Iterator<TermOccurrence> occurrenceIterator(Term term);
	public Collection<TermOccurrence> getOccurrences(Term term);
	public void addOccurrence(Term term, TermOccurrence e);
	public void addAllOccurrences(Term term, Collection<TermOccurrence> c);
	public Type getStoreType();
	public void flush();
	public State getCurrentState();
	public void makeIndex();


	
	/**
	 * Returns the path to access the occurrence store if
	 * this occurrence store is of type {@link Type#FILE}, 
	 * <code>null</code> otherwise.
	 * 
	 * @return
	 * 		the URL, <code>null</code> if this store is of type {@link Type#MEMORY}
	 */
	public String getUrl();
	
	/**
	 * Removes all occurrences of the term
	 * @param t
	 */
	public void removeTerm(Term t);
	
	public void deleteMany(TermSelector selector);
}
