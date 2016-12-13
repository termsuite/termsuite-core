package fr.univnantes.termsuite.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;

public class FileSystemUtils {
	
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
	

	public static long pathDocumentCount(String directory, String pattern) {
		String glob = String.format("glob:%s", pattern);
		
		Path directoryPath = check(directory);
		
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
				glob);
		
		try {
			return Files.walk(directoryPath).filter(path -> 
				pathMatcher.matches(path) && path.toFile().isFile()
			).collect(Collectors.counting());
			
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}		
	}


	private static Path check(String directory) {
		Path directoryPath = Paths.get(directory);
		Preconditions.checkArgument(directoryPath.toFile().exists(),
				"Directory %s does not exist", directory);
		Preconditions.checkArgument(directoryPath.toFile().isDirectory(),
				"Not a directory: %s", directory);
		return directoryPath;
	}
			
	public static <T> Stream<T> pathWalker(String directory, String pattern,
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
}
