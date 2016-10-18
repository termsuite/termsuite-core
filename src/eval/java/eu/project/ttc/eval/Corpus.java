package eu.project.ttc.eval;

import java.nio.file.Path;
import java.nio.file.Paths;

import eu.project.ttc.engines.desc.Lang;

public enum Corpus{
	WIND_ENERGY("we","wind-energy"),
	MOBILE_TECHNOLOGY("mobile","mobile-technilogy");
	
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


	public Path getRef(TermType termType, Lang source, Lang target) {
		String type = termType.toString().toLowerCase();
		return Paths.get("src", "eval", "resources", "refs", 
				this.fullName,
				type,
				String.format("%s-%s-%s-%s.tsv", this.shortName, type, source.getCode(), target.getCode())
				);
	}
	
	public Path getMWTRef(Lang source, Lang target) {
		return getRef(TermType.MWT, source, target);
	}
	
	public Path getSWTRef(Lang source, Lang target) {
		return getRef(TermType.SWT, source, target);		
	}

	public boolean hasRef(TermType termType, Lang source, Lang target) {
		return getRef(termType, source, target).toFile().exists();
	}

	public boolean hasSWTRef(Lang source, Lang target) {
		return hasRef(TermType.SWT, source, target);
	}

	public boolean hasMWTRef(Lang source, Lang target) {
		return hasRef(TermType.MWT, source, target);
	}
	
	public Path getRootDir() {
		return Paths.get("src", "test","resources","eu","project","ttc","test","corpus",shortName);
	}
	
	public Path getTxtDir(Lang lang) {
		return getRootDir().resolve(lang.getName()).resolve("txt");
	}
}