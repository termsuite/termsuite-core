package eu.project.ttc.test.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.univnantes.termsuite.utils.FileUtils;

public class FileUtilsSpec {

	@Rule 
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testReplaceRootAsString() throws IOException {
		assertThat(FileUtils.replaceRootDir("/path/to/old/root/tata/toto.txt", "/path/to/old/root", "/path/to/new/root")).isEqualTo(
				"/path/to/new/root/tata/toto.txt");

	}

	@Test
	public void testFileUtilsSpec() {
		assertThat(FileUtils.replaceExtensionWith("tata.txt", "xmi"))
			.isEqualTo("tata.xmi");
		assertThat(FileUtils.replaceExtensionWith("tata", "xmi"))
		.isEqualTo("tata.xmi");
	assertThat(FileUtils.replaceExtensionWith("tata.toto.txt", "xmi"))
			.isEqualTo("tata.toto.xmi");
		assertThat(FileUtils.replaceExtensionWith("/path/to/toto.txt", "xmi"))
			.isEqualTo("/path/to/toto.xmi");
		assertThat(FileUtils.replaceExtensionWith("/path/to/toto.tata.txt", "xmi"))
			.isEqualTo("/path/to/toto.tata.xmi");
		assertThat(FileUtils.replaceExtensionWith("/path/t.o/toto.txt", "xmi"))
			.isEqualTo("/path/t.o/toto.xmi");
		assertThat(FileUtils.replaceExtensionWith("/path/t.o/toto", "xmi"))
		.isEqualTo("/path/t.o/toto.xmi");
	}
	
	
	@Test
	public void testReplaceRootBasic() throws IOException {
		File tata = folder.newFolder("tata");
		File toto = folder.newFile("tata/toto.txt");
		File titi = folder.newFolder("titi");
		
		String newPath = FileUtils.replaceRootDir(
				toto.getCanonicalPath(), folder.getRoot(), titi);
		
		assertThat(newPath).isEqualTo(folder.getRoot().getAbsolutePath() + "/titi/tata/toto.txt");

		String newPath2 = FileUtils.replaceRootDir(
				toto.getCanonicalPath(), tata, titi);
		
		assertThat(newPath2).isEqualTo(folder.getRoot().getAbsolutePath() + "/titi/toto.txt");

	}
}
