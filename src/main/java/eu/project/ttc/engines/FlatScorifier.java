package eu.project.ttc.engines;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.scored.ScoredTerm;
import eu.project.ttc.models.scored.ScoredVariation;
import eu.project.ttc.resources.ScoredModel;
import eu.project.ttc.resources.TermIndexResource;
import fr.univnantes.lina.UIMAProfiler;

public class FlatScorifier extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(FlatScorifier.class);

	@ExternalResource(key=ScoredModel.SCORED_MODEL, mandatory=true)
	private ScoredModel scoredModel;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	private static Comparator<ScoredTerm> wrComparator = new Comparator<ScoredTerm>() {
		@Override
		public int compare(ScoredTerm o1, ScoredTerm o2) {
			return Double.compare(o2.getWRLog(), o1.getWRLog());
		}
	};

	private static Comparator<ScoredVariation> variationScoreComparator = new Comparator<ScoredVariation>() {
		@Override
		public int compare(ScoredVariation o1, ScoredVariation o2) {
			return ComparisonChain.start()
					.compare(o2.getVariationScore(), o1.getVariationScore())
					.compare(o1.getTerm().getGroupingKey(), o2.getTerm().getGroupingKey())
					.compare(o1.getVariant().getTerm().getGroupingKey(), o2.getVariant().getTerm().getGroupingKey())
					.result();
			
		}
	};
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		LOGGER.info("Start flat scorifier");

		this.scoredModel.importTermIndex(termIndexResource.getTermIndex());
		
		this.scoredModel.sort(wrComparator);
		
		for(ScoredTerm t:this.scoredModel.getTerms()) {
			if(t.getVariations().isEmpty())
				continue;
			List<ScoredVariation> sv = Lists.newArrayListWithExpectedSize(t.getVariations().size());
			sv.addAll(t.getVariations());
			Collections.sort(sv, variationScoreComparator);
			t.setVariations(sv);
		}
		
		this.scoredModel.sort(wrComparator);
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
	
	private static  <T extends ScoredVariation> List<T> decrementAndPropagate(PriorityQueue<T>  inputTerms) {
		List<T> decrementedTerms = Lists.newLinkedList();
		T base;
		Collection<TermOccurrence> baseOccurrences;
		List<T> toUpdate;
		int nbRem;
		while(!inputTerms.isEmpty()) {
			System.out.println("--------------------");
			for(T t:inputTerms)
				System.out.println(t);
			base = inputTerms.poll();
			decrementedTerms.add(base);
			baseOccurrences = base.getOccurrences();
			toUpdate = Lists.newArrayList();
			Iterator<T> it = inputTerms.iterator();
			while(it.hasNext()) {
				T other = it.next();
				nbRem = other.removeOverlappingOccurrences(baseOccurrences);
				if(nbRem > 0 ) {
					toUpdate.add(other);
					it.remove();
				}
			}
			
			for(T so:toUpdate)
				inputTerms.add(so);
		} 
		return decrementedTerms;
	}

}
