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
	GENERAL_LANGUAGE("[LANG_SHORT]/[LANG]-general-language.txt", "", ""),
	PREFIX_BANK("[LANG_SHORT]/morphology/[LANG]-prefix-bank.txt", "", ""),
	MANUAL_MORPHOLOGY("[LANG_SHORT]/morphology/[LANG]-manual-composition.txt", "", ""),
	ROOT_BANK("[LANG_SHORT]/morphology/[LANG]-root-bank.txt", "", ""),
	ALLOWED_CHARS("[LANG_SHORT]/[LANG]-allowed-chars.txt", "", ""),
	COMPOST_INFLECTION_RULES("[LANG_SHORT]/morphology/[LANG]-compost-inflection-rules.txt", "", ""),
	COMPOST_STOP_LIST("[LANG_SHORT]/morphology/[LANG]-compost-stop-list.txt", "", ""),
	COMPOST_TRANSFORMATION_RULES("[LANG_SHORT]/morphology/[LANG]-compost-transformation-rules.txt", "", ""),
	DICO("[LANG_SHORT]/[LANG]-dico.txt", "", ""),
	FIXED_EXPRESSIONS("[LANG_SHORT]/[LANG]-fixed-expressions.txt", "", ""),
	TAGGER_CASE_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-case-mapping.xml", "", ""),
	TAGGER_CATEGORY_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-category-mapping.xml", "", ""),
	TAGGER_GENDER_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-gender-mapping.xml", "", ""),
	TAGGER_MOOD_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-mood-mapping.xml", "", ""),
	TAGGER_NUMBER_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-number-mapping.xml", "", ""),
	TAGGER_SUBCATEGORY_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-subcategory-mapping.xml", "", ""),
	TAGGER_TENSE_MAPPING("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-[TAGGER_SHORT]-tense-mapping.xml", "", ""),
	MWT_RULES("[LANG_SHORT]/[LANG]-multi-word-rule-system.regex", "", ""),
	NEOCLASSICAL_PREFIXES("[LANG_SHORT]/morphology/[LANG]-neoclassical-prefixes.txt", "", ""),
	SEGMENT_BANK("[LANG_SHORT]/[LANG]-segment-bank.xml", "", ""),
	STOP_WORDS_FILTER("[LANG_SHORT]/[LANG]-stop-word-filter.xml", "", ""),
	TREETAGGER_CONFIG("[LANG_SHORT]/tagging/[TAGGER]/[LANG]-treetagger.xml", "", ""),
	VARIANTS("[LANG_SHORT]/[LANG]-variants.yaml", "", "");
	
	
	private String pathPattern;
	private String title;
	private String description;

	
	private TermSuiteResource(String pathPattern, String title, String description) {
		this.pathPattern = pathPattern;
		this.title = title;
		this.description = description;
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
	
	private static final String TAGGER_SHORT_PATTERN = "[TAGGER_SHORT]";
	private static final String TAGGER_PATTERN = "[TAGGER]";
	private static final String LANG_PATTERN = "[LANG]";
	private static final String LANG_SHORT_PATTERN = "[LANG_SHORT]";


	public String getUrl(String protocol, Lang lang) {
		return String.format("%s:%s", protocol, getPath(lang));
	}

	public String getUrl(String protocol, Lang lang, Tagger tagger) {
		return String.format("%s:%s", protocol, getPath(lang, tagger));
	}

	public String getFileUrl(Lang lang) {
		return getUrl("file", lang);
	}

	public String getFileUrl(Lang lang, Tagger tagger) {
		return getUrl("file", lang, tagger);
	}

	
	public String getPath(Lang lang) {
		return getPath(lang, null);
	}
	
	public String getPath(Lang lang, Tagger tagger) {
		Preconditions.checkNotNull(lang);
		String path = getPathPattern()
				.replace(LANG_SHORT_PATTERN, lang.getCode())
				.replace(LANG_PATTERN, lang.getName().toLowerCase());
		if(getPathPattern().contains(TAGGER_PATTERN) || getPathPattern().contains(TAGGER_SHORT_PATTERN)) {
			Preconditions.checkArgument(
					tagger != null, 
					"Tagger should not be nil for resource %s.", 
					this.toString().toLowerCase());
			path = path
					.replace(TAGGER_SHORT_PATTERN, tagger.getShortName())
					.replace(TAGGER_PATTERN, tagger.getName());
		}
		
		return path;
		
	} 
	
	
	public static final TermSuiteResource forFileName(String fileName) {
		for(Lang l:Lang.values()) {
			for(Tagger t:Tagger.values()) {
				for(TermSuiteResource r:TermSuiteResource.values())
					if(r.getPath(l, t).equals(fileName))
						return r;
			}
		}
		return null;
	}
	
	
}
