package fr.univnantes.termsuite.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.univnantes.termsuite.utils.JsonConfigObject;

public class TerminologyStats extends JsonConfigObject {

	@JsonProperty("number-of-terms")
	private int nbTerms;

	@JsonProperty("number-of-words")
	private int nbWords;

	@JsonProperty("number-of-singe-words")
	private int nbSingleWords;

	@JsonProperty("number-of-size-2-words")
	private int nbSize2Words;

	@JsonProperty("number-of-size-3-words")
	private int nbSize3Words;

	@JsonProperty("number-of-size-4-words")
	private int nbSize4Words;

	@JsonProperty("number-of-size-5-words")
	private int nbSize5Words;

	@JsonProperty("number-of-size-6-words")
	private int nbSize6Words;



	@JsonProperty("number-of-extensions")
	private int nbExtensions;

	@JsonProperty("number-of-prefixations")
	private int nbPrefixations;

	@JsonProperty("number-of-grpahical-variations")
	private int nbGraphical;

	@JsonProperty("number-of-derivations")
	private int nbDerivations;

	@JsonProperty("number-of-morphological-variations")
	private int nbMorphological;

	@JsonProperty("number-of-semantic-variations")
	private int nbSemantic;

	@JsonProperty("number-of-infered-variations")
	private int nbInfered;

	@JsonProperty("number-of-semantic-variations-from-dico")
	private int nbSemanticWithDico;

	@JsonProperty("number-of-semantic-variations-from-distrib")
	private int nbSemanticDistribOnly;

	@JsonProperty("number-of-compound-words")
	private int nbCompounds;

	@JsonProperty("number-of-variations")
	private int nbVariations;

	@JsonProperty("rule-distribution")
	private Map<String, Integer> ruleDistribution;

	@JsonProperty("pattern-distribution")
	private Map<String, Integer> patternDistribution;

	public int getNbTerms() {
		return nbTerms;
	}

	public void setNbTerms(int nbTerms) {
		this.nbTerms = nbTerms;
	}

	public int getNbWords() {
		return nbWords;
	}

	public void setNbWords(int nbWords) {
		this.nbWords = nbWords;
	}

	public int getNbSingleWords() {
		return nbSingleWords;
	}

	public void setNbSingleWords(int nbSingleWords) {
		this.nbSingleWords = nbSingleWords;
	}

	public int getNbSize2Words() {
		return nbSize2Words;
	}

	public void setNbSize2Words(int nbSize2Words) {
		this.nbSize2Words = nbSize2Words;
	}

	public int getNbSize3Words() {
		return nbSize3Words;
	}

	public void setNbSize3Words(int nbSize3Words) {
		this.nbSize3Words = nbSize3Words;
	}

	public int getNbSize4Words() {
		return nbSize4Words;
	}

	public void setNbSize4Words(int nbSize4Words) {
		this.nbSize4Words = nbSize4Words;
	}

	public int getNbSize5Words() {
		return nbSize5Words;
	}

	public void setNbSize5Words(int nbSize5Words) {
		this.nbSize5Words = nbSize5Words;
	}

	public int getNbSize6Words() {
		return nbSize6Words;
	}

	public void setNbSize6Words(int nbSize6Words) {
		this.nbSize6Words = nbSize6Words;
	}

	public int getNbCompounds() {
		return nbCompounds;
	}

	public void setNbCompounds(int nbCompounds) {
		this.nbCompounds = nbCompounds;
	}

	public int getNbVariations() {
		return nbVariations;
	}

	public void setNbVariations(int nbVariations) {
		this.nbVariations = nbVariations;
	}

	public Map<String, Integer> getRuleDistribution() {
		return ruleDistribution;
	}

	public void setRuleDistribution(Map<String, Integer> ruleDistribution) {
		this.ruleDistribution = ruleDistribution;
	}

	public Map<String, Integer> getPatternDistribution() {
		return patternDistribution;
	}

	public void setPatternDistribution(Map<String, Integer> patternDistribution) {
		this.patternDistribution = patternDistribution;
	}

	public int getNbExtensions() {
		return nbExtensions;
	}

	public void setNbExtensions(int nbExtensions) {
		this.nbExtensions = nbExtensions;
	}

	public int getNbPrefixations() {
		return nbPrefixations;
	}

	public void setNbPrefixations(int nbPrefixations) {
		this.nbPrefixations = nbPrefixations;
	}

	public int getNbGraphical() {
		return nbGraphical;
	}

	public void setNbGraphical(int nbGraphical) {
		this.nbGraphical = nbGraphical;
	}

	public int getNbDerivations() {
		return nbDerivations;
	}

	public void setNbDerivations(int nbDerivations) {
		this.nbDerivations = nbDerivations;
	}

	public int getNbMorphological() {
		return nbMorphological;
	}

	public void setNbMorphological(int nbMorphological) {
		this.nbMorphological = nbMorphological;
	}

	public int getNbSemantic() {
		return nbSemantic;
	}

	public void setNbSemantic(int nbSemantic) {
		this.nbSemantic = nbSemantic;
	}

	public int getNbInfered() {
		return nbInfered;
	}

	public void setNbInfered(int nbInfered) {
		this.nbInfered = nbInfered;
	}

	public int getNbSemanticWithDico() {
		return nbSemanticWithDico;
	}

	public void setNbSemanticWithDico(int nbSemanticWithDico) {
		this.nbSemanticWithDico = nbSemanticWithDico;
	}

	public int getNbSemanticDistribOnly() {
		return nbSemanticDistribOnly;
	}

	public void setNbSemanticDistribOnly(int nbSemanticDistribOnly) {
		this.nbSemanticDistribOnly = nbSemanticDistribOnly;
	}
	
}
