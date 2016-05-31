package eu.project.ttc.utils;

import eu.project.ttc.engines.desc.Lang;

public class IstexUtils {

	public static Lang toTermSuiteLang(String istexLang) {
		switch (istexLang) {
		case "eng":
			return Lang.EN;
		case "fra":
			return Lang.FR;
		default:
			throw new IllegalArgumentException(String.format("Language %s not supported by Istex API", istexLang));
		}
	}
}
