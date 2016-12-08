package fr.univnantes.termsuite.test.func.tools.cmd;

import static fr.univnantes.termsuite.test.TermSuiteAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import com.google.common.base.Splitter;

import fr.univnantes.termsuite.api.TerminologyIO;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.test.TermSuiteAssertions;
import fr.univnantes.termsuite.test.func.FunctionalTests;
import fr.univnantes.termsuite.tools.TerminologyExtractor;
import fr.univnantes.termsuite.utils.FileUtils;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TermSuiteTerminoCLISpec {
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	
	@After
	public void clear() {
		TermSuiteResourceManager.getInstance().clear();
	}
	
	@Test
	public void testTerminoEnBasic() throws Exception {
		
		
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		Path tbxPath = Paths.get(folder.getRoot().getAbsolutePath(), "tbx.json");
		Path tsvPath = Paths.get(folder.getRoot().getAbsolutePath(), "tsv.json");
		
		
		assertThat(jsonPath.toFile()).doesNotExist();
		assertThat(tbxPath.toFile()).doesNotExist();
		assertThat(tbxPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --tsv %s --tbx %s --json %s" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				tsvPath.toString(),
				tbxPath.toString(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		assertThat(tbxPath.toFile()).exists();
		assertThat(tbxPath.toFile()).exists();
		
		Terminology termindex = TerminologyIO.fromJson(jsonPath);
		assertThat(termindex).containsTerm("nn: wind energy").hasNTerms(843);
		assertNull(termindex.getTermByGroupingKey("nn: wind energy").getContext());
	}


	@Test
	public void testTerminoEnWithSpecificTsvExport() throws Exception {
		
		Path tsvPath = Paths.get(folder.getRoot().getAbsolutePath(), "tsv.json");
		
		
		assertThat(tsvPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --tsv %s "
				+ "--tsv-properties pilot,frequency,dfreq,pattern,spottingRule "
				+ "--tsv-show-scores" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				tsvPath.toString()
			));
		
		assertThat(tsvPath.toFile()).exists();

		TermSuiteAssertions.assertThat(FileUtils.readFile(tsvPath.toString(), Charset.defaultCharset()))
			.tsvLineEquals(1, "#","type","pilot","f","dfreq","p", "rule")
			.tsvLineEquals(2, "1","T","rotor","96","2", "N", "n")
			;
				
	}

	@Test
	public void testTerminoEnWithFilterTh() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		assertThat(jsonPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --json %s --filter-property dfreq --filter-th 2" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = TerminologyIO.fromJson(jsonPath);
		assertThat(termindex)
			.containsTerm("nn: wind energy")
			.containsRelation("nn: wind energy", RelationType.SYNTACTICAL, "ann: offshore wind energy")
			.hasNTerms(92);
	}
	

	@Test
	public void testTerminoEnWithFilterTop100() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		assertThat(jsonPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --json %s --filter-property specificity --filter-top-n 100 --filter-variants" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = TerminologyIO.fromJson(jsonPath);
		assertThat(termindex).containsTerm("nn: wind turbine").hasNTerms(100);
	}

	
	@Test
	public void testTerminoEnContextualizeSWTOnly() throws Exception {
		
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino1.json");
		
		assertThat(jsonPath.toFile()).doesNotExist();
		assertTrue(jsonPath.toFile().getAbsoluteFile().getParentFile().canWrite());
		
		launch(String.format("-t %s -c %s -l %s --json %s "
				+ "--contextualize --context-scope 4 --allow-mwts-in-contexts" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = TerminologyIO.fromJson(jsonPath);
		assertThat(termindex).containsTerm("nn: wind energy");
//		assertNotNull(termindex.getTermByGroupingKey("n: wind").getContext());
		assertNull(termindex.getTermByGroupingKey("nn: wind energy").getContext());
	}

	@Test
	public void testTerminoEnContextualizeAllTerms() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino2.json");
		
		assertThat(jsonPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --json %s "
				+ "--contextualize --context-scope 2 --contextualize-all-terms --allow-mwts-in-contexts" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = TerminologyIO.fromJson(jsonPath);
		assertThat(termindex).containsTerm("nn: wind energy");
//		assertNotNull(termindex.getTermByGroupingKey("n: wind").getContext());
		assertNotNull(termindex.getTermByGroupingKey("nn: wind energy").getContext());
	}

	
	private void launch(String args) throws Exception {
		List<String> argList = Splitter.on(" ").splitToList(args);
		TerminologyExtractor.main(argList.toArray(new String[argList.size()]));
	}
}
