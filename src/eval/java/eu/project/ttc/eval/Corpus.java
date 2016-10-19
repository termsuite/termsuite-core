package eu.project.ttc.eval;

import java.nio.file.Path;
import java.nio.file.Paths;

import eu.project.ttc.engines.desc.Lang;

public enum Corpus{
	WIND_ENERGY("we","wind-energy"),
	MOBILE_TECHNOLOGY("mb","mobile-tech");
	
	private String shortName;
	private String fullName;
	private Corpus(String shortName, String fullName) {
		this.shortName = shortName;
		this.fullName = fullName;
	}
	public String getShortName() {
		return shortName;
	}
	public String getFullName() {
		return fullName;
	}


	public Path getRootDir() {
		return Paths.get("src", "test","resources","eu","project","ttc","test","corpus",shortName);
	}
	
	public Path getTxtDir(Lang lang) {
		return getRootDir().resolve(lang.getName()).resolve("txt");
	}
	
	@Override
	public String toString() {
		return shortName;
	}
}