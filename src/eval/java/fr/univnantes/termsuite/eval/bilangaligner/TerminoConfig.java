package fr.univnantes.termsuite.eval.bilangaligner;

import java.nio.file.Paths;

import com.google.common.base.Charsets;

import fr.univnantes.termsuite.api.TerminoExtractor;
import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.eval.TermSuiteEvals;
import fr.univnantes.termsuite.eval.model.Corpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TermProperty;

public class TerminoConfig {
	
	private int scope = 3;
	private boolean swtOnly = true;
	private int frequencyTh = 2;
	private int coocFrequencyTh = 1;

	public TerminoConfig() {
		super();
	}

	public int getScope() {
		return scope;
	}

	public TerminoConfig setScope(int scope) {
		this.scope = scope;
		return this;
	}

	public boolean isSwtOnly() {
		return swtOnly;
	}

	public TerminoConfig setSwtOnly(boolean swtOnly) {
		this.swtOnly = swtOnly;
		return this;
	}

	public int getFrequencyTh() {
		return frequencyTh;
	}

	public TerminoConfig setFrequencyTh(int frequencyTh) {
		this.frequencyTh = frequencyTh;
		return this;
	}

	public int getCoocFrequencyTh() {
		return coocFrequencyTh;
	}

	public TerminoConfig setCoocFrequencyTh(int coocFrequencyTh) {
		throw new UnsupportedOperationException("Not yet implemented");
	}	
	
	public TerminoExtractor toExtractor(Lang lang, Corpus corpus) {
		String corpusDir = Paths.get(corpus.getRootDir().toString(), lang.getName()).toString();
		TerminoExtractor extractor = TerminoExtractor
				.fromTxtCorpus(lang, corpusDir, "**/*.txt", Charsets.UTF_8.name())
				.setTreeTaggerHome(TermSuiteEvals.getTreeTaggerPath().toString())
				.disableScoring()
				.disableVariationDetection()
				.useContextualizer(new ContextualizerOptions().setScope(scope));
		
		if(frequencyTh > 1)
			extractor.preFilter(new TerminoFilterOptions().by(TermProperty.FREQUENCY).keepOverTh(frequencyTh));
		
		return extractor;
	}
	
	@Override
	public String toString() {
		return String.format("scope%d-th%d", scope, frequencyTh);
	}
}
