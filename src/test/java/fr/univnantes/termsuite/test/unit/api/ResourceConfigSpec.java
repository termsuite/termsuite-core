package fr.univnantes.termsuite.test.unit.api;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.test.unit.io.JsonTerminologyIOSpec;

public class ResourceConfigSpec {

	private static final String DIR_PATH = "./src/main/resources/fr/univnantes/termsuite/resources/";
	private ResourceConfig resourceConfig;
	
	@Before
	public void setup() {
		resourceConfig = new ResourceConfig();
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAddInvalidDirectory() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Directory does not exist");

		resourceConfig.addDirectory(Paths.get(DIR_PATH + "ebgeb"));
		// should  raise exception
	}
	
	@Test
	public void testLoadResourcesFromDirectoryWithMissingTrailingSlash() {
		resourceConfig.addDirectory(Paths.get(DIR_PATH.substring(0, DIR_PATH.length() - 1)));
		assertEquals(DIR_PATH, resourceConfig.getURLPrefixes().get(0).getPath());
	}
	
	@Test
	public void testLoadResourcesFromJarExceptionDirectory() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Not a jar");
		
		resourceConfig.addJar(Paths.get(DIR_PATH));
	}
	
	@Test
	public void testLoadResourcesFromJarExceptionSimpleExistingFile() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Not a jar");
		
		resourceConfig.addJar(Paths.get(JsonTerminologyIOSpec.jsonFile1));
	}
}
