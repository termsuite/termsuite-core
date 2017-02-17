package fr.univnantes.termsuite.test.func.tools.cmd;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Splitter;

import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.func.FunctionalTests;
import fr.univnantes.termsuite.tools.PreprocessorCLI;

public class PreprocessorCLISpec {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testTerminoWithoutOutputParam() throws Exception {
		try {
			launch(String.format("-t %s -c %s -l %s --watch \"%s\"" ,
					FunctionalTests.getTaggerPath(),
					FunctionalTests.getCorpusWEShortPath(Lang.EN),
					Lang.EN.getCode(),
					"offshore wind energy"
					));
			fail("Should have rased an exception because at least one output param must be set.");
		} catch(Exception e) {
			assertThat(e).hasMessageContaining("At least one option in --json, --json-anno, --tsv-anno, --xmi-anno must be set");
		}

	}

	
	@Test
	public void testTerminoEnBasic() throws Exception {
		
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		Path xmiAnnoPath = Paths.get(folder.getRoot().getAbsolutePath(), "xmi");
		Path jsonAnnoPath = Paths.get(folder.getRoot().getAbsolutePath(), "json");
		Path tsvAnnoPath = Paths.get(folder.getRoot().getAbsolutePath(), "tsv");
		
		
		assertThat(jsonPath.toFile()).doesNotExist();
		assertThat(xmiAnnoPath.toFile()).doesNotExist();
		assertThat(jsonAnnoPath.toFile()).doesNotExist();
		assertThat(tsvAnnoPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --tsv-anno %s --xmi-anno %s --json-anno %s --json %s --watch \"%s\"" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				tsvAnnoPath.toString(),
				xmiAnnoPath.toString(),
				jsonAnnoPath.toString(),
				jsonPath.toString(),
				"offshore wind energy"
			));
		
		assertThat(jsonPath.toFile()).exists().isFile();
		assertThat(xmiAnnoPath.toFile()).exists().isDirectory();
		assertThat(jsonAnnoPath.toFile()).exists().isDirectory();
		assertThat(tsvAnnoPath.toFile()).exists().isDirectory();
		
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		assertThat(termindex)
			.containsTerm("nn: wind energy")
			.hasNTerms(11521);
		Term term = termindex.getTerms().get("nn: wind energy");
		assertNull(term.getContext());
	}

	private void launch(String args) throws Exception {
		List<String> argList = Splitter.on(" ").splitToList(args);
		new PreprocessorCLI().launch(argList.toArray(new String[argList.size()]));
	}
}
