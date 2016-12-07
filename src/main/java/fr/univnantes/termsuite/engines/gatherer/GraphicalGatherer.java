package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.util.Lists;

import fr.univnantes.termsuite.metrics.DiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.metrics.EditDistance;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.TermIndexes;

public class GraphicalGatherer extends AbstractGatherer {

	private EditDistance distance = new DiacriticInsensitiveLevenshtein(Locale.ENGLISH);
	private double similarityThreshold = 1d;
	
	public GraphicalGatherer() {
		super();
		
		setRelationType(RelationType.GRAPHICAL);
		setNbFixedLetters(2);
	}

	public GraphicalGatherer setDistance(EditDistance distance) {
		this.distance = distance;
		return this;
	}
	
	public GraphicalGatherer setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
		return this;
	}
	
	public GraphicalGatherer setNbFixedLetters(int nbFixedLetters) {
		switch(nbFixedLetters){
		case 2:
			setIndexName(TermIndexes.FIRST_LETTERS_2, true);
			break;
		case 3:
			setIndexName(TermIndexes.FIRST_LETTERS_3, true);
			break;
		case 4:
			setIndexName(TermIndexes.FIRST_LETTERS_4, true);
			break;
		}
		return this;
	}
	
	
	@Override
	protected void gather(TermIndex termIndex, Collection<Term> termClass, String clsName) {
		List<Term> terms = termClass.getClass().isAssignableFrom(List.class) ? 
				(List<Term>) termClass
					: Lists.newArrayList(termClass);
	
		Term t1, t2;
		double dist;
		for(int i=0; i<terms.size();i++) {
			t1 = terms.get(i);
			for(int j=i+1; j<terms.size();j++) {
				cnt.incrementAndGet();
//				if(nbComparisons % OBSERVER_STEP == 0 && taskObserver.isPresent())
//					taskObserver.get().work(OBSERVER_STEP);
				t2 = terms.get(j);
				dist = distance.computeNormalized(t1.getLemma(), t2.getLemma());
				if(dist >= this.similarityThreshold) {
//					gatheredCnt++;
					TermRelation rel1 = new TermRelation(RelationType.GRAPHICAL, t1, t2);
					rel1.setProperty(RelationProperty.SIMILARITY, dist);
					termIndex.addRelation(rel1);
					TermRelation rel2 = new TermRelation(RelationType.GRAPHICAL, t2, t1);
					rel2.setProperty(RelationProperty.SIMILARITY, dist);
					termIndex.addRelation(rel2);
					watch(t1, t2, dist);
					watch(t2, t1, dist);

				}
			}
		}
		
		// log some stats
//		logger.debug("Graphical gathering {} terms gathered / {} pairs compared", gatheredCnt, nbComparisons);
		
//		progressLoggerTimer.cancel();
	}
	
	private void watch(Term t1, Term t2, double dist) {
		if(history.isPresent()) {
			if(history.get().isWatched(t1.getGroupingKey()))
				history.get().saveEvent(
						t1.getGroupingKey(),
						this.getClass(), 
						"Term has a new graphical variant " + t2 + " (dist="+dist+")");
		}
	}

}
