package fr.univnantes.termsuite.model;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.api.TermSuiteException;

public abstract class FileSystemCorpus {

	private Lang lang;
	private Path rootDirectory;
	private Charset encoding = Charsets.UTF_8;
	private String pattern;
	private String extension;

	public FileSystemCorpus(Lang lang, Path rootDirectory, String pattern, String extension) {
		super();
		this.rootDirectory = rootDirectory.toAbsolutePath();
		this.lang = lang;
		this.pattern = pattern;
		this.extension = extension;
	}

	public Lang getLang() {
		return lang;
	}

	public void setLang(Lang lang) {
		this.lang = lang;
	}

	public Path getRootDirectory() {
		return rootDirectory;
	}

	public void setRootDirectory(Path rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getExtension() {
		return this.extension;
	}
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	private  Path check(Path directoryPath) {
		Preconditions.checkArgument(directoryPath.toFile().exists(),
				"Directory %s does not exist", directoryPath);
		Preconditions.checkArgument(directoryPath.toFile().isDirectory(),
				"Not a directory: %s", directoryPath);
		return directoryPath;
	}
	
	protected <T> Stream<T> pathWalker(Path directory, String pattern,
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

	public String readFileContent(Document doc) {
		try {
			return com.google.common.io.Files.toString(Paths.get(doc.getUrl()).toFile(), encoding);
		} catch (IOException e) {
			throw new TermSuiteException(
					"Could not read file content for document " + doc.getUrl(), 
					e);
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", this.getClass().getSimpleName(), this.rootDirectory);
	}
}
