package fr.univnantes.termsuite.test.func.tools.cmd;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Splitter;
import com.google.common.io.Files;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.test.asserts.TermSuiteAssertions;
import fr.univnantes.termsuite.test.func.FunctionalTests;
import fr.univnantes.termsuite.tools.AlignerCLI;

public class AlignerCLISpec {
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	Path tsvPath;
	
	@Before
	public void setup() {
		tsvPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.tsv");
	}
	
	@Test
	public void testAlignerFrEnBasic() throws Exception {
		
		launch(String.format("--source-termino %s --target-termino %s --dictionary %s --term %s --tsv %s",
				FunctionalTests.getTerminoWEShortPath(Lang.FR),
				FunctionalTests.getTerminoWEShortPath(Lang.EN),
				FunctionalTests.getDicoPath(Lang.FR, Lang.EN),
				"énergie",
				tsvPath.toString()
				));
		TermSuiteAssertions.assertThat(tsv())
			.tsvLineEquals(1, 1, "énergie", "power", str(0.520), "DICTIONARY")
			.tsvLineEquals(2, 2, "énergie", "energy", str(0.295), "DICTIONARY")
			.hasLineCount(10);
	}
	
	@Test
	public void testAlignerDicoWithExplanation() throws Exception {
		launch(String.format("--source-termino %s --target-termino %s --dictionary %s --term %s -n 10 --explain  --tsv %s",
				FunctionalTests.getTerminoWEShortPath(Lang.FR),
				FunctionalTests.getTerminoWEShortPath(Lang.EN),
				FunctionalTests.getDicoPath(Lang.FR, Lang.EN),
				"énergie",
				tsvPath.toString()
				));
		TermSuiteAssertions.assertThat(tsv())
			.tsvLineEquals(1, 1, "énergie", "power", str(0.520), "DICTIONARY", "{}")
			.hasLineCount(10);

	}
	
	
	@Test
	public void testAlignerOnMWT() throws Exception {
		launch(String.format("--source-termino %s --target-termino %s --dictionary %s --term %s -n 10 --tsv %s",
				FunctionalTests.getTerminoWEShortPath(Lang.FR),
				FunctionalTests.getTerminoWEShortPath(Lang.EN),
				FunctionalTests.getDicoPath(Lang.FR, Lang.EN),
				"\"turbine éolienne\"",
				tsvPath.toString()
				));
		TermSuiteAssertions.assertThat(tsv())
			.tsvLineEquals(1, 1, "turbine éolienne", "wind turbine", str(1), "COMPOSITIONAL")
			.hasLineCount(1);
	}


	@Test
	public void testAlignerDistribWithExplanation() throws Exception {
		launch(String.format("--source-termino %s --target-termino %s --dictionary %s --term %s -n 10 --explain  --tsv %s",
				FunctionalTests.getTerminoWEShortPath(Lang.FR),
				FunctionalTests.getTerminoWEShortPath(Lang.EN),
				FunctionalTests.getDicoPath(Lang.FR, Lang.EN),
				"rotor",
				tsvPath.toString()
				));
		TermSuiteAssertions.assertThat(tsv())
			.tsvLineEquals(1, 1, "rotor", "rotor", str(0.932), "DICTIONARY", "{}")
			.tsvLineEquals(2, 2, "rotor", "extreme", str(0.008), "DISTRIBUTIONAL", "{wind: 117,31}")
			.hasLineCount(10);
	}
	
	

	@Test
	public void testAlignerPrintToSystemOut() throws Exception {
		
		launch(String.format("--source-termino %s --target-termino %s --dictionary %s --term %s -n 3 ",
				FunctionalTests.getTerminoWEShortPath(Lang.FR),
				FunctionalTests.getTerminoWEShortPath(Lang.EN),
				FunctionalTests.getDicoPath(Lang.FR, Lang.EN),
				"énergie",
				tsvPath.toString()
				));
	}
	

	@Test
	public void testReadTermsFromFile() throws Exception {
		Path listFile = Paths.get(folder.getRoot().getAbsolutePath(), "termList.txt");
		listFile.toFile().delete();
		Files.append("  énergie  \n", listFile.toFile(), Charset.defaultCharset());
		Files.append("  turbine   \t éolienne  \n", listFile.toFile(), Charset.defaultCharset());
		
		launch(String.format("--source-termino %s --target-termino %s --dictionary %s --term-list %s -n 3  --tsv %s",
				FunctionalTests.getTerminoWEShortPath(Lang.FR),
				FunctionalTests.getTerminoWEShortPath(Lang.EN),
				FunctionalTests.getDicoPath(Lang.FR, Lang.EN),
				listFile,
				tsvPath.toString()
				));

		System.out.println(tsv());
		TermSuiteAssertions.assertThat(tsv())
			.hasLineCount(4)
			.tsvLineEquals(1, 1, "énergie", "power", str(0.554), "DICTIONARY")
			.tsvLineEquals(2, 2, "énergie", "energy", str(0.315), "DICTIONARY")
			.tsvLineEquals(3, 3, "énergie", "motors", str(0.131), "DISTRIBUTIONAL")
			.tsvLineEquals(4, 1, "turbine éolienne", "wind turbine", str(1), "COMPOSITIONAL")
			;
	}
	
	@Test
	public void testAlignerFrEnCount3() throws Exception {
		
		launch(String.format("--source-termino %s --target-termino %s --dictionary %s --term %s -n 3  --tsv %s",
				FunctionalTests.getTerminoWEShortPath(Lang.FR),
				FunctionalTests.getTerminoWEShortPath(Lang.EN),
				FunctionalTests.getDicoPath(Lang.FR, Lang.EN),
				"énergie",
				tsvPath.toString()
				));

		TermSuiteAssertions.assertThat(tsv())
			.hasLineCount(3)
			.tsvLineEquals(1, 1, "énergie", "power", str(0.554), "DICTIONARY")
			.tsvLineEquals(2, 2, "énergie", "energy", str(0.315), "DICTIONARY")
			.tsvLineEquals(3, 3, "énergie", "motors", str(0.131), "DISTRIBUTIONAL")
			;
	}
	
	
	private String str(double d) {
		return String.format("%.3f", d);
	}

	private String tsv() throws IOException {
		return Files.toString(tsvPath.toFile(), Charset.defaultCharset());
	}

	
	private void launch(String args) throws Exception {
		List<String> argList = Splitter.on(" ").splitToList(args);
		new AlignerCLI().launch(argList.toArray(new String[argList.size()]));
	}

}
