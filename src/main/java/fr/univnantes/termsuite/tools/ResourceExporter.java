package fr.univnantes.termsuite.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.uima.ResourceType;

/**
 * 
 * Export the built-in resources to an external directory.
 * 
 * @author Damien Cram
 *
 */
public class ResourceExporter {
	
	
	public static void main(String[] args) throws IOException {
		if(args.length==0) {
			System.err.println("Error: missing DESTINATION argument.");
			System.err.format("usage: java -cp termsuite-core.jar %s DESTINATION%n", ResourceExporter.class.getCanonicalName());
			System.exit(1);
		} else {
			String pathStr = args[0].replaceFirst("^~",System.getProperty("user.home"));
			new ResourceExporter().exportTo(Paths.get(pathStr));
		}
	}
	
	
	public void exportTo(Path toDirectoryPath) throws IOException {
		if(toDirectoryPath.toFile().exists())
			Preconditions.checkArgument(toDirectoryPath.toFile().isDirectory(), "Not a directory: %s", toDirectoryPath);
		if(!toDirectoryPath.toFile().exists())
			toDirectoryPath.toFile().mkdirs();
		
		for(Lang lang:Lang.values()) {
			for(Tagger tagger:Tagger.values()) {
				for(ResourceType r:ResourceType.values()) {
					Path targetResourcePath = Paths.get(toDirectoryPath.toString(), r.getPath(lang, tagger));
					if(targetResourcePath.toFile().exists())
						continue;
					else {
						targetResourcePath.getParent().toFile().mkdirs();
						if(r.existsInClasspath(lang,  tagger))
							FileUtils.copyURLToFile(
									r.fromClasspath(lang, tagger), 
									targetResourcePath.toFile());
					}
				}
			}
		}
	}
}
