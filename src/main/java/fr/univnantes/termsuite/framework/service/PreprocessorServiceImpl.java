package fr.univnantes.termsuite.framework.service;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import fr.univnantes.termsuite.api.CorpusMetadata;
import fr.univnantes.termsuite.api.PreparedCorpus;
import fr.univnantes.termsuite.api.ResourceOptions;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.api.TextCorpus;
import fr.univnantes.termsuite.framework.PreprocessingPipelineBuilder;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.uima.PipelineListener;
import fr.univnantes.termsuite.uima.PreparationPipelineException;
import fr.univnantes.termsuite.uima.PreparationPipelineOptions;
import fr.univnantes.termsuite.uima.readers.TermSuiteJsonCasSerializer;
import fr.univnantes.termsuite.utils.JCasUtils;
import fr.univnantes.termsuite.utils.TermHistory;

public class PreprocessorServiceImpl implements PreprocessorService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreprocessorServiceImpl.class);
	
	@Inject
	CorpusService corpusService;

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#prepare(fr.univnantes.termsuite.api.TextCorpus, java.nio.file.Path, fr.univnantes.termsuite.uima.PreparationPipelineOptions, java.util.Optional, java.util.Optional, java.util.Optional, org.apache.uima.analysis_engine.AnalysisEngineDescription)
	 */
	@Override
	public Stream<JCas> prepare(TextCorpus textCorpus, 
			Path taggerPath, PreparationPipelineOptions options, 
			Optional<ResourceOptions> resourceOpts, 
			Optional<TermHistory> termHistory, 
			Optional<PipelineListener> listener, AnalysisEngineDescription... customAEs) {
		
		CorpusMetadata corpusMetadata;
		try {
			corpusMetadata = corpusService.computeMetadata(textCorpus);
		} catch (IOException e1) {
			throw new TermSuiteException(e1);
		}
		
		PreprocessingPipelineBuilder builder = PreprocessingPipelineBuilder
				.create(textCorpus.getLang(), taggerPath)
				.setNbDocuments(corpusMetadata.getNbDocuments())
				.setCorpusSize(corpusMetadata.getTotalSize());
	
		if(listener.isPresent())
			builder.addPipelineListener(listener.get());
		
		for(AnalysisEngineDescription customAE:customAEs) 
			builder.addCustomAE(customAE);
		
		if(resourceOpts.isPresent()) {
			if(resourceOpts.get().getResourceDirectory().isPresent())
				builder.setResourceDir(resourceOpts.get().getResourceDirectory().get().toString());
	
			else if(resourceOpts.get().getResourceJar().isPresent())
				builder.setResourceJar(resourceOpts.get().getResourceJar().get().toString());
		}

		if(termHistory.isPresent())
				builder.setHistory(termHistory.get());
		
		ThreadLocal<AnalysisEngine> analysisEngine = ThreadLocal.withInitial(()-> {
			try {
				LOGGER.info("Initializing analysis engine for thread {}", Thread.currentThread().getName());
				ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();
		    	AnalysisEngineDescription aaeDesc;
				aaeDesc = createEngineDescription(builder.create());
				// Instantiate AAE
				final AnalysisEngine aae = UIMAFramework.produceAnalysisEngine(aaeDesc, resMgr, null);
				return aae;
			} catch (ResourceInitializationException e) {
				throw new PreparationPipelineException(e);
			}
		});

		return corpusService
				.documents(textCorpus)
				.map(document -> {
					try {
						JCas cas = createCas(document, corpusMetadata);
						analysisEngine.get().process(cas);
						return cas;
					} catch (UIMAException | IOException e) {
						throw new TermSuiteException(e);
					}
				});
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#createCas(fr.univnantes.termsuite.model.Document, fr.univnantes.termsuite.api.CorpusMetadata)
	 */
	@Override
	public JCas createCas(Document document, CorpusMetadata corpusMetadata) throws UIMAException, IOException {
		String documentText = corpusService.readDocumentText(document, corpusMetadata.getEncoding());
		JCas cas = JCasFactory.createJCas();
		cas.setDocumentLanguage(document.getLang().getCode());
		cas.setDocumentText(documentText);
		SourceDocumentInformation sdi = JCasUtils.initJCasSDI(
				cas, 
				document.getLang().getCode(), 
				documentText, 
				document.getUrl());
		sdi.setCorpusSize(corpusMetadata.getTotalSize());
		sdi.setNbDocuments(corpusMetadata.getNbDocuments());
		return cas;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#toXMIPath(java.nio.file.Path, org.apache.uima.jcas.JCas)
	 */
	@Override
	public void toXMIPath(Path filePath, JCas cas) {
		try {
			XmiCasSerializer.serialize(cas.getCas(), new FileOutputStream(makeParentDirs(filePath).toFile()));
		} catch (FileNotFoundException | SAXException e) {
			throw new TermSuiteException(e);
		}
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#toJsonPath(java.nio.file.Path, org.apache.uima.jcas.JCas)
	 */
	@Override
	public void toJsonPath(Path filePath, JCas cas) {
		try {
			TermSuiteJsonCasSerializer.serialize(new FileWriter(makeParentDirs(filePath).toFile()), cas);
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}

	private Path makeParentDirs(Path path) {
		path.getParent().toFile().mkdirs();
		return path;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#consumeToTargetJsonCorpus(org.apache.uima.jcas.JCas, fr.univnantes.termsuite.api.TextCorpus, fr.univnantes.termsuite.api.PreparedCorpus)
	 */
	@Override
	public void consumeToTargetJsonCorpus(JCas cas, TextCorpus textCorpus, PreparedCorpus targetCorpus) {
		String sourceDocumentUri = JCasUtils.getSourceDocumentAnnotation(cas).get().getUri();
		Path targetDocumentPath = corpusService.getTargetDocumentPath(targetCorpus, textCorpus, Paths.get(sourceDocumentUri));
		toJsonPath(targetDocumentPath, cas);
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#consumeToTargetXMICorpus(org.apache.uima.jcas.JCas, fr.univnantes.termsuite.api.TextCorpus, fr.univnantes.termsuite.api.PreparedCorpus)
	 */
	@Override
	public void consumeToTargetXMICorpus(JCas cas, TextCorpus textCorpus, PreparedCorpus targetCorpus) {
		String sourceDocumentUri = JCasUtils.getSourceDocumentAnnotation(cas).get().getUri();
		Path targetDocumentPath = corpusService.getTargetDocumentPath(targetCorpus, textCorpus, Paths.get(sourceDocumentUri));
		toXMIPath(targetDocumentPath, cas);
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#consumeToTerminology(java.util.stream.Stream, fr.univnantes.termsuite.model.Terminology, int)
	 */
	@Override
	public void consumeToTerminology(Stream<JCas> cases, Terminology terminology, int maxSize) {
		final TermOccAnnotationImporter importer = new TermOccAnnotationImporter(maxSize, terminology);
		cases.forEach(cas -> importer.importCas(cas));
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.termsuite.framework.service.PreprocessorService#generateTerminologyName(fr.univnantes.termsuite.api.TextCorpus)
	 */
	@Override
	public String generateTerminologyName(TextCorpus textCorpus) {
		return String.format("%s-%s-%s", 
				textCorpus.getRootDirectory().getFileName(),
				textCorpus.getLang(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now()));
	}
}
