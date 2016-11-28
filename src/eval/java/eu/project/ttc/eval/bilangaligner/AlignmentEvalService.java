package eu.project.ttc.eval.bilangaligner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.util.Lists;

import eu.project.ttc.eval.TermSuiteEvals;
import eu.project.ttc.eval.model.Corpus;
import eu.project.ttc.eval.model.LangPair;
import eu.project.ttc.eval.resources.Tsv3ColFile;
import fr.univnantes.termsuite.model.Lang;

public class AlignmentEvalService {

	public Tsv3ColFile getRefFile(AlignmentEvalRun run) {
		return new Tsv3ColFile(getRefFilePath(run.getCorpus(), run.getLangPair()));
	}

	private Path getRefFilePath(Corpus corpus, LangPair pair) {
		return Paths.get("src", "eval", "resources", "refs", 
				corpus.getFullName(),
				String.format("%s-%s-%s.tsv", 
						corpus.getShortName(), 
						pair.getSource().getCode(), 
						pair.getTarget().getCode())
				);
	}
	
	public boolean hasRef(Corpus corpus, LangPair pair) {
		return getRefFilePath(corpus, pair).toFile().exists();
	}

	public boolean hasAnyRefForLangPair(LangPair pair) {
		for(Corpus corpus:Corpus.values())
			if(hasRef(corpus, pair))
				return true;
		return false;
	}

	public Stream<LangPair> langPairs() {
		List<LangPair> pairs = Lists.newArrayList();
		for(Lang source:Lang.values()) {
			for(Lang target:Lang.values()) {
				LangPair pair = new LangPair(source, target);
				if(hasAnyRefForLangPair(pair))
					pairs.add(pair);
			}
		}
		return pairs.stream();
	}

	
	public Path getLangPairPath(LangPair langPair) {
		Path path = TermSuiteEvals.getAlignmentDirectory().resolve(langPair.toString());
		path.toFile().mkdirs();
		return path;
	}
	
	public Path getEvaluatedMethodPath(LangPair langPair, EvaluatedMethod evaluatedMethod) {
		Path path = getLangPairPath(langPair).resolve(evaluatedMethod.toString());
		path.toFile().mkdirs();
		return path;
	}
	
	public void saveRunTrace(AlignmentEvalRun run) throws IOException {
		try(Writer writer = new FileWriter(getRunTracePath(run).toFile())) {
			writer.write(AlignmentRecord.toOneLineHeaders());
			writer.write('\n');
			
			run.getTrace().tries().forEach(e -> {
				try {
					writer.write(e.toOneLineString());
					writer.write('\n');
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			});
		}
	}

	
	public Path getRunTracePath(AlignmentEvalRun run) {
		return getEvaluatedMethodPath(run.getLangPair(), run.getEvaluatedMethod()).resolve(
				String.format("%s-%s", run.getCorpus(),  run.getTerminoConfig()));
	}

	public Writer getResultWriter(LangPair langPair, EvaluatedMethod evaluatedMethod) throws IOException {
		Path resultPath = getEvaluatedMethodPath(langPair, evaluatedMethod).resolve("results.txt");
		FileWriter writer = new FileWriter(resultPath.toFile());
		writer.write(String.format("%s\t%s\t%s\t%s\t%s%n", 
				"run",
				"pr",
				"tot",
				"suc",
				"ign"
				));
		return writer;
	}

	public void writeResultLine(Writer resultWriter, AlignmentEvalRun run) throws IOException {
		RunTrace trace = run.getTrace();
		resultWriter.write(String.format("%s\t%.2f\t%d\t%d\t%d%n", 
				run.getName(),
				trace.getPrecision(),
				trace.validResults().count(),
				trace.successResults().count(),
				trace.invalidResults().count()
				));
		resultWriter.flush();
	}
}
