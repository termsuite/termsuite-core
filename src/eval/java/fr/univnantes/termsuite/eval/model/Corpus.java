package fr.univnantes.termsuite.eval.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import fr.univnantes.termsuite.eval.TermSuiteEvals;
import fr.univnantes.termsuite.model.Lang;

public enum Corpus{
//	BREAST_CANCER("bc","breast-cancer", false),
	WIND_ENERGY("we","wind-energy", true),
	MOBILE_TECHNOLOGY("mb","mobile-tech", true),
	;
	
	private String shortName;
	private String fullName;
	private boolean embedded;
	
	private Corpus(String shortName, String fullName, boolean embbeded) {
		this.shortName = shortName;
		this.fullName = fullName;
		this.embedded = embbeded;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getFullName() {
		return fullName;
	}

	public Path getRootDir() {
		return Paths.get("src", "test","resources","fr","univnantes","termsuite","test","corpus",shortName);
	}

	public Path getTxtDir(Lang lang) {
		if(isEmbedded())
			return getTxtDir(lang);
		else {
			String corpusRoot = TermSuiteEvals.getCheckedProperty("corpus." + fullName).toString();
			return Paths.get(corpusRoot).resolve(lang.getName()).resolve("txt");
		}
	}

	@Override
	public String toString() {
		return shortName;
	}
	
	public boolean isEmbedded() {
		return embedded;
	}
	
	public static Stream<Corpus> all() {
		return Arrays.stream(values());
	}

	public static Stream<Corpus> embeddedCorpora() {
		return all().filter(c -> c.isEmbedded());
	}
		
}