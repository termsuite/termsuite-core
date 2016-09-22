package eu.project.ttc.tools.builders;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.tools.builders.internal.FileSystemHelper;
import eu.project.ttc.utils.JCasUtils;

public class TermSuitePreprocessor {
	
	private Lang lang;

	private Stream<Document> documentStream = null;
	
	private String treeTaggerHome = null;

	
	public static TermSuitePreprocessor fromTextString(Lang lang, String text) {
		return fromSingleDocument(lang, new Document(lang, "file://inline.text", text));
	}

	public static TermSuitePreprocessor fromSingleDocument(Lang lang, Document document) {
		return fromDocumentCollection(lang, Lists.newArrayList(document));
	}

	public static TermSuitePreprocessor fromDocumentStream(Lang lang, Stream<Document> documentStream) {
		TermSuitePreprocessor extractor = new TermSuitePreprocessor();
		extractor.documentStream = documentStream;
		extractor.lang = lang;
		return extractor;
	}
	
	public static TermSuitePreprocessor fromDocumentCollection(Lang lang, Collection<Document> documents) {
		return fromDocumentStream(lang, documents.stream());
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

	public static TermSuitePreprocessor fromTxtCorpus(Lang lang, String directory, String pattern, String encoding) {
		return fromDocumentStream(
				lang, 
				FileSystemHelper.pathWalker(
						directory, 
						pattern, 
						FileSystemHelper.pathToDocumentMapper(lang, encoding)));
	}

	private TermSuitePreprocessor() {}
	
	public TermSuitePreprocessor setTreeTaggerHome(String treeTaggerHome) {
		this.treeTaggerHome = treeTaggerHome;
		return this;
	}
	
	public Stream<JCas> stream() {
		Preconditions.checkState(treeTaggerHome != null, "TreeTagger home is null. Please use #setTreeTaggerHome()");

		TermSuitePipeline pipeline = TermSuitePipeline.create(lang.getCode())
				.aeWordTokenizer()
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
					return cas;
				} catch (UIMAException e) {
					throw new CorpusException(e);
				}
			});
			
		} catch (ResourceInitializationException e1) {
			throw new CorpusException(e1);
		}

	}
	
	
	
}
