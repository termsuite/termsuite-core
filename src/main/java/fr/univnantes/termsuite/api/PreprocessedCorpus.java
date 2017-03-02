package fr.univnantes.termsuite.api;

import java.util.stream.Stream;

import org.apache.uima.jcas.JCas;

import fr.univnantes.termsuite.model.Lang;

public interface PreprocessedCorpus {
	Stream<JCas> cases();
	Lang getLang();
}
