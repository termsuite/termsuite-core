package fr.univnantes.termsuite.engines.gatherer;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.metrics.FastDiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.uima.resources.ObserverResource.SubTaskObserver;
import fr.univnantes.termsuite.utils.TermHistory;

public class TermGatherer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermGatherer.class);

	private TermHistory history;
	private YamlRuleSet rules;
	private GathererOptions gathererOptions = new GathererOptions();
	
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
				.setVariationType(VariationType.PREFIXATION)
				.setIndexName(TermIndexes.PREFIXATION_LEMMAS, true)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(VariationType.PREFIXATION))	
				.gather(termino);
		}
		
		if(gathererOptions.isDerivationGathererEnabled()) {
			LOGGER.info("Gathering derivation variants");
			new AbstractGatherer()
				.setIndexName(TermIndexes.DERIVATION_LEMMAS, true)
				.setVariationType(VariationType.DERIVATION)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(VariationType.DERIVATION))	
				.gather(termino);
		}
		
		if(gathererOptions.isMorphologicalGathererEnabled()) {
			LOGGER.info("Gathering morphological variants");
			new AbstractGatherer()
				.setIndexName(TermIndexes.ALLCOMP_PAIRS, false)
				.setVariationType(VariationType.MORPHOLOGICAL)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(VariationType.MORPHOLOGICAL))	
				.gather(termino);
		} 
		
		LOGGER.info("Gathering syntagmatic variants");
		new AbstractGatherer()
			.setIndexName(TermIndexes.ALLCOMP_PAIRS, true)
			.setVariationType(VariationType.SYNTAGMATIC)
			.setGroovyAdapter(groovyService)
			.setHistory(history)
			.setVariantRules(rules.getVariantRules(VariationType.SYNTAGMATIC))	
			.gather(termino);

		if(gathererOptions.isSemanticGathererEnabled()) {
			LOGGER.info("Gathering morphological semantic variants");
			new SemanticGatherer()
				.setDictionary(dico)
				.setVariationType(VariationType.SEMANTIC)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(VariationType.SEMANTIC))	
				.gather(termino);
		}
		
		/*
		 * Gathers extensions of morpho, derivative, prefix, and semantic variants
		 */
		new ExtensionVariantGatherer()
				.setHistory(history)
				.gather(termino);
		
		if(gathererOptions.isGraphicalGathererEnabled()) {
			LOGGER.info("Gathering graphical variants");
			new GraphicalGatherer()
					.setDistance(new FastDiacriticInsensitiveLevenshtein(false))
					.setSimilarityThreshold(gathererOptions.getGraphicalSimilarityThreshold())
					.setNbFixedLetters(termino.getLang().getGraphicalVariantNbPreindexingLetters())
					.setHistory(history)
					.gather(termino);
		}
		
		new GraphicalGatherer()
			.setDistance(new FastDiacriticInsensitiveLevenshtein(false))
			.setSimilarityThreshold(gathererOptions.getGraphicalSimilarityThreshold())
			.setIsGraphicalVariantProperties(termino);
		
		
		/*
		 * Merging terms
		 */
		new TermMerger()
				.setHistory(history)
				.mergeTerms(termino);
		
		/*
		 * Set the variant_frequency properties
		 */
		new TerminologyService(termino).variations()
				.forEach(r -> r.setProperty(RelationProperty.VARIANT_BAG_FREQUENCY, r.getTo().getFrequency()));


		sw.stop();
		LOGGER.debug("{} finished in {}", this.getClass().getSimpleName(), sw);
	}


}
