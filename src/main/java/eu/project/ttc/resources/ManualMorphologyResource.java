package eu.project.ttc.resources;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.project.ttc.engines.morpho.Segmentation;
import eu.project.ttc.io.SegmentationParser;
import fr.univnantes.julestar.uima.resources.MapResource;

public class ManualMorphologyResource extends MapResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(ManualMorphologyResource.class);

	private Map<String, Segmentation> segmentations = Maps.newHashMap();
	
	private SegmentationParser parser = new SegmentationParser();
	
	@Override
	protected void doKeyValue(int lineNum, String line, String key, String value) {
		if(segmentations.containsKey(key))
			LOGGER.warn("Ignoring duplicate word lemma {} in {} resource", key, this.getClass().getSimpleName());
		else
			segmentations.put(key, parser.parse(key));
	}
	
	public Segmentation getSegmentation(String lemma) {
		return segmentations.get(lemma);
	}

}
