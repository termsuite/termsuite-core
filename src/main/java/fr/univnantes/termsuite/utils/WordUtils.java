package fr.univnantes.termsuite.utils;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Word;

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
