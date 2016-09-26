package eu.project.ttc.tools.api.internal;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import eu.project.ttc.api.Document;
import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.engines.desc.Lang;

public class FileSystemHelper {
	
	public static Function<Path, Document> pathToDocumentMapper(Lang lang, String encoding) {
		return path -> {
			try {
				return new Document(
						lang, 
						path.toUri().getPath(), 
						FileUtils.readFileToString(path.toFile(), encoding));
			} catch (IOException e) {
				throw new TermSuiteException("Unable to read file " + path, e);
			}
		};
	}
	
	public static <T> Stream<T> pathWalker(String directory, String pattern,
			Function<? super Path, T> pathMapper) {
		Path directoryPath = Paths.get(directory);
		String glob = String.format("glob:%s", pattern);
		
		Preconditions.checkArgument(directoryPath.toFile().exists(),
				"Directory %s does not exist", directory);
		Preconditions.checkArgument(directoryPath.toFile().isDirectory(),
				"Not a directory: %s", directory);
		
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
}
