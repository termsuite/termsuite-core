package eu.project.ttc.tools.builders;

import java.nio.file.Path;

import eu.project.ttc.engines.desc.Lang;

public class DocumentFactory {
	
	public static Document create(Lang lang, Path path) {
		try {
			return new Document(
					lang,
					path.toUri().toURL().toString(),
					org.apache.commons.io.FileUtils.readFileToString(path.toFile())
				);
		} catch (Exception e) {
			throw new CorpusException(e);
		}
	}

}
