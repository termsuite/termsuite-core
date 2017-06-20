package fr.univnantes.termsuite.framework.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.TXTCorpus;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.api.XMICorpus;
import fr.univnantes.termsuite.engines.prepare.TerminologyPreparator;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ImporterModule;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.projection.DocumentProjection;
import fr.univnantes.termsuite.projection.DocumentProjectionService;
import fr.univnantes.termsuite.uima.readers.JsonCasSerializer;
import fr.univnantes.termsuite.uima.readers.TSVCasSerializer;
import fr.univnantes.termsuite.utils.JCasUtils;

public class PreprocessorService {

	private static final Logger logger = LoggerFactory.getLogger(PreprocessorService.class);

	@Inject
	private CorpusService corpusService;
	 
	@Inject
	private AnalysisEngine engine;

	@Inject
	private Lang uimaPipelineLang;
	
	public Stream<JCas> prepare(TXTCorpus textCorpus) {
		checkLang(textCorpus);
		Stream<Document> documents = textCorpus.documents();
		return prepare(documents, textCorpus::readDocumentText);
	}

	public void checkLang(TXTCorpus textCorpus) {
		Preconditions.checkArgument(this.uimaPipelineLang == textCorpus.getLang(), 
				"Preprocessor lang is %s while corpus lang is %s", 
				this.uimaPipelineLang, 
				textCorpus.getLang());
	}

	public JCas prepare(Document document, String documentText) {
		try {
			JCas cas = createCas(document, documentText);
			return prepare(cas);
		} catch (UIMAException e) {
			throw new TermSuiteException(e);
		}
	}

	public JCas prepare(JCas blankCas) {
		try {
			engine.process(blankCas);
			return blankCas;
		} catch (AnalysisEngineProcessException e) {
			throw new TermSuiteException(e);
		}
	}

	public Stream<JCas> prepare(Stream<Document> documents, Function<Document, String> textReader) {
		return documents.map(doc -> prepare(doc, textReader.apply(doc)));
	}

	public JCas createCas(Document document, String documentText) throws UIMAException {
		JCas cas = JCasFactory.createJCas();
		cas.setDocumentLanguage(document.getLang().getCode());
		cas.setDocumentText(documentText);
		JCasUtils.initJCasSDI(
				cas, 
				document.getLang().getCode(), 
				documentText, 
				document.getUrl(),
				document.getSize());
		return cas;
	}

	public JCas toTSVCas(JCas cas, Path filePath) {
		try {
			logger.debug("Exporting CAS to {}", filePath);
			TSVCasSerializer.serialize(cas, new FileWriter(makeParentDirs(filePath).toFile()));
			logger.debug("TSV CAS export succeeded");
			return cas;
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}

	public JCas toXMICas(JCas cas, Path filePath) {
		try {
			logger.debug("Exporting CAS to {}", filePath);
			XmiCasSerializer.serialize(cas.getCas(), new FileOutputStream(makeParentDirs(filePath).toFile()));
			logger.debug("XMI CAS export succeeded");
			return cas;
		} catch (FileNotFoundException | SAXException e) {
			throw new TermSuiteException(e);
		}
	}

	public JCas toJSONCas(JCas cas, Path filePath) {
		try {
			logger.debug("Exporting CAS to {}", filePath);
			JsonCasSerializer.serialize(new FileWriter(makeParentDirs(filePath).toFile()), cas);
			logger.debug("JSON CAS export succeeded");
			return cas;
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}

	private Path makeParentDirs(Path path) {
		path.getParent().toFile().mkdirs();
		return path;
	}

	public void consumeToTargetJsonCorpus(JCas cas, TXTCorpus textCorpus, XMICorpus targetCorpus) {
		String sourceDocumentUri = JCasUtils.getSourceDocumentAnnotation(cas).get().getUri();
		Path targetDocumentPath = corpusService.getTargetDocumentPath(targetCorpus, textCorpus, Paths.get(sourceDocumentUri));
		toJSONCas(cas, targetDocumentPath);
	}

	public void consumeToTargetXMICorpus(JCas cas, TXTCorpus textCorpus, XMICorpus targetCorpus) {
		String sourceDocumentUri = JCasUtils.getSourceDocumentAnnotation(cas).get().getUri();
		Path targetDocumentPath = corpusService.getTargetDocumentPath(targetCorpus, textCorpus, Paths.get(sourceDocumentUri));
		toXMICas(cas, targetDocumentPath);
	}

	public String generateTerminologyName(TXTCorpus textCorpus) {
		return String.format("%s-%s-%s", 
				textCorpus.getRootDirectory().getFileName(),
				textCorpus.getLang(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now()));
	}
	
	public DocumentProjectionService toProjectionService(Document document, String documentText) {
		String name = "document-" + document.getUrl();
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(uimaPipelineLang, name);
		Injector injector = Guice.createInjector(new ImporterModule(indexedCorpus, Integer.MAX_VALUE));
		ImporterService importer = injector.getInstance(ImporterService.class);
		
		/*
		 * Prepare document
		 */
		JCas prepare = prepare(document,documentText);
		importer.importCas(prepare);
		DocumentProjection projection = new DocumentProjection(indexedCorpus.getTerminology().getTerms().values());
		DocumentProjectionService documentProjectionService = new DocumentProjectionService(projection);
		
		/*
		 * Compute basic term properties
		 */
		TermSuiteFactory.createPipeline(TerminologyPreparator.class, indexedCorpus).run();

		return documentProjectionService;
	}
}
