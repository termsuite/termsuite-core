package eu.project.ttc.resources;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import eu.project.ttc.engines.morpho.Segmentation;
import fr.univnantes.julestar.uima.resources.MapResource;

public class MorphologyExceptionResource extends MapResource {

	
	private Map<String, Segmentation> segmentation = Maps.newHashMap();
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doKeyValue(int lineNum, String line, String key, String value) {
	}

}
