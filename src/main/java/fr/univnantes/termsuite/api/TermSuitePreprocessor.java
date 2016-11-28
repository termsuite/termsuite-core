package fr.univnantes.termsuite.api;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.uima.TermSuitePipeline;
import fr.univnantes.termsuite.uima.readers.TermSuiteJsonCasSerializer;
import fr.univnantes.termsuite.utils.FileSystemUtils;
import fr.univnantes.termsuite.utils.FileUtils;
import fr.univnantes.termsuite.utils.JCasUtils;

public class TermSuitePreprocessor {
	
	public static enum OutputFormat{JSON,XMI}
	
	private Lang lang;

	private Stream<Document> documentStream = null;
	
	private String inputDirectory = "/";

	private String treeTaggerHome = null;

	private String outputEncoding = Charset.defaultCharset().name();
	
	private Optional<String> outputDirectory = Optional.empty();
	private OutputFormat outputFormat = OutputFormat.JSON;
	
	private long nbDocuments = -1;
	
	public static TermSuitePreprocessor fromTextString(Lang lang, String text) {
		return fromSingleDocument(lang, new Document(lang, "file://inline.text", text));
	}

	public static TermSuitePreprocessor fromSingleDocument(Lang lang, Document document) {
		return fromDocumentCollection(lang, Lists.newArrayList(document));
	}

	public static TermSuitePreprocessor fromDocumentStream(Lang lang, Stream<Document> documentStream, long nbDocuments) {
		TermSuitePreprocessor extractor = new TermSuitePreprocessor();
		extractor.documentStream = documentStream;
		extractor.lang = lang;
		extractor.nbDocuments  = nbDocuments;
		return extractor;
	}
	
	public static TermSuitePreprocessor fromDocumentCollection(Lang lang, Collection<Document> documents) {
		return fromDocumentStream(lang, documents.stream(), documents.size());
	}

	
	public static TermSuitePreprocessor fromTxtCorpus(Lang lang, String directory) {
		return fromTxtCorpus(lang, directory, "**/*.txt", Charset.defaultCharset().name());
	}

	
	/**
	 * 
	 * Example: "**\/*.{txt,data}"
	 *   
	 * 
	 * @param lang
	 * @param directory
	 * @param pattern
	 * @return
	 */
	public static TermSuitePreprocessor fromTxtCorpus(Lang lang, String directory, String pattern) {
		return fromTxtCorpus(lang, directory, pattern, Charset.defaultCharset().name());
	}
	
	public TermSuitePreprocessor toJson(String outputDirectory, String encoding) {
		this.outputDirectory = Optional.of(outputDirectory);
		this.outputEncoding = encoding;
		
		return this;
	}


	public static TermSuitePreprocessor fromTxtCorpus(Lang lang, String directory, String pattern, String encoding) {
		TermSuitePreprocessor preprocessor = fromDocumentStream(
				lang, 
				FileSystemUtils.pathWalker(
						directory, 
						pattern, 
						FileSystemUtils.pathToDocumentMapper(lang, encoding)),
				FileSystemUtils.pathDocumentCount(directory, pattern)
			);
		preprocessor.inputDirectory = directory;
		return preprocessor;
	}

	private TermSuitePreprocessor() {}
	
	public TermSuitePreprocessor setTreeTaggerHome(String treeTaggerHome) {
		this.treeTaggerHome = treeTaggerHome;
		return this;
	}
	
	public Stream<JCas> stream() {
		Preconditions.checkState(treeTaggerHome != null, "TreeTagger home is null. Please use #setTreeTaggerHome()");

		TermSuitePipeline pipeline = TermSuitePipeline.create(lang.getCode());
		
		if(nbDocuments != -1)
			pipeline.aeDocumentLogger(nbDocuments);
		
		pipeline.aeWordTokenizer()
				.setTreeTaggerHome(treeTaggerHome)
				.aeTreeTagger()
				.aeStemmer()
				.setAddSpottedAnnoToTermIndex(false)
				.aeRegexSpotter();
		
	    ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();
	    
		try {
			// Create AAE
			AnalysisEngineDescription aaeDesc = createEngineDescription(pipeline.createDescription());

			// Instantiate AAE
			final AnalysisEngine aae = UIMAFramework.produceAnalysisEngine(aaeDesc, resMgr, null);

			return documentStream.map(document -> {
				JCas cas;
				try {
					cas = JCasFactory.createJCas();
					cas.setDocumentLanguage(document.getLang().getCode());
					cas.setDocumentText(document.getText());
					JCasUtils.initJCasSDI(
							cas, 
							document.getLang().getCode(), 
							document.getText(), 
							document.getUrl());
					aae.process(cas);
					
					
					if(outputDirectory.isPresent())
						exportCas(document, cas);
					
					return cas;
				} catch (UIMAException e) {
					throw new TermSuiteException(e);
				}
			});
			
		} catch (ResourceInitializationException e1) {
			throw new TermSuiteException(e1);
		}

	}

	private void exportCas(Document document, JCas cas) {
		String toFilePath;
		try {
			toFilePath = FileUtils.replaceRootDir(
					document.getUrl(), 
					new File(inputDirectory).getCanonicalPath(), 
					outputDirectory.get());
			toFilePath = FileUtils.replaceExtensionWith(
					toFilePath, 
					this.outputFormat.toString().toLowerCase());
			
			new File(toFilePath).getParentFile().mkdirs();
			
			try(Writer writer = new FileWriter(toFilePath)) {
				if(outputFormat == OutputFormat.JSON)
					TermSuiteJsonCasSerializer.serialize(writer, cas);
				if(outputFormat == OutputFormat.XMI)
					XmiCasSerializer.serialize(cas.getCas(), 
							cas.getTypeSystem(), 
							new FileOutputStream(toFilePath));
			} catch (Exception e) {
				throw new TermSuiteException("Could not export cas to " + toFilePath + " for cas " + document.getUrl(),e);
			}
		} catch (IOException e1) {
			throw new TermSuiteException("Could not export cas " + document.getUrl(),e1);
		}
	}

	public void execute() {
		stream().forEach(cas -> {});
	}
}
