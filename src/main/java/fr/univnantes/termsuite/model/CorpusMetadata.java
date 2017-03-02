package fr.univnantes.termsuite.model;

public class CorpusMetadata {
	
	private int nbDocuments;
	private long totalSize;
	
	public CorpusMetadata() {
		this.nbDocuments = -1;
		this.totalSize = -1;
	}
		
	public CorpusMetadata(int nbDocuments, long totalSize) {
		super();
		this.nbDocuments = nbDocuments;
		this.totalSize = totalSize;
	}
	
	public int getNbDocuments() {
		return nbDocuments;
	}
	public long getTotalSize() {
		return totalSize;
	}
}
