package fr.univnantes.termsuite.framework.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import fr.univnantes.termsuite.api.ExtractorConfigIO;
import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.uima.ResourceType;

public class LanguageService {
	private static final String URL_FORMAT = ResourceType.DEFAULT_RESOURCE_URL_PREFIX + "%s/%s-extractor-config.json";

	public ExtractorOptions getDefaultExtractorConfig(Lang lang) {
		String configUrl = String.format(URL_FORMAT, lang.getCode(), lang.getName());
		URL url = getClass().getResource(configUrl);
		try(InputStream openStream = url.openStream()) {
			ExtractorOptions opts = ExtractorConfigIO.fromJson(openStream);
			return opts;
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
