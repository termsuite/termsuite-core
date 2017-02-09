package fr.univnantes.termsuite.framework.service;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class TermService {
	
	private TerminologyService terminology;
	private Term term;

	public ContextVector getContext() {
		return term.getContext();
	}

	public Double getOrthographicScore() {
		return term.getOrthographicScore();
	}

	public Integer getIndependantFrequency() {
		return term.getIndependantFrequency();
	}

	public Double getIndependance() {
		return term.getIndependance();
	}

	public Integer getSwtSize() {
		return term.getSwtSize();
	}

	public Number getNumber(TermProperty property) {
		return term.getNumber(property);
	}

	public TermService(TerminologyService terminology, Term term) {
		super();
		Preconditions.checkNotNull(term);
		Preconditions.checkNotNull(terminology);
		this.terminology = terminology;
		this.term = term;
	}
	
	@Override
	public int hashCode() {
		return term.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermService) {
			return ((TermService) obj).term.equals(term);
		} else
			return term.equals(obj);
	}
	
	@Override
	public String toString() {
		return term.toString();
	}

	public boolean notFiltered() {
		return !isFiltered();
	}

	public boolean isFiltered() {
		return isPropertySet(TermProperty.FILTERED) && term.getBoolean(TermProperty.FILTERED);
	}

	public boolean isCompound() {
		return isSingleWord() && this.term.getWords().get(0).getWord().isCompound();
	}
	
	/**
	 * Returns the concatenation of inner words' lemmas.
	 */
	public String getLemma() {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for(TermWord tw:this.term.getWords()) {
			if(i>0)
				builder.append(TermSuiteConstants.WHITESPACE);
			builder.append(tw.getWord().getLemma());
			i++;
		}
		return builder.toString();
	}


	/* 
	 * *******************************************************************************
	 * PROPERTY GETTERS/SETTERS
	 * *******************************************************************************
	 */
	

	/*
	 * GROUPING_KEY
	 */
	public String getGroupingKey() {
		return term.getString(TermProperty.GROUPING_KEY);		
	}
	
	/*
	 * DOCUMENT_FREQUENCY
	 */
	public Integer getDocumentFrequency() {
		return term.getInteger(TermProperty.DOCUMENT_FREQUENCY);
	}

	public void setDocumentFrequency(int documentFrequency) {
		term.setProperty(TermProperty.DOCUMENT_FREQUENCY, documentFrequency);
	}
	
	/*
	 * FREQUENCY
	 */
	public Integer getFrequency() {
		return term.getInteger(TermProperty.FREQUENCY);		
	}

	public void setFrequency(int frequency) {
		term.setProperty(TermProperty.FREQUENCY, frequency);
	}
	
	/*
	 * PATTERN
	 */
	public String getPattern() {
		return term.getString(TermProperty.PATTERN);		
	}
	
	public void setPattern(String pattern) {
		term.setProperty(TermProperty.PATTERN, pattern);
	}
	
	/*
	 * PILOT
	 */
	public String getPilot() {
		return term.getString(TermProperty.PILOT);
	}
	public void setPilot(String pilot) {
		term.setProperty(TermProperty.PILOT, pilot);
	}

	/*
	 * SPOTTING_RULE
	 */
	public String getSpottingRule() {
		return term.getString(TermProperty.SPOTTING_RULE);		
	}

	public void setSpottingRule(String spottingRule) {
		term.setProperty(TermProperty.SPOTTING_RULE, spottingRule);
	}
	
	/*
	 * GENERAL_FREQUENCY_NORM
	 */
	public Double getGeneralFrequencyNorm() {
		return term.getDouble(TermProperty.GENERAL_FREQUENCY_NORM);
	}
	
	public void setGeneralFrequencyNorm(double normalizedGeneralTermFrequency) {
		term.setProperty(TermProperty.GENERAL_FREQUENCY_NORM, normalizedGeneralTermFrequency);
	}
	
	/*
	 * FREQUENCY_NORM
	 */
	public Double getFrequencyNorm() {
		return term.getDouble(TermProperty.FREQUENCY_NORM);
	}
	
	public void setFrequencyNorm(double normalizedTermFrequency) {
		term.setProperty(TermProperty.FREQUENCY_NORM, normalizedTermFrequency);
	}
	
	/*
	 * RANK
	 */
	public Integer getRank() {
		return term.getInteger(TermProperty.RANK);
	}
	
	public void setRank(int rank) {
		term.setProperty(TermProperty.RANK, rank);
	}
	
	/*
	 * SPECIFICITY
	 */
	public Double getSpecificity() {
		return term.getDouble(TermProperty.SPECIFICITY);
	}
	
	public void setSpecificity(double specificity) {
		term.setProperty(TermProperty.SPECIFICITY, specificity);
	}
	
	/*
	 * IS_FIXED_EXPRESSION
	 */
	public Boolean isFixedExpression() {
		return term.getBoolean(TermProperty.IS_FIXED_EXPRESSION);
	}
	
	public void setFixedExpression(boolean fixedExpression) {
		term.setProperty(TermProperty.IS_FIXED_EXPRESSION, fixedExpression);
	}

	/*
	 * TF_IDF
	 */
	public Double getTfIdf() {
		return term.getDouble(TermProperty.TF_IDF);
	}

	public void setTfIdf(double tfIdf) {
		term.setProperty(TermProperty.TF_IDF, tfIdf);
	}

	public Number getPropertyNumberValue(TermProperty p) {
		return (Number)term.get(p);
	}

	public void setDepth(int depth) {
		term.setProperty(TermProperty.DEPTH, depth);
	}
	
	public Integer getDepth() {
		return term.getInteger(TermProperty.DEPTH);
	}

	public boolean isSingleWord() {
		return term.getWords().size() == 1;
	}
	
	public boolean isMultiWord() {
		return term.getWords().size() > 1;
	}
	
	
	private Semaphore frequencyMutex = new Semaphore(1);
	public void incrementFrequency(int increment) {
		frequencyMutex.acquireUninterruptibly();
		if(term.isPropertySet(TermProperty.FREQUENCY))
			term.setProperty(
					TermProperty.FREQUENCY, 
					term.getInteger(TermProperty.FREQUENCY) + increment);
		else
			term.setProperty(
					TermProperty.FREQUENCY, 
					increment);
		frequencyMutex.release();
	}

	public Term getTerm() {
		return this.term;
	}

	public void setProperty(TermProperty property, Comparable<?> value) {
		this.term.setProperty(property, value);
	}

	public void dropContext() {
		this.term.setContext(null);
	}

	public Stream<RelationService> extensions() {
		return terminology.extensions(this.getTerm());
	}

	public List<TermWord> getWords() {
		return term.getWords();
	}

	public Stream<RelationService> inboundRelations() {
		return terminology.inboundRelations(term);
	}

	public Stream<RelationService> inboundRelations(RelationType relType, RelationType... relTypes) {
		return terminology.inboundRelations(term, relType, relTypes);
	}

	public Stream<RelationService> outboundRelations(RelationType relType, RelationType... relTypes) {
		return terminology.outboundRelations(term, relType, relTypes);
	}

	public Stream<RelationService> outboundRelations() {
		return terminology.outboundRelations(term);
	}

	public boolean isContextSet() {
		return this.term.getContext()!= null;
	}
	
	public void updateTfIdf() {
		term.setProperty(
				TermProperty.TF_IDF, 
				(double)term.getFrequency()/term.getDocumentFrequency());
	}
	
	public void updateSpecificity() {
		term.setProperty(
				TermProperty.SPECIFICITY, 
				Math.log10(1 + term.getFrequencyNorm()/term.getGeneralFrequencyNorm()));
	}

	public boolean isPropertySet(TermProperty property) {
		return term.isPropertySet(property);
	}

	public Stream<RelationService> extensionBases() {
		return terminology.extensionBases(term);
	}

	public Stream<RelationService> variations() {
		return terminology.variationsFrom(term);
	}

	public Stream<RelationService> variationBases() {
		return terminology.variationsTo(term);
	}

	public Stream<RelationService> variations(VariationType variationType) {
		return variations().filter(rel -> rel.getBooleanIfSet(variationType.getRelationProperty()));
	}
}
