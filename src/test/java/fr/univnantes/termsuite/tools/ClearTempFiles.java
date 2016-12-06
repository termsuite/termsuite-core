package fr.univnantes.termsuite.tools;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

import fr.univnantes.termsuite.test.func.FunctionalTests;

public class ClearTempFiles {

	public static void main(String[] args) throws IOException {
		FileUtils.deleteDirectory(FunctionalTests.getTestTmpDir().toFile());
	}
}
