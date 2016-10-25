package eu.project.ttc.utils;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.Word;

public class WordUtils {

	/**
	 * 
	 * @param compound
	 * @param component
	 * @return
	 */
	public static String getComponentSubstring(Word compound, Component component) {
		return compound.getLemma().substring(component.getBegin(), component.getEnd());
	}

}
