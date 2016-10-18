package eu.project.ttc.eval.aligner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.BilingualAligner;
import eu.project.ttc.engines.BilingualAligner.TranslationCandidate;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.eval.Corpus;
import eu.project.ttc.eval.TermSuiteEvals;
import eu.project.ttc.eval.TermType;
import eu.project.ttc.eval.TerminoConfig;
import eu.project.ttc.eval.Tsv2ColFile;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.tools.TermSuiteAlignerBuilder;

public class BilingualAlignementEvalRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteEvals.class);

	private static List<TerminoConfig> TERMINO_CONFIGS = Lists.newArrayList(
				new TerminoConfig()
			);
	public static void main(String[] args) throws IOException {
		LOGGER.info("Starting the bilingual aligner evaluation script");
		Stopwatch sw = Stopwatch.createStarted();
		
		for(Lang source:Lang.values()) {
			for(Lang target:Lang.values()) {
				for(Corpus corpus:Corpus.values()) {
					for(TermType termType:TermType.values()) {
						if(corpus.hasRef(termType, source, target)) {
							for(TerminoConfig config:TERMINO_CONFIGS)
								runEval(source, target, corpus, termType, config);
						}
					}
				}
			}			
		}
		sw.stop();
		LOGGER.info("Finished evaluation of bilingual aligner in {}", sw.toString());
	}
	
	public static void runEval(Lang source, Lang target, Corpus corpus, TermType termType, TerminoConfig config) throws IOException {
		
		
		LOGGER.info(String.format("Running evaluation [corpus: %s, source: %s, target: %s, type: %s]",
				corpus.getFullName(),
				source.getName(),
				target.getName(),
				termType));

		Path dicoPath = TermSuiteEvals.getDictionaryPath(source, target);
		if(!dicoPath.toFile().isFile()) {
			LOGGER.warn("Skipping evaluation because dictionary not found: %s", dicoPath);
			return;
		}
		
		TermIndex sourceTermino = TermSuiteEvals.getTerminology(corpus, source, config);
		TermIndex targetTermino = TermSuiteEvals.getTerminology(corpus, target, config);
		
		BilingualAligner aligner = TermSuiteAlignerBuilder.start()
				.setSourceTerminology(sourceTermino)
				.setTargetTerminology(targetTermino)
				.setDicoPath(dicoPath.toString())
				.setDistanceCosine()
				.create();
		
		
		new Tsv2ColFile(corpus.getRef(termType, source, target))
			.termPairs(sourceTermino, targetTermino)
			.forEach(pair -> {
				Term expectedTerm = pair[1];
				Term sourceTerm = pair[0];
				LOGGER.debug("Aligning source term <{}>. Expecting target term <{}>", 
						sourceTerm.getGroupingKey(), 
						expectedTerm.getGroupingKey());
				
		
				List<TranslationCandidate> targets = aligner.align(sourceTerm, 100, 1);

				if(targets.isEmpty()) {
					LOGGER.warn("Candidate list returned by aligner is empty");
				} else {
					if(targets.get(0).getTerm().equals(expectedTerm))
						LOGGER.debug("SUCCESS");
					else {
						OptionalInt index = IntStream.range(0, targets.size())
							.filter(i -> targets.get(i).getTerm().equals(expectedTerm))
							.findFirst();
		                	
						LOGGER.debug("FAIL. Position of expected term: {}",index.isPresent() ? index.getAsInt() + 1 : "[not found in candidate list]");
					}
				}
			});
	}

}
