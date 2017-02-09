package fr.univnantes.termsuite.framework.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.PreprocessedCorpus;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.prepare.TerminologyPreparator;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ImportModule;
import fr.univnantes.termsuite.model.CorpusMetadata;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TextCorpus;
import fr.univnantes.termsuite.projection.DocumentProjection;
import fr.univnantes.termsuite.projection.DocumentProjectionService;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.uima.readers.TermSuiteJsonCasSerializer;
import fr.univnantes.termsuite.utils.JCasUtils;

public class PreprocessorService {

	@Inject
	private CorpusService corpusService;
	 
	@Inject
	private AnalysisEngine engine;

	@Inject
	private Lang uimaPipelineLang;
	
	public Stream<JCas> prepare(TextCorpus textCorpus) {
		checkLang(textCorpus);
		Stream<Document> documents = corpusService.documents(textCorpus);
		
		CorpusMetadata corpusMetadata;
		try {
			corpusMetadata = corpusService.computeMetadata(textCorpus);
		} catch (IOException e1) {
			throw new TermSuiteException(e1);
		}
		return prepare(documents, corpusMetadata);
	}

	public void checkLang(TextCorpus textCorpus) {
		Preconditions.checkArgument(this.uimaPipelineLang == textCorpus.getLang(), 
				"Preprocessor lang is %s while corpus lang is %s", 
				this.uimaPipelineLang, 
				textCorpus.getLang());
	}
	
	public JCas prepare(Document document) {
		try {
			JCas cas = createCas(document);
			engine.process(cas);
			return cas;
		} catch (UIMAException | IOException e) {
			throw new TermSuiteException(e);
		}
	}

	public Stream<JCas> prepare(Stream<Document> documents) {
		return documents.map(this::prepare);
	}

	public Stream<JCas> prepare(Stream<Document> documents, CorpusMetadata corpusMetadata) {
		return documents
				.map(document -> {
					try {
						JCas cas = createCas(document, corpusMetadata);
						engine.process(cas);
						return cas;
					} catch (UIMAException | IOException e) {
						throw new TermSuiteException(e);
					}
				});
	}

	public JCas createCas(Document document) throws UIMAException, IOException {
		return createCas(document, new CorpusMetadata(Charset.defaultCharset()));
	}
	
	public JCas createCas(Document document, CorpusMetadata corpusMetadata) throws UIMAException, IOException {
		JCas cas = createCas(
				corpusService.readDocumentText(document, corpusMetadata.getEncoding()), 
				document.getLang(), 
				document.getUrl());
		SourceDocumentInformation sdi = JCasUtils.getSourceDocumentAnnotation(cas).get();
		sdi.setCorpusSize(corpusMetadata.getTotalSize());
		sdi.setNbDocuments(corpusMetadata.getNbDocuments());
		return cas;
	}

	public JCas createCas(String documentText, Lang lang, String url) throws UIMAException {
		JCas cas = JCasFactory.createJCas();
		cas.setDocumentLanguage(lang.getCode());
		cas.setDocumentText(documentText);
		JCasUtils.initJCasSDI(
				cas, 
				lang.getCode(), 
				documentText, 
				url);
		return cas;
	}

	private void toXMIPath(Path filePath, JCas cas) {
		try {
			XmiCasSerializer.serialize(cas.getCas(), new FileOutputStream(makeParentDirs(filePath).toFile()));
		} catch (FileNotFoundException | SAXException e) {
			throw new TermSuiteException(e);
		}
	}

	private void toJsonPath(Path filePath, JCas cas) {
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

	public void consumeToTargetJsonCorpus(JCas cas, TextCorpus textCorpus, PreprocessedCorpus targetCorpus) {
		String sourceDocumentUri = JCasUtils.getSourceDocumentAnnotation(cas).get().getUri();
		Path targetDocumentPath = corpusService.getTargetDocumentPath(targetCorpus, textCorpus, Paths.get(sourceDocumentUri));
		toJsonPath(targetDocumentPath, cas);
	}

	public void consumeToTargetXMICorpus(JCas cas, TextCorpus textCorpus, PreprocessedCorpus targetCorpus) {
		String sourceDocumentUri = JCasUtils.getSourceDocumentAnnotation(cas).get().getUri();
		Path targetDocumentPath = corpusService.getTargetDocumentPath(targetCorpus, textCorpus, Paths.get(sourceDocumentUri));
		toXMIPath(targetDocumentPath, cas);
	}

	public String generateTerminologyName(TextCorpus textCorpus) {
		return String.format("%s-%s-%s", 
				textCorpus.getRootDirectory().getFileName(),
				textCorpus.getLang(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now()));
	}
	
	public DocumentProjectionService toProjectionService(Document document) {
		String name = "document-" + document.getUrl();
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(uimaPipelineLang, name);
		Injector injector = Guice.createInjector(new ImportModule(indexedCorpus, Integer.MAX_VALUE));
		TermOccAnnotationImporter importer = injector.getInstance(TermOccAnnotationImporter.class);
		
		/*
		 * Prepare document
		 */
		JCas prepare = prepare(document);
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
