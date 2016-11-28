package fr.univnantes.termsuite.uima.readers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.utils.IstexUtils;

/**
 * 
 * Reads collections from a ISTEX
 * 
 * @author Damien Cram
 *
 */
public class IstexCollectionReader extends CollectionReader_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(IstexCollectionReader.class);

	public static final String PARAM_LANGUAGE = "CorpusLanguage";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
	private Lang lang;

	public static final String PARAM_API_URL = "ApiURL";
	@ConfigurationParameter(name = PARAM_API_URL, mandatory = true)
	private String urlStr;
	private URI apiURI;

	public static final String PARAM_IGNORE_LANGUAGE_ERRORS = "ignoreLanguageErrors";
	@ConfigurationParameter(name = PARAM_IGNORE_LANGUAGE_ERRORS, mandatory = false, defaultValue = "false")
	private boolean ignoreLanguageErrors;

	public static final String PARAM_ID_LIST = "idList";
	@ConfigurationParameter(name = PARAM_API_URL, mandatory = true)
	private String idListStr;
	private List<String> idList;
	private List<URL> urlList;

	private int currentIndex = 0;
	private int cumulatedSize = 0;
	private long lastTop;
	
	private CompletionService<String> pool;
	private ExecutorService threadPool;

	@Override
	public void initialize() throws ResourceInitializationException {
		logger.debug("Initializing Istex collection reader");
		this.lang = Lang.forName((String) getConfigParameterValue(PARAM_LANGUAGE));
		this.urlStr = (String) getConfigParameterValue(PARAM_API_URL);
		this.ignoreLanguageErrors = (Boolean) getConfigParameterValue(PARAM_IGNORE_LANGUAGE_ERRORS);
		this.idListStr = (String) getConfigParameterValue(PARAM_ID_LIST);
		this.idList = Splitter.on(",").splitToList(this.idListStr);
		try {
			this.apiURI = new URI(urlStr);
		} catch (URISyntaxException e) {
			logger.error("Could not parse Istex API's url: {}", this.urlStr);
			throw new ResourceInitializationException(e);
		}

		threadPool = Executors.newFixedThreadPool(4);
		pool = new ExecutorCompletionService<String>(threadPool);
		urlList = Lists.newArrayList();
		for (String documentId:idList) {
			String documentPath = String.format("/document/%s/", documentId);
			URL documentURL;
			try {
				documentURL = new URL(apiURI.toURL(), documentPath);
				urlList.add(documentURL);
			} catch (MalformedURLException e) {
				throw new ResourceInitializationException(e);
			}
		}

		new Thread() {

			@Override
			public void run() {
				for(URL documentURL:urlList) {
					if(logger.isTraceEnabled())
						logger.trace("Submitting retrieve task for {}", documentURL);
					pool.submit(new GetDocumentTask(documentURL));
				}
			}
		}.start();

		logger.debug("End of Istex collection reader initialization");

	}

	private final class GetDocumentTask implements Callable<String> {

		private URL documentURL;

		public GetDocumentTask(URL documentURL) {
			super();
			this.documentURL = documentURL;
		}

		public String call() {
			InputStream openStream;
			try {
				if(logger.isTraceEnabled())
					logger.trace("Executing retrieve task for  {}", documentURL);
				openStream = documentURL.openStream();
				Scanner scanner = new Scanner(openStream);
				Scanner s = scanner.useDelimiter("\\A");
				String string = s.hasNext() ? s.next() : "";
				scanner.close();
				return string;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void getNext(CAS cas) throws IOException, CollectionException {
		if (currentIndex == 0) {
			logger.debug("Processing the first document of istex collection");
			lastTop = System.currentTimeMillis();
		}
		URL documentUrl = urlList.get(currentIndex++);

		long top = System.currentTimeMillis();
		if (top - lastTop > 5000l) {
			lastTop = top;
			logger.info("{}% - Processing Istex document {} on {}",
					String.format("%3d", (currentIndex * 100) / idList.size()), currentIndex, idList.size());
		}
		
		SourceDocumentInformation sdi;
		try {
			sdi = new SourceDocumentInformation(cas.getJCas());
			sdi.setUri(documentUrl.toString());
			String text = toDocumentText(documentUrl);
			cas.setDocumentText(text);
			cas.setDocumentLanguage(lang.getCode());
			sdi.setDocumentSize(text.length());
			this.cumulatedSize += text.length();
			sdi.setCumulatedDocumentSize(this.cumulatedSize);
			sdi.setCorpusSize(-1);
			sdi.setBegin(0);
			sdi.setEnd(text.length());
			sdi.setOffsetInSource(0);
			sdi.setDocumentIndex(currentIndex);
			sdi.setNbDocuments(this.idList.size());
			boolean lastSegment = currentIndex == idList.size() - 1;
			sdi.setLastSegment(lastSegment);
			sdi.addToIndexes();
			if(lastSegment)
				threadPool.shutdown();
		} catch (CASException e) {
			throw new CollectionException(e);
		}

	}

	@SuppressWarnings("unchecked")
	private String toDocumentText(URL documentURL) throws IOException {
		String json;
		try {
			json = pool.take().get();

			ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
			Map<String, Object> map = mapper.readValue(json, Map.class);
			IstexDocument document = new IstexDocument();
			document.setLanguage(((List<String>) map.get("language")).get(0));
			document.setTitle((String) map.get("title"));
			document.setAbstract((String) map.get("abstract"));
			Lang docLang = IstexUtils.toTermSuiteLang(document.getLanguage());
			if (docLang != this.lang) {
				String msg = String.format("Bad language for document %s. Expected: %s, actual: %s", documentURL.toString(),
						this.lang.getCode(), docLang.getCode());
				if (ignoreLanguageErrors) {
					logger.warn(msg);
					return "";
				} else
					throw new IllegalArgumentException(msg);
			}
	
			return String.format("%s . %s", document.getTitle(), document.getAbstract());

		} catch (Exception e) {
			logger.error("An error occurrence with the Istex client pool");
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return currentIndex < this.idList.size();
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
	}

}
