package fr.univnantes.termsuite.framework.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.api.Corpus;
import fr.univnantes.termsuite.api.CorpusMetadata;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.api.TextCorpus;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.uima.readers.StringPreparator;
import fr.univnantes.termsuite.utils.FileUtils;

public class CorpusService {

	public Path getTargetDocumentPath(Corpus targetCorpus, 
			Corpus sourceCorpus, 
			Path sourceDocumentPath) {
		
		// 1. relativize source
		String srcRelative = sourceCorpus.getRootDirectory().relativize(sourceDocumentPath).toString();
		
		// 2. to target relative path
		String targetRelative = srcRelative.replaceAll("\\."+sourceCorpus.getExtension()+"$", "." + targetCorpus.getExtension());
		
		// 3. resolve target path
		Path targetPath = targetCorpus.getRootDirectory().resolve(targetRelative);

		return targetPath;
	}

	public String readDocumentText(Document document, Charset encoding) throws IOException {
		String rawString = FileUtils.readFile(document.getUrl(), encoding);
		String prepared = cleanRawText(rawString);
		return prepared;
	}

	StringPreparator stringPreparator = new StringPreparator();
	public String cleanRawText(String rawText) {
		return stringPreparator.prepare(rawText);
	}

	public CorpusMetadata computeMetadata(TextCorpus corpus) throws IOException {
		String glob = String.format("glob:%s", corpus.getPattern());
		
		Path directoryPath = check(corpus.getRootDirectory());
		
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
				glob);
		
		AtomicLong corpusSize = new AtomicLong(0);
		AtomicLong documentCount = new AtomicLong(0);
		Files.walk(directoryPath).filter(path -> 
			pathMatcher.matches(path) && path.toFile().isFile()
		).forEach(path -> {
			documentCount.incrementAndGet();
			corpusSize.addAndGet(path.toFile().length());
		});
		return new CorpusMetadata(corpus.getEncoding(), documentCount.intValue(), corpusSize.longValue());

	}
	
	private  Path check(Path directoryPath) {
		Preconditions.checkArgument(directoryPath.toFile().exists(),
				"Directory %s does not exist", directoryPath);
		Preconditions.checkArgument(directoryPath.toFile().isDirectory(),
				"Not a directory: %s", directoryPath);
		return directoryPath;
	}
	
	private <T> Stream<T> pathWalker(Path directory, String pattern,
			Function<? super Path, T> pathMapper) {
		String glob = String.format("glob:%s", pattern);
		
		Path directoryPath = check(directory);
		
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
				glob);
		
		try {
			return Files.walk(directoryPath).filter(path -> {
				return pathMatcher.matches(path) && path.toFile().isFile();
			}).map(pathMapper);
			
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
	
	public Stream<Document> documents(TextCorpus corpus) {
		return pathWalker(
				corpus.getRootDirectory(), 
				corpus.getPattern(), 
				path -> new Document(corpus.getLang(),  path.toUri().getPath()));
	}
}
