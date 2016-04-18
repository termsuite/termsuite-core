package eu.project.ttc.readers;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.types.SourceDocumentInformation;

public class StreamingCollectionReader extends CollectionReader_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(StreamingCollectionReader.class);

	public static final String PARAM_LANGUAGE = "CorpusLanguage";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory=true)
	private Lang mLanguage;

	public static final String PARAM_NAME = "StreamName";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory=true)
	private String streamName;

	private int mCurrentIndex;
	private int cumulatedLength;
	
	public static final String DEFAULT_QUEUE_NAME = "queue.default";
	
	public static final String PARAM_QUEUE_NAME = "QueueName";
	@ConfigurationParameter(name = PARAM_QUEUE_NAME, mandatory=false, defaultValue = DEFAULT_QUEUE_NAME)
	private String queueName;

	private BlockingQueue<CollectionDocument> documentQueue;
	
	private CollectionDocument currentDoc;
	
	@Override
	public void initialize() throws ResourceInitializationException {
		this.queueName = (String) getConfigParameterValue(PARAM_QUEUE_NAME);
		this.mLanguage = Lang.forName((String) getConfigParameterValue(PARAM_LANGUAGE));
		this.streamName = (String) getConfigParameterValue(PARAM_NAME);
		documentQueue = QueueRegistry.getInstance().getQueue(queueName);
		this.mCurrentIndex = 0;
		this.cumulatedLength = 0;
	}

		
	@Override
	public void getNext(CAS cas) throws IOException, CollectionException {
		this.cumulatedLength += currentDoc.getText().length();
		logger.info("[Stream {}] Processing document {}: {} (total length processed: {})", 
				this.streamName,
				this.mCurrentIndex,
				this.currentDoc.getUri(),
				this.cumulatedLength);

		SourceDocumentInformation sdi;
		try {
			
			sdi = new SourceDocumentInformation(cas.getJCas());
			sdi.setUri(currentDoc.getUri());
			cas.setDocumentLanguage(mLanguage.getCode());
			cas.setDocumentText(currentDoc.getText());
			sdi.setDocumentSize(currentDoc.getText().length());
			sdi.setCumulatedDocumentSize(this.cumulatedLength);
			sdi.setBegin(0);
			sdi.setEnd(currentDoc.getText().length());
			sdi.setOffsetInSource(0);
			sdi.setDocumentIndex(mCurrentIndex);
			
			/*
			 * Cannot be known in case of streaming
			 */
			sdi.setCorpusSize(-1);
			sdi.setNbDocuments(-1);
			
			// Cannot know if this is the last
			sdi.setLastSegment(false);
			
			sdi.addToIndexes();
			this.mCurrentIndex++;
		} catch (CASException e) {
			throw new CollectionException(e);
		}
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		try {
			if(documentQueue.isEmpty())
				logger.info("Waiting for a new document.");
			currentDoc = documentQueue.take();
			if(currentDoc == CollectionDocument.LAST_DOCUMENT)
				return false;
			else
				return true;
		} catch (InterruptedException e) {
			logger.info("Stream {} interrupted", this.streamName);
			return false;
		}
	}

	@Override
	public Progress[] getProgress() {
		return null;
	}

	@Override
	public void close() throws IOException {
	}
}
