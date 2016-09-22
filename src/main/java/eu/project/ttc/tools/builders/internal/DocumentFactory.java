package eu.project.ttc.tools.builders.internal;

import java.nio.file.Path;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.tools.builders.CorpusException;
import eu.project.ttc.tools.builders.Document;

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
