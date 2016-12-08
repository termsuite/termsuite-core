package fr.univnantes.termsuite.engines.gatherer;

import java.math.BigInteger;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.metrics.FastDiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.uima.resources.ObserverResource.SubTaskObserver;
import fr.univnantes.termsuite.utils.TermHistory;

public class TermGatherer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermGatherer.class);

	private static final int OBSERVING_STEP = 1000;

	private TermHistory history;
	private YamlRuleSet rules;
	private GathererOptions gathererOptions = new GathererOptions();
	
	private BigInteger totalComparisons = BigInteger.valueOf(0);
	private int nbComparisons = 0;
	private Optional<SubTaskObserver> taskObserver = Optional.empty();

	private Optional<MultimapFlatResource> dico = Optional.empty();

	public TermGatherer setGathererOptions(GathererOptions gathererOptions) {
		this.gathererOptions = gathererOptions;
		return this;
	}
	
	public TermGatherer setRules(YamlRuleSet rules) {
		this.rules = rules;
		return this;
	}
	
	public TermGatherer setDictionary(MultimapFlatResource dico) {
		this.dico = Optional.of(dico);
		return this;
	}
	
	public TermGatherer setTaskObserver(SubTaskObserver taskObserver) {
		this.taskObserver = Optional.of(taskObserver);
		return this;
	}
	
	public TermGatherer setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	
	public void gather(Terminology termino) {
		Stopwatch sw = Stopwatch.createStarted();
		
		GroovyService groovyService = new GroovyService(termino);

		if(gathererOptions.isPrefixationGathererEnabled()) {
			LOGGER.info("Gathering prefixation variants");
			new AbstractGatherer()
				.setRuleType(RuleType.PREFIXATION)
				.setIndexName(TermIndexes.PREFIXATION_LEMMAS, true)
				.setRelationType(RelationType.SYNTACTICAL)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(RuleType.PREFIXATION))	
				.gather(termino);
		}
		
		if(gathererOptions.isDerivationGathererEnabled()) {
			LOGGER.info("Gathering derivation variants");
			new AbstractGatherer()
				.setIndexName(TermIndexes.DERIVATION_LEMMAS, true)
				.setRuleType(RuleType.DERIVATION)
				.setRelationType(RelationType.SYNTACTICAL)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(RuleType.DERIVATION))	
				.gather(termino);
		}
		
		if(gathererOptions.isMorphologicalGathererEnabled()) {
			LOGGER.info("Gathering morphological variants");
			new AbstractGatherer()
				.setIndexName(TermIndexes.ALLCOMP_PAIRS, false)
				.setRuleType(RuleType.MORPHOLOGICAL)
				.setRelationType(RelationType.MORPHOLOGICAL)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(RuleType.MORPHOLOGICAL))	
				.gather(termino);
		} 
		
		LOGGER.info("Gathering syntagmatic variants");
		new AbstractGatherer()
			.setIndexName(TermIndexes.ALLCOMP_PAIRS, true)
			.setRuleType(RuleType.SYNTAGMATIC)
			.setRelationType(RelationType.SYNTACTICAL)
			.setGroovyAdapter(groovyService)
			.setHistory(history)
			.setVariantRules(rules.getVariantRules(RuleType.SYNTAGMATIC))	
			.gather(termino);

		if(gathererOptions.isSemanticGathererEnabled()) {
			LOGGER.info("Gathering morphological semantic variants");
			new SemanticGatherer()
			.setDictionary(dico)
			.setRuleType(RuleType.SEMANTIC)
			.setRelationType(RelationType.SYNONYMIC)
			.setGroovyAdapter(groovyService)
			.setHistory(history)
			.setVariantRules(rules.getVariantRules(RuleType.SEMANTIC))	
			.gather(termino);
		}
		
		
		if(gathererOptions.isGraphicalGathererEnabled()) {
			LOGGER.info("Gathering graphical variants");
			new GraphicalGatherer()
				.setDistance(new FastDiacriticInsensitiveLevenshtein(false))
				.setSimilarityThreshold(gathererOptions.getGraphicalSimilarityThreshold())
				.setNbFixedLetters(termino.getLang().getGraphicalVariantNbPreindexingLetters())
				.setHistory(history)
				.gather(termino);
		}

		sw.stop();
		LOGGER.debug("{} finished in {}", this.getClass().getSimpleName(), sw);
	}

}
