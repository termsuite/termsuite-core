package fr.univnantes.termsuite.eval.bilangaligner;

public class TerminoConfig {
	
	private int scope = 3;
	private int frequencyTh = 2;
	private int coocFrequencyTh = 1;

	public TerminoConfig() {
		super();
	}

	public int getScope() {
		return scope;
	}

	public TerminoConfig setScope(int scope) {
		this.scope = scope;
		return this;
	}

	public int getFrequencyTh() {
		return frequencyTh;
	}

	public TerminoConfig setFrequencyTh(int frequencyTh) {
		this.frequencyTh = frequencyTh;
		return this;
	}

	public int getCoocFrequencyTh() {
		return coocFrequencyTh;
	}

	public TerminoConfig setCoocFrequencyTh(int coocFrequencyTh) {
		throw new UnsupportedOperationException("Not yet implemented");
	}	
	
	@Override
	public String toString() {
		return String.format("scope%d-th%d", scope, frequencyTh);
	}
}
