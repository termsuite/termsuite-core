package fr.univnantes.termsuite.uima;

import fr.univnantes.termsuite.model.Tagger;

public class PreparationPipelineOptions {
	
	private Tagger tagger = Tagger.TREE_TAGGER;

	private boolean documentLoggingEnabled = true;
	private boolean fixedExpressionEnabled = true;

	public Tagger getTagger() {
		return tagger;
	}

	public PreparationPipelineOptions setTagger(Tagger tagger) {
		this.tagger = tagger;
		return this;
	}

	public boolean isDocumentLoggingEnabled() {
		return documentLoggingEnabled;
	}

	public PreparationPipelineOptions setDocumentLoggingEnabled(boolean documentLoggingEnabled) {
		this.documentLoggingEnabled = documentLoggingEnabled;
		return this;
	}

	public boolean isFixedExpressionEnabled() {
		return fixedExpressionEnabled;
	}
	
	public PreparationPipelineOptions setFixedExpressionEnabled(boolean fixedExpressionEnabled) {
		this.fixedExpressionEnabled = fixedExpressionEnabled;
		return this;
	}

}
