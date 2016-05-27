package eu.project.ttc.tools;

import com.google.common.base.Preconditions;

import eu.project.ttc.engines.desc.Lang;

/**
 * 
 * A meta-type for TermSuite linguistic resources.
 * 
 * @author Damien Cram
 *
 */
public enum TermSuiteResource {
	GENERAL_LANGUAGE(PatternType.END, "GeneralLanguage.", "", ""),
	PREFIX_BANK(PatternType.END, "Prefix.", "", ""),
	ROOT_BANK(PatternType.END, "RootBank.", "", ""),
	ALLOWED_CHARS(PatternType.BEGIN, "-allowed-chars.txt", "", ""),
	COMPOST_INFLECTION_RULES(PatternType.BEGIN, "-compost-inflection-rules.txt", "", ""),
	COMPOST_STOP_LIST(PatternType.BEGIN, "-compost-stop-list.txt", "", ""),
	COMPOST_TRANSFORMATION_RULES(PatternType.BEGIN, "-compost-transformation-rules.txt", "", ""),
	DICO(PatternType.BEGIN, "-dico.txt", "", ""),
	FROZEN_EXPRESSIONS(PatternType.BEGIN, "-frozen-expressions.list", "", ""),
	TAGGER_CASE_MAPPING(PatternType.TAGGER, "-[TAGGER]-case-mapping.xml", "", ""),
	TAGGER_CATEGORY_MAPPING(PatternType.TAGGER, "-[TAGGER]-category-mapping.xml", "", ""),
	TAGGER_GENDER_MAPPING(PatternType.TAGGER, "-[TAGGER]-gender-mapping.xml", "", ""),
	TAGGER_MOOD_MAPPING(PatternType.TAGGER, "-[TAGGER]-mood-mapping.xml", "", ""),
	TAGGER_NUMBER_MAPPING(PatternType.TAGGER, "-[TAGGER]-number-mapping.xml", "", ""),
	TAGGER_SUBCATEGORY_MAPPING(PatternType.TAGGER, "-[TAGGER]-subcategory-mapping.xml", "", ""),
	TAGGER_TENSE_MAPPING(PatternType.TAGGER, "-[TAGGER]-tense-mapping.xml", "", ""),
	MWT_RULES(PatternType.BEGIN, "-multi-word-rule-system.regex", "", ""),
	NEOCLASSICAL_PREFIXES(PatternType.BEGIN, "-neoclassical-prefixes.txt", "", ""),
	SEGMENT_BANK(PatternType.BEGIN, "-segment-bank.xml", "", ""),
	STOP_WORDS_FILTER(PatternType.BEGIN, "-stop-word-filter.xml", "", ""),
	TREETAGGER_CONFIG(PatternType.BEGIN, "-treetagger.xml", "", ""),
	VARIANTS(PatternType.BEGIN, "-variants.yaml", "", ""), 
	FIXED_EXPRESSION_REGEXES(PatternType.BEGIN, "-fixed-expressions.txt", "", "");
	
	public static enum PatternType {END, TAGGER, BEGIN}
	
	private String pathPattern;
	private String title;
	private String description;
	private PatternType patternType;

	
	private TermSuiteResource(PatternType patternType, String pathPattern, String title, String description) {
		this.pathPattern = pathPattern;
		this.title = title;
		this.description = description;
		this.patternType = patternType;
	}
	
	public String getPathPattern() {
		return pathPattern;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getTaggerPath(Lang lang, Tagger tagger) {
		Preconditions.checkArgument(patternType == PatternType.TAGGER,
				"Not a tagger resource: %s", this);
		return String.format("%s%s",
				lang.getName().toLowerCase(),
				getPathPattern().replace("[TAGGER]", tagger.getResourceShortName())
			);
	}

	public String getPath(Lang lang) {
		switch(patternType) {
		case BEGIN:
			return String.format("%s%s",
					lang.getName().toLowerCase(),
					getPathPattern()
				);
		case END:
			return String.format("%s%s",
					getPathPattern(),
					lang.getNameUC()
				);
		case TAGGER:
			throw new IllegalArgumentException("Bad method invokation for tagger resource " + this);
		default:
			throw new IllegalArgumentException("Unknown pattern type \""+patternType+"\" for tagger resource " + this);
		}
	} 
	
	
	public static final TermSuiteResource forFileName(Lang l, String fileName) {
		for(TermSuiteResource r:values()) {
			switch(r.patternType) {
			case TAGGER:
				for(Tagger t:Tagger.values()) {
					if(r.getTaggerPath(l, t).equals(fileName))
						return r;
				}
				continue;
			default:
				if(r.getPath(l).equals(fileName))
					return r;
			}
		}
		return null;
	}
	
	
}
