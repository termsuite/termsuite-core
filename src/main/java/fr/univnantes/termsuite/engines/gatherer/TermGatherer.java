package fr.univnantes.termsuite.engines.gatherer;

import java.math.BigInteger;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.metrics.FastDiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermIndex;
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
	
	
	public void gather(TermIndex termIndex) {
		Stopwatch sw = Stopwatch.createStarted();
		
		GroovyService groovyService = new GroovyService(termIndex);

		if(gathererOptions.isPrefixationGathererEnabled()) {
			LOGGER.info("Gathering prefixation variants");
			new AbstractGatherer()
				.setRuleType(RuleType.PREFIXATION)
				.setIndexName(TermIndexes.PREFIXATION_LEMMAS, true)
				.setRelationType(RelationType.SYNTACTICAL)
				.setGroovyAdapter(groovyService)
				.setHistory(history)
				.setVariantRules(rules.getVariantRules(RuleType.PREFIXATION))	
				.gather(termIndex);
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
				.gather(termIndex);
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
				.gather(termIndex);
		} 
		
		LOGGER.info("Gathering syntagmatic variants");
		new AbstractGatherer()
			.setIndexName(TermIndexes.ALLCOMP_PAIRS, true)
			.setRuleType(RuleType.SYNTAGMATIC)
			.setRelationType(RelationType.SYNTACTICAL)
			.setGroovyAdapter(groovyService)
			.setHistory(history)
			.setVariantRules(rules.getVariantRules(RuleType.SYNTAGMATIC))	
			.gather(termIndex);

		if(gathererOptions.isSemanticGathererEnabled()) {
			LOGGER.info("Gathering morphological semantic variants");
			new SemanticGatherer()
			.setDictionary(dico)
			.setRuleType(RuleType.SEMANTIC)
			.setRelationType(RelationType.SYNONYMIC)
			.setGroovyAdapter(groovyService)
			.setHistory(history)
			.setVariantRules(rules.getVariantRules(RuleType.SEMANTIC))	
			.gather(termIndex);
		}
		
		
		if(gathererOptions.isGraphicalGathererEnabled()) {
			LOGGER.info("Gathering graphical variants");
			new GraphicalGatherer()
				.setDistance(new FastDiacriticInsensitiveLevenshtein(false))
				.setSimilarityThreshold(gathererOptions.getGraphicalSimilarityThreshold())
				.setNbFixedLetters(termIndex.getLang().getGraphicalVariantNbPreindexingLetters())
				.setHistory(history)
				.gather(termIndex);
		}

		sw.stop();
		LOGGER.debug("{} finished in {}", this.getClass().getSimpleName(), sw);
	}

//	public void oldGather(TermIndex termIndex) {
//		if(termIndex.getTerms().isEmpty())
//			return;
//
//		/*
//		 * Prepare observer and indexes
//		 */
//		for(RunConfig runConfig:RUN_CONFIGS) {
//			LOGGER.debug("Gathering terms with index {} - rule type: {}", runConfig.indexName, runConfig.variantRuleIndex);
//			
//			CustomTermIndex customIndex = termIndex.getCustomIndex(runConfig.indexName);
//			customIndex.cleanSingletonKeys();
//			// clean biggest classes
//			customIndex.cleanEntriesByMaxSize(WARNING_CRITICAL_SIZE);
//
//			LOGGER.debug("Number of classes having size > 1: {}", customIndex.keySet().size());
//			BigInteger bSize = new BigInteger("0");
//			for(String cls:customIndex.keySet()) {
//				long classSize = customIndex.getTerms(cls).size();
//				bSize = bSize.add(new BigInteger(Long.toString(classSize*(classSize-1)/2)));
//			}
//			LOGGER.debug("Number of raw comparisons: {}", bSize);
//			Set<Pair<Term>> comparedPairs = new HashSet<>();
//			for(String cls:customIndex.keySet()) {
//				List<Term> terms = customIndex.getTerms(cls);
//				comparedPairs.addAll(Lists.cartesianProduct(terms, terms)
//					.stream()
//					.filter(p -> !p.get(0).equals(p.get(1)))
//					.map(p->new Pair<Term>(p.get(0), p.get(1)))
//					.collect(Collectors.toSet()));
//			}
//			LOGGER.debug("Number of term pairs: {}", comparedPairs.size());
//			
//			
//
//			CustomIndexStats stats = new CustomIndexStats(customIndex);
//
//			
//			// Display class sizes
//			Stopwatch sw1 = Stopwatch.createStarted();
//			int k = 0;
//			LOGGER.debug("Biggest class is {}, size: {}", stats.getBiggestClass(), stats.getBiggestSize());
//			
//			
//			int size;
//			for(Integer i:stats.getSizeCounters().keySet()) {
//				k ++;
//				size = stats.getSizeCounters().get(i).size();
//				totalComparisons = totalComparisons.add(BigInteger.valueOf(size * i*(i-1)));
//			}
//			LOGGER.debug("Number of term pairs to test: " + totalComparisons);
//			sw1.stop();
//			LOGGER.debug("Time to get the comparisons number: " + sw1.elapsed(TimeUnit.MILLISECONDS));
//			LOGGER.debug("Number of classes: " + k);
//			if(taskObserver.isPresent())
//				taskObserver.get().setTotalTaskWork(totalComparisons.longValue());
//		}
//
//
//		LOGGER.debug("Gathering with default variant rule indexing (source and target patterns)");
//		for(RunConfig runConfig:RUN_CONFIGS) {
//			gather(termIndex, runConfig.indexName, runConfig.variantRuleIndex);
//			termIndex.dropCustomIndex(runConfig.indexName);
//		}
//	}
//	
//	private void gather(TermIndex termIndex, final String gatheringKey, VariantRuleIndex variantRuleIndex) {
//		LOGGER.debug("Rule-based gathering over the pregathering key {}", gatheringKey);
//
//		// create the index
//		CustomTermIndex customIndex = termIndex.getCustomIndex(gatheringKey);
//		LOGGER.debug("Rule-based gathering over {} classes", customIndex.size());
//
//
//		// Log the progress every 5 seconds
//		Timer progressLoggerTimer = new Timer("Syn. Variant Gathering Timer");
//		progressLoggerTimer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				LOGGER.info("progress for key {}: ({}%)",
//						gatheringKey,
//						String.format("%.2f", ((float)nbComparisons*100)/totalComparisons.longValue())
//						);
//			}
//		}, 5000l, 5000l);
//		
//		// Do the gathering in each class
//		nbComparisons = 0;
//		for (String cls : customIndex.keySet()) {
//			List<Term> list = customIndex.getTerms(cls);
//			List<String> examples = Lists.newLinkedList();
//			int cnt =0;
//			for(Term t:list) {
//				examples.add(t.getGroupingKey());
//				cnt++;
//				if(cnt > 5)
//					break;
//			}
//			if(list.size() > 1 && LOGGER.isTraceEnabled())
//				LOGGER.trace("Rule-based gathering over the '" + cls + "' term class of size " + list.size() + ": " + Joiner.on(" ").join(examples));
//
//			
//			Term source;
//			Term target;
//			for(ListIterator<Term> sourceIt=list.listIterator(); sourceIt.hasNext();) {
//				source=sourceIt.next();
//				for(ListIterator<Term> targetIt=list.listIterator(sourceIt.nextIndex()); targetIt.hasNext();) {
//					nbComparisons+=2;
//					target=targetIt.next();
//					
//					applyGatheringRules(termIndex, variantRuleIndex, source, target);
//					applyGatheringRules(termIndex, variantRuleIndex, target, source);
//					if(nbComparisons % OBSERVING_STEP == 0) 
//						if(taskObserver.isPresent())
//							taskObserver.get().work(OBSERVING_STEP);
//				}
//					
//			}
//		}
//		
//		//finalize
//		progressLoggerTimer.cancel();
//	}

}
