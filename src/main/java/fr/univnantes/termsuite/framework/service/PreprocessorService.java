package fr.univnantes.termsuite.framework.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;

import fr.univnantes.termsuite.api.CorpusMetadata;
import fr.univnantes.termsuite.api.PreparedCorpus;
import fr.univnantes.termsuite.api.ResourceOptions;
import fr.univnantes.termsuite.api.TextCorpus;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.PipelineListener;
import fr.univnantes.termsuite.uima.PreparationPipelineOptions;
import fr.univnantes.termsuite.utils.TermHistory;

public interface PreprocessorService {

	Stream<JCas> prepare(TextCorpus textCorpus, Path taggerPath, PreparationPipelineOptions options,
			Optional<ResourceOptions> resourceOpts, Optional<TermHistory> termHistory,
			Optional<PipelineListener> listener, AnalysisEngineDescription... customAEs);

	JCas createCas(Document document, CorpusMetadata corpusMetadata) throws UIMAException, IOException;

	void toXMIPath(Path filePath, JCas cas);

	void toJsonPath(Path filePath, JCas cas);

	void consumeToTargetJsonCorpus(JCas cas, TextCorpus textCorpus, PreparedCorpus targetCorpus);

	void consumeToTargetXMICorpus(JCas cas, TextCorpus textCorpus, PreparedCorpus targetCorpus);

	void consumeToTerminology(Stream<JCas> cases, Terminology terminology, int maxSize);

	String generateTerminologyName(TextCorpus textCorpus);

	void consumeToTerminology(Stream<JCas> cases, Terminology terminology);

}