package fr.univnantes.termsuite.model;

import java.nio.charset.Charset;

public class CorpusMetadata {
	
	private int nbDocuments;
	private long totalSize;
	private Charset encoding;
	
	public CorpusMetadata(Charset encoding) {
		this(encoding, -1, -1);
	}
	
	public CorpusMetadata(Charset encoding, int nbDocuments, long totalSize) {
		super();
		this.nbDocuments = nbDocuments;
		this.totalSize = totalSize;
		this.encoding = encoding;
	}
	
	public Charset getEncoding() {
		return encoding;
	}
	
	public int getNbDocuments() {
		return nbDocuments;
	}
	public long getTotalSize() {
		return totalSize;
	}
}
