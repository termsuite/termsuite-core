package eu.project.ttc.eval;

public class AlignmentEvalRun {

	private LangPair langPair;
	private EvaluatedMethod evaluatedMethod;
	private Corpus corpus;
	private TerminoConfig terminoConfig;
	
	/*
	 * Inner params
	 */
	private RunTrace trace;

	
	public AlignmentEvalRun(LangPair langPair, EvaluatedMethod evaluatedMethod, Corpus corpus, TerminoConfig config) {
		this.langPair = langPair;
		this.evaluatedMethod = evaluatedMethod;
		this.corpus = corpus;
		this.terminoConfig = config;
		this.trace = new RunTrace();
	}


	public LangPair getLangPair() {
		return langPair;
	}


	public EvaluatedMethod getEvaluatedMethod() {
		return evaluatedMethod;
	}


	public Corpus getCorpus() {
		return corpus;
	}


	public TerminoConfig getTerminoConfig() {
		return terminoConfig;
	}


	public RunTrace getTrace() {
		return trace;
	}


	public String getName() {
		return String.format("run-%s-%s-%s-%s",
				langPair,
				corpus,
				terminoConfig,
				evaluatedMethod);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
