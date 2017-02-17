package fr.univnantes.termsuite.io.other;

public class VariantEvalExporterOptions {
	private int nbVariantsPerTerm;
	private int contextSize;
	private int nbExampleOccurrences;
	private int topN;

	public VariantEvalExporterOptions() {
	}

	public int getNbVariantsPerTerm() {
		return nbVariantsPerTerm;
	}

	public void setNbVariantsPerTerm(int nbVariantsPerTerm) {
		this.nbVariantsPerTerm = nbVariantsPerTerm;
	}

	public int getContextSize() {
		return contextSize;
	}

	public void setContextSize(int contextSize) {
		this.contextSize = contextSize;
	}

	public int getNbExampleOccurrences() {
		return nbExampleOccurrences;
	}

	public void setNbExampleOccurrences(int nbExampleOccurrences) {
		this.nbExampleOccurrences = nbExampleOccurrences;
	}

	public int getTopN() {
		return topN;
	}

	public void setTopN(int topN) {
		this.topN = topN;
	}
}