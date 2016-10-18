package eu.project.ttc.eval.aligner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;

import eu.project.ttc.engines.BilingualAligner;
import eu.project.ttc.engines.BilingualAligner.RequiresSize2Exception;
import eu.project.ttc.engines.BilingualAligner.TranslationCandidate;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.eval.AlignmentTry;
import eu.project.ttc.eval.ConfigListBuilder;
import eu.project.ttc.eval.Corpus;
import eu.project.ttc.eval.RunTrace;
import eu.project.ttc.eval.TermList;
import eu.project.ttc.eval.TermSuiteEvals;
import eu.project.ttc.eval.TerminoConfig;
import eu.project.ttc.eval.Tsv2ColFile;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.tools.TermSuiteAlignerBuilder;

public class BilingualAlignementEvalRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteEvals.class);

	
	public BilingualAlignementEvalRunner() {
		super();
	}

	public static void main(String[] args) throws IOException {
		new BilingualAlignementEvalRunner().run();
	}

	private void run() throws IOException {
		LOGGER.info("Starting the bilingual aligner evaluation script");
		Stopwatch sw = Stopwatch.createStarted();
		
		List<TerminoConfig> configs = ConfigListBuilder.start().frequencies(1,2).scopes(2,3).list();

			
		for(Lang source:Lang.values()) {
			for(Lang target:Lang.values()) {
				if(!TermSuiteEvals.hasAnyRefForPair(source, target))
					continue;
				
				Path parent = TermSuiteEvals.getAlignmentDirectory().resolve(String.format("%s-%s", source.getCode(), target.getCode()));
				parent.toFile().mkdirs();
				
				String fileName = String.format("%s-%s.txt", 
						source.getCode(),
						target.getCode());
				Path langPairResultFile = parent.resolve(fileName);
				try(Writer langPairResultWriter = new FileWriter(langPairResultFile.toFile())) {
					
					langPairResultWriter.write(String.format("%s\t%s\t%s\t%s\t%s%n", 
							"run",
							"pr",
							"tot",
							"suc",
							"ign"
							));
					
					for(Corpus corpus:Corpus.values()) {
	//					for(TermType termType:TermType.values()) {
							if(corpus.hasRef(TermList.SWT, source, target)) {
								
								
								for(TerminoConfig config:configs) {
									try {
										String runName = String.format("%s-th%s-scope%d", 
												corpus.getShortName(),
												Integer.toString((int)config.getFrequencyTh()),
												config.getScope()
											);
										Path traceFile = parent.resolve(runName);
										try(Writer traceWriter = new FileWriter(traceFile.toFile())) {
											RunTrace trace = runEval(runName, source, target, corpus, TermList.SWT, config);
										
											traceWriter.write(AlignmentTry.toOneLineHeaders());
											traceWriter.write('\n');

											trace.tries().forEach(e -> {
												try {
													traceWriter.write(e.toOneLineString());
													traceWriter.write('\n');
												} catch (IOException e1) {
													throw new RuntimeException(e1);
												}
											});
											
											langPairResultWriter.write(String.format("%s\t%.2f\t%d\t%d\t%d%n", 
													trace.getRunName(),
													trace.getPrecision(),
													trace.validResults().count(),
													trace.successResults().count(),
													trace.invalidResults().count()
													));
											langPairResultWriter.flush();
										}

									} catch (DictionaryNotFound e) {
										LOGGER.warn("Skipping evaluation because dictionary not found: %s", e.getPath());
									}
								}
							}
	//					}
					}
					
					langPairResultWriter.flush();
					
				}			
			}
		}
		sw.stop();
		LOGGER.info("Finished evaluation of bilingual aligner in {}", sw.toString());
	}
	
	public RunTrace runEval(String runName, Lang source, Lang target, Corpus corpus, TermList termType, TerminoConfig config) throws IOException, DictionaryNotFound {
		
		RunTrace trace = new RunTrace(runName);
		
		
		LOGGER.info(String.format("Running evaluation [corpus: %s, source: %s, target: %s, type: %s]",
				corpus.getFullName(),
				source.getName(),
				target.getName(),
				termType));

		Path dicoPath = TermSuiteEvals.getDictionaryPath(source, target);
		if(!dicoPath.toFile().isFile()) 
			throw new DictionaryNotFound(dicoPath.toString());
		
		TermIndex sourceTermino = TermSuiteEvals.getTerminology(corpus, source, config);
		TermIndex targetTermino = TermSuiteEvals.getTerminology(corpus, target, config);
		
		BilingualAligner aligner = TermSuiteAlignerBuilder.start()
				.setSourceTerminology(sourceTermino)
				.setTargetTerminology(targetTermino)
				.setDicoPath(dicoPath.toString())
				.setDistanceCosine()
				.create();
		
		CustomTermIndex sourceLemmaIndex = sourceTermino.getCustomIndex(TermIndexes.LEMMA_LOWER_CASE);
		CustomTermIndex targetLemmaIndex = targetTermino.getCustomIndex(TermIndexes.LEMMA_LOWER_CASE);

		new Tsv2ColFile(corpus.getRef(termType, source, target))
			.lines()
			.filter(line -> {
				if(!sourceLemmaIndex.containsKey(line[0])) {
					LOGGER.debug("Ignoring ref line <{}> (term not found in source terminology)", Joiner.on(" ").join(line));
					trace.newTry(new AlignmentTry()
							.setValid(false)
							.setSourceLemma(line[0])
							.setTargetLemma(line[1])
							.setComment("Term not found in source termino: " + line[0]));
					return false;
				}
				if(!targetLemmaIndex.containsKey(line[1])) {
					LOGGER.debug("Ignoring ref line <{}> (term not found in target terminology)", Joiner.on(" ").join(line));
					trace.newTry(new AlignmentTry()
							.setValid(false)
							.setSourceLemma(line[0])
							.setTargetLemma(line[1])
							.setComment("Term not found in target termino: " + line[1]));
					return false;
				}
				return true;
			}).map(line -> {
				List<Term> sources = sourceLemmaIndex.getTerms(line[0]);
				Collections.sort(sources, TermProperty.FREQUENCY.getComparator(true));
				List<Term> targets = targetLemmaIndex.getTerms(line[1]);
				Collections.sort(targets, TermProperty.FREQUENCY.getComparator(true));
				LOGGER.debug("Reading eval pair. Source: <{}>. Target: <{}>", sources.get(0), targets.get(0));

				return new Term[]{sources.get(0), targets.get(0)};
			}).forEach(pair -> {
				Term expectedTerm = pair[1];
				Term sourceTerm = pair[0];
				try {
					LOGGER.debug("Aligning source term <{}>. Expecting target term <{}>", 
							sourceTerm.getGroupingKey(), 
							expectedTerm.getGroupingKey());
					
					List<TranslationCandidate> targets = aligner.align(sourceTerm, 100, 1);
	
					if(targets.isEmpty()) {
						trace.newTry(new AlignmentTry(sourceTerm, expectedTerm)
								.setValid(true)
								.setSuccess(false)
								.setComment("Empty candidate list"));
						LOGGER.debug("Candidate list returned by aligner is empty");
					} else {
						if(targets.get(0).getTerm().equals(expectedTerm)) {
							trace.newTry( new AlignmentTry(sourceTerm, expectedTerm)
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
								trace.newTry(new AlignmentTry(sourceTerm, expectedTerm)
										.setValid(true)
										.setSuccess(false)
										.setMethod(targets.get(index.getAsInt()).getMethod())
										.setComment("Term found at position: " + index.getAsInt() + 1)
										.setTargetTermCandidatePosition( index.getAsInt() + 1));
										
								LOGGER.debug("FAILED. Position of expected term: {}",index.getAsInt() + 1);
			                } else {
								trace.newTry(new AlignmentTry(sourceTerm, expectedTerm)
										.setValid(true)
										.setSuccess(false)
										.setComment("Expected term not found in candidates"));
								LOGGER.debug("FAILED. Target term not found in candidates");
			                	
			                }
						}
					}
				} catch(RequiresSize2Exception e) {
					LOGGER.warn("Source term has too many lemmas <{}>. Alignment for such terms not yet implemented.", sourceTerm);
				}
			});
		
		return trace;
	}

}
