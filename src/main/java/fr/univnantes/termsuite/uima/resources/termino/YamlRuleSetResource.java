package fr.univnantes.termsuite.uima.resources.termino;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.engines.gatherer.YamlRuleSet;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSetIO;

public class YamlRuleSetResource implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(YamlRuleSetResource.class);
	
	private YamlRuleSet ruleSet;
	
	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		InputStream inputStream = null;
		InputStreamReader reader = null;
		StringWriter writer = null;
		try {
			inputStream = aData.getInputStream();
			writer = new StringWriter();
			reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			IOUtils.copy(reader, writer);
			
			// set the adapter
			ruleSet = YamlRuleSetIO.fromYaml(writer.toString());
			
		} catch (IOException e) {
			LOGGER.error("Could not load the yaml variant rules resource dur to IOException");
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(writer);
		}
	}
	
	public YamlRuleSet getRuleSet() {
		return ruleSet;
	}
	

}
