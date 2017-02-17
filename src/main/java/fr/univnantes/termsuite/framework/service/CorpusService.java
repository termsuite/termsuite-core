package fr.univnantes.termsuite.framework.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.api.PreprocessedCorpus;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Corpus;
import fr.univnantes.termsuite.model.CorpusMetadata;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.TextCorpus;
import fr.univnantes.termsuite.uima.readers.JsonCasDeserializer;
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

	public String cleanRawText(String rawText) {
		StringPreparator stringPreparator = new StringPreparator();
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
	
	/**
	 * 
	 * Reads the {@link TextCorpus} from file system as a {@link Document} stream.
	 * 
	 * @param corpus
	 * 			The input {@link TextCorpus} to read from file system
	 * @return
	 * 			The {@link Document} stream
	 */
	public Stream<Document> documents(TextCorpus corpus) {
		return pathWalker(
				corpus.getRootDirectory(), 
				corpus.getPattern(), 
				path -> new Document(corpus.getLang(),  path.toUri().getPath()));
	}
	
	
	/**
	 * 
	 * Reads a {@link PreprocessedCorpus} from file system 
	 * as a {@link JCas} stream.
	 * 
	 * @param corpus
	 * 			The {@link PreprocessedCorpus}
	 * @return
	 * 			The {@link JCas} stream
	 */
	public Stream<JCas> cases(PreprocessedCorpus corpus) {
		return pathWalker(
				corpus.getRootDirectory(), 
				corpus.getPattern(), 
				path -> {
					try {
						JCas jCas = JCasFactory.createJCas();
						CAS cas = jCas.getCas();
						if(corpus.getExtension().equals(PreprocessedCorpus.XMI_EXTENSION)) {
							XmiCasDeserializer.deserialize(new FileInputStream(path.toFile()), cas);
						} else if(corpus.getExtension().equals(PreprocessedCorpus.JSON_EXTENSION)) {
							JsonCasDeserializer.deserialize(new FileInputStream(path.toFile()), cas);
						} else
							throw new IllegalArgumentException("Expected a XMI or JSON " + PreprocessedCorpus.class.getSimpleName());
						return jCas;
					} catch (Exception e) {
						throw new TermSuiteException(e);
					}
				});	
		
	}


}
