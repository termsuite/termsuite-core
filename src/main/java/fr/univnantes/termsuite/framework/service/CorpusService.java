package fr.univnantes.termsuite.framework.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import fr.univnantes.termsuite.api.TextualCorpus;
import fr.univnantes.termsuite.model.CorpusMetadata;
import fr.univnantes.termsuite.model.FileSystemCorpus;

public class CorpusService {

	public Path getTargetDocumentPath(FileSystemCorpus targetCorpus, 
			FileSystemCorpus sourceCorpus, 
			Path sourceDocumentPath) {
		
		// 1. relativize source
		String srcRelative = sourceCorpus.getRootDirectory().relativize(sourceDocumentPath).toString();
		
		// 2. to target relative path
		String targetRelative = srcRelative.replaceAll("\\."+sourceCorpus.getExtension()+"$", "." + targetCorpus.getExtension());
		
		// 3. resolve target path
		Path targetPath = targetCorpus.getRootDirectory().resolve(targetRelative);

		return targetPath;
	}


	public CorpusMetadata computeMetadata(TextualCorpus corpus) throws IOException {
		
		AtomicLong corpusSize = new AtomicLong(0);
		AtomicLong documentCount = new AtomicLong(0);
		
		corpus.documents().forEach(doc -> {
			corpusSize.addAndGet(corpus.readDocumentText(doc).length());
			documentCount.incrementAndGet();
		});
			
		return new CorpusMetadata(documentCount.intValue(), corpusSize.longValue());

	}
}
