package fr.univnantes.termsuite.api;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import fr.univnantes.termsuite.model.FileSystemCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.uima.readers.JsonCasDeserializer;

public class XMICorpus extends FileSystemCorpus implements PreprocessedCorpus {
	
	public static final String XMI_PATTERN = "**/*.xmi";
	public static final String XMI_EXTENSION = "xmi";
	public static final String JSON_PATTERN = "**/*.json";
	public static final String JSON_EXTENSION = "json";

	public XMICorpus(Lang lang, Path rootDirectory, String pattern, String extension) {
		super(lang, rootDirectory, pattern, extension);
	}

	@Override
	public Stream<JCas> cases() {
		return pathWalker(
				getRootDirectory(), 
				getPattern(), 
				path -> {
					try {
						JCas jCas = JCasFactory.createJCas();
						CAS cas = jCas.getCas();
						if(getExtension().equals(XMICorpus.XMI_EXTENSION)) {
							XmiCasDeserializer.deserialize(new FileInputStream(path.toFile()), cas);
						} else if(getExtension().equals(XMICorpus.JSON_EXTENSION)) {
							JsonCasDeserializer.deserialize(new FileInputStream(path.toFile()), cas);
						} else
							throw new IllegalArgumentException("Expected a XMI or JSON " + XMICorpus.class.getSimpleName());
						return jCas;
					} catch (Exception e) {
						throw new TermSuiteException(e);
					}
				});	
		
	}

}
