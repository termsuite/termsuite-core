package eu.project.ttc.eval.aligner;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import eu.project.ttc.engines.BilingualAligner;
import eu.project.ttc.engines.BilingualAligner.RequiresSize2Exception;
import eu.project.ttc.engines.BilingualAligner.TranslationCandidate;
import eu.project.ttc.eval.AlignmentEvalRun;
import eu.project.ttc.eval.AlignmentEvalService;
import eu.project.ttc.eval.AlignmentRecord;
import eu.project.ttc.eval.ConfigListBuilder;
import eu.project.ttc.eval.Corpus;
import eu.project.ttc.eval.EvaluatedMethod;
import eu.project.ttc.eval.RunTrace;
import eu.project.ttc.eval.TermSuiteEvals;
import eu.project.ttc.eval.TerminoConfig;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.tools.TermSuiteAlignerBuilder;

public class BilingualAlignementEvalRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteEvals.class);

	private AlignmentEvalService service;
	
	
	public BilingualAlignementEvalRunner() {
		super();
		this.service = new AlignmentEvalService();
	}

	public static void main(String[] args) throws IOException {
		new BilingualAlignementEvalRunner().run();
	}

	
	private void run() throws IOException {
		LOGGER.info("Starting the bilingual aligner evaluation script");
		Stopwatch sw = Stopwatch.createStarted();
		

			
		service.langPairs().forEach( langPair -> {
			for(EvaluatedMethod evaluatedMethod:EvaluatedMethod.values()) {
				try(Writer resultWriter = service.getResultWriter(langPair, evaluatedMethod)) {
					for(TerminoConfig config:ConfigListBuilder.start().frequencies(1,5).scopes(1,3).list()) {
						for(Corpus corpus:Corpus.values()) {
							try {
								AlignmentEvalRun run = new AlignmentEvalRun(langPair, evaluatedMethod, corpus, config);
								if(service.hasRef(run.getCorpus(), run.getLangPair())) {
									runEval(run);
									service.saveRunTrace(run);
									service.writeResultLine(resultWriter, run);
								}
							} catch (DictionaryNotFound e) {
								LOGGER.warn("Skipping evaluation because dictionary not found: %s", e.getPath());
							}
						}
					}
				} catch (IOException e1) {
					LOGGER.error("IO error during eval", e1);
				}
			}
		});

		sw.stop();
		LOGGER.info("Finished evaluation of bilingual aligner in {}", sw.toString());
	}
	
	public RunTrace runEval(AlignmentEvalRun run) throws IOException, DictionaryNotFound {
		RunTrace trace = run.getTrace();
		
		LOGGER.info(String.format("Running evaluation %s", run));
		

		Path dicoPath = TermSuiteEvals.getDictionaryPath(run.getLangPair());
		if(!dicoPath.toFile().isFile()) 
			throw new DictionaryNotFound(dicoPath.toString());
		
		TermIndex sourceTermino = TermSuiteEvals.getTerminology(run.getCorpus(), run.getLangPair().getSource(), run.getTerminoConfig());
		TermIndex targetTermino = TermSuiteEvals.getTerminology(run.getCorpus(), run.getLangPair().getTarget(), run.getTerminoConfig());
		
		BilingualAligner aligner = TermSuiteAlignerBuilder.start()
				.setSourceTerminology(sourceTermino)
				.setTargetTerminology(targetTermino)
				.setDicoPath(dicoPath.toString())
				.setDistanceCosine()
				.create();
		
		service.getRefFile(run)
			.pairs(sourceTermino, targetTermino)
			.filter(pair -> run.getEvaluatedMethod().acceptPair(aligner, pair[0], pair[1]))
			.forEach(pair -> {
				Term expectedTerm = pair[1];
				Term sourceTerm = pair[0];
				try {
					LOGGER.debug("Aligning source term <{}>. Expecting target term <{}>", 
							sourceTerm.getGroupingKey(), 
							expectedTerm.getGroupingKey());
					
					List<TranslationCandidate> targets = run.getEvaluatedMethod().align(aligner, sourceTerm, 100, 1);
	
					if(targets.isEmpty()) {
						trace.newTry(new AlignmentRecord(sourceTerm, expectedTerm)
								.setValid(true)
								.setSuccess(false)
								.setComment("Empty candidate list"));
						LOGGER.debug("Candidate list returned by aligner is empty");
					} else {
						if(targets.get(0).getTerm().equals(expectedTerm)) {
							trace.newTry( new AlignmentRecord(sourceTerm, expectedTerm)
									.setValid(true)
									.setSuccess(true)
									.setMethod(targets.get(0).getMethod())
									.setComment("OK"));
							LOGGER.debug("SUCCESS");
						} else {
							OptionalInt index = IntStream.range(0, targets.size())
								.filter(i -> targets.get(i).getTerm().equals(expectedTerm))
								.findFirst();
			                if(index.isPresent()) {
								trace.newTry(new AlignmentRecord(sourceTerm, expectedTerm)
										.setValid(true)
										.setSuccess(false)
										.setMethod(targets.get(index.getAsInt()).getMethod())
										.setComment("Term found at position: " + index.getAsInt() + 1)
										.setTargetTermCandidatePosition( index.getAsInt() + 1));
										
								LOGGER.debug("FAILED. Position of expected term: {}",index.getAsInt() + 1);
			                } else {
								trace.newTry(new AlignmentRecord(sourceTerm, expectedTerm)
										.setValid(true)
										.setSuccess(false)
										.setComment("Expected term not found in candidates"));
								LOGGER.debug("FAILED. Target term not found in candidates");
			                	
			                }
						}
					}
				} catch(RequiresSize2Exception e) {
					trace.newTry(new AlignmentRecord(sourceTerm, expectedTerm)
							.setValid(false)
							.setComment("Source term has too many lemmas"));

					LOGGER.warn("Source term has too many lemmas <{}>. Alignment for such terms not yet implemented.", sourceTerm);
				}
			});
		
		return trace;
	}

}
