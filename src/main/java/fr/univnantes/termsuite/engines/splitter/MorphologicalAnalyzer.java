package fr.univnantes.termsuite.engines.splitter;

import java.util.Optional;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.resources.preproc.ManualSegmentationResource;
import fr.univnantes.termsuite.uima.resources.preproc.PrefixTree;
import fr.univnantes.termsuite.uima.resources.preproc.SimpleWordSet;
import fr.univnantes.termsuite.uima.resources.termino.CompostInflectionRules;
import fr.univnantes.termsuite.uima.resources.termino.SuffixDerivationList;
import fr.univnantes.termsuite.utils.TermHistory;

public class MorphologicalAnalyzer {

	private MorphologicalOptions options;

	private Optional<SuffixDerivationList> suffixDerivationList = Optional.empty();
	private Optional<ManualSegmentationResource> manualCompositions = Optional.empty();
	private Optional<ManualSegmentationResource> prefixExceptions = Optional.empty();
	private Optional<MultimapFlatResource> manualSuffixDerivations = Optional.empty();
	private Optional<CompostInflectionRules> inflectionRules = Optional.empty();
	private Optional<CompostInflectionRules> transformationRules = Optional.empty();
	private Optional<SimpleWordSet> languageDico = Optional.empty();
	private Optional<SimpleWordSet> neoclassicalPrefixes = Optional.empty();
	private Optional<SimpleWordSet> stopList = Optional.empty();
	private Optional<PrefixTree> prefixTree = Optional.empty();
	
	private TermHistory history;
	
	public MorphologicalAnalyzer setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	public MorphologicalAnalyzer setOptions(MorphologicalOptions options) {
		this.options = options;
		return this;
	}
	
	public MorphologicalAnalyzer setSuffixDerivationList(SuffixDerivationList suffixDerivationList) {
		this.suffixDerivationList = Optional.ofNullable(suffixDerivationList);
		return this;
	}

	public MorphologicalAnalyzer setManualCompositions(ManualSegmentationResource manualCompositions) {
		this.manualCompositions = Optional.ofNullable(manualCompositions);
		return this;
	}

	public MorphologicalAnalyzer setPrefixExceptions(ManualSegmentationResource prefixExceptions) {
		this.prefixExceptions = Optional.ofNullable(prefixExceptions);
		return this;
	}

	public MorphologicalAnalyzer setManualSuffixDerivations(MultimapFlatResource manualSuffixDerivations) {
		this.manualSuffixDerivations = Optional.ofNullable(manualSuffixDerivations);
		return this;
	}

	public MorphologicalAnalyzer setInflectionRules(CompostInflectionRules inflectionRules) {
		this.inflectionRules = Optional.ofNullable(inflectionRules);
		return this;
	}

	public MorphologicalAnalyzer setTransformationRules(CompostInflectionRules transformationRules) {
		this.transformationRules = Optional.ofNullable(transformationRules);
		return this;
	}

	public MorphologicalAnalyzer setLanguageDico(SimpleWordSet languageDico) {
		this.languageDico = Optional.ofNullable(languageDico);
		return this;
	}

	public MorphologicalAnalyzer setNeoclassicalPrefixes(SimpleWordSet neoclassicalPrefixes) {
		this.neoclassicalPrefixes = Optional.ofNullable(neoclassicalPrefixes);
		return this;
	}

	public MorphologicalAnalyzer setStopList(SimpleWordSet stopList) {
		this.stopList = Optional.ofNullable(stopList);
		return this;
	}

	public MorphologicalAnalyzer setPrefixTree(PrefixTree prefixTree) {
		this.prefixTree = Optional.ofNullable(prefixTree);
		return this;
	}

	public void analyze(Terminology termIndex) {
		if(options.isPrefixSplitterEnabled()) {
			if(prefixTree.isPresent())
				new PrefixSplitter()
					.setHistory(history)
					.setPrefixTree(prefixTree.get())
					.splitPrefixes(termIndex);
			
			if(prefixExceptions.isPresent())
				new ManualPrefixSetter()
					.setHistory(history)
					.setPrefixExceptions(prefixExceptions.get())
					.setPrefixes(termIndex);
		}
		
		if(manualCompositions.isPresent())
			new ManualSplitter()
				.setManualCompositions(manualCompositions.get())
				.split(termIndex);
		
		if(options.isDerivationDetecterEnabled()) {
			if(suffixDerivationList.isPresent())
				new SuffixDerivationDetecter()
					.setHistory(history)
					.setSuffixDerivationList(suffixDerivationList.get())
					.detectDerivations(termIndex);
			
			if(manualSuffixDerivations.isPresent())
				new ManualSuffixDerivationDetecter()
					.setHistory(history)
					.setManualSuffixDerivations(manualSuffixDerivations.get())
					.detectDerivations(termIndex);
		}
		
		if(options.isNativeSplittingEnabled()) {
			new NativeSplitter()
				.setOptions(options)
				.setInflectionRules(this.inflectionRules.get())
				.setTransformationRules(this.transformationRules.get())
				.setLanguageDico(this.languageDico.get())
				.setNeoclassicalPrefixes(this.neoclassicalPrefixes.get())
				.setStopList(this.stopList.get())
				.split(termIndex);
		}
	}
	
	
	
}
