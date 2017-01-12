package fr.univnantes.termsuite.api;

import java.nio.charset.Charset;
import java.nio.file.Path;

import com.google.common.base.Charsets;

import fr.univnantes.termsuite.model.Lang;

public abstract class Corpus {

	private Lang lang;
	private Path rootDirectory;
	private Charset encoding = Charsets.UTF_8;
	private String pattern;
	private String extension;

	public Corpus(Lang lang, Path rootDirectory, String pattern, String extension) {
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
}
