package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.metrics.EditDistance;
import fr.univnantes.termsuite.metrics.FastDiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.termino.TermIndexes;

public class GraphicalGatherer extends AbstractGatherer {
	
	private EditDistance distance = new FastDiacriticInsensitiveLevenshtein(false);
	private double similarityThreshold = 1d;
	
	public GraphicalGatherer() {
		super();
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
		case 1:
			setIndexName(TermIndexes.FIRST_LETTERS_1, true);
			break;
		case 2:
			setIndexName(TermIndexes.FIRST_LETTERS_2, true);
			break;
		case 3:
			setIndexName(TermIndexes.FIRST_LETTERS_3, true);
			break;
		case 4:
			setIndexName(TermIndexes.FIRST_LETTERS_4, true);
			break;
		default:
			throw new IllegalArgumentException("Bad value for number of letters for a n-first-letters index: " + nbFixedLetters);
		}
		return this;
	}
	
	@Override
	protected void gather(TerminologyService termino, Collection<Term> termClass, String clsName) {
		
		List<Term> terms = termClass.getClass().isAssignableFrom(List.class) ? 
				(List<Term>) termClass
					: Lists.newArrayList(termClass);
	
		Term t1, t2;
		double dist;
		for(int i=0; i<terms.size();i++) {
			t1 = terms.get(i);
			for(int j=i+1; j<terms.size();j++) {
				cnt.incrementAndGet();
				t2 = terms.get(j);
				dist = distance.computeNormalized(t1.getLemma(), t2.getLemma(), this.similarityThreshold);
				if(dist >= this.similarityThreshold) {
					
					/*
					 * Direct the graphical connection according to frequency then specificity
					 */
					if(t1.getFrequency() > t2.getFrequency())
						createGraphicalRelation(termino, t1, t2, dist);
					else if(t1.getFrequency() == t2.getFrequency() && t1.getSpecificity() > t2.getSpecificity())
						createGraphicalRelation(termino, t1, t2, dist);
					else
						createGraphicalRelation(termino, t2, t1, dist);
				}
			}
		}
	}
	
	protected TermRelation createGraphicalRelation(TerminologyService termino, Term from, Term to, Double similarity) {
		TermRelation rel = termino.createVariation(VariationType.GRAPHICAL, from, to);
		rel.setProperty(RelationProperty.GRAPHICAL_SIMILARITY, similarity);
		watch(from, to, similarity);
		return rel;
	}
	
	private void watch(Term from, Term to, double dist) {
		if(history.isPresent()) {
			if(history.get().isGKeyWatched(from.getGroupingKey()))
				history.get().saveEvent(
						from.getGroupingKey(),
						this.getClass(), 
						"Term has a new graphical variant " + to + " (dist="+dist+")");
			if(history.get().isGKeyWatched(to.getGroupingKey()))
				history.get().saveEvent(
						to.getGroupingKey(),
						this.getClass(), 
						"Term has a new graphical base " + from + " (dist="+dist+")");
		}
	}

	public void setIsGraphicalVariantProperties(Terminology termino) {
		termino.getRelations(RelationType.VARIATION)
		.filter(r -> r.isPropertySet(RelationProperty.IS_GRAPHICAL) 
				&& !r.getPropertyBooleanValue(RelationProperty.IS_GRAPHICAL))
		.forEach(r -> {
			double similarity = distance.computeNormalized(
					r.getFrom().getLemma(), 
					r.getTo().getLemma(), 
					this.similarityThreshold) ;
			boolean isGraphicalVariant = similarity >= this.similarityThreshold;
			r.setProperty(RelationProperty.IS_GRAPHICAL, isGraphicalVariant);
			if(isGraphicalVariant)
				r.setProperty(RelationProperty.GRAPHICAL_SIMILARITY, similarity);
			
			if(history.isPresent()) {
				if(history.get().isGKeyWatched(r.getFrom().getGroupingKey())
						|| history.get().isGKeyWatched(r.getTo().getGroupingKey()))
					history.get().saveEvent(
							r.getFrom().getGroupingKey(),
							this.getClass(), 
							"Variation "+r+" is marked as graphical (dist="+similarity+")");
					history.get().saveEvent(
						r.getTo().getGroupingKey(),
							this.getClass(), 
							"Variation "+r+" is marked as graphical (dist="+similarity+")");
			}

		});

		
	}

}
