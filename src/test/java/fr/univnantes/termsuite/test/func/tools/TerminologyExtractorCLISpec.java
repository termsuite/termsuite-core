
package fr.univnantes.termsuite.test.func.tools;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import com.google.common.base.Splitter;

import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.asserts.TermSuiteAssertions;
import fr.univnantes.termsuite.test.func.FunctionalTests;
import fr.univnantes.termsuite.tools.TerminologyExtractorCLI;
import fr.univnantes.termsuite.utils.FileUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TerminologyExtractorCLISpec {
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testTerminoEnBasicFromXMIPreparedCorpus() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		assertThat(jsonPath.toFile()).doesNotExist();
		
		Path xmiPath = FunctionalTests.getXmiPreprocessedCorpusWEShortPath(Lang.EN);
		launch(String.format("--from-prepared-corpus %s -l %s --json %s",
				xmiPath,
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		assertThat(jsonPath.toFile()).exists();
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		assertThat(termindex)
			.containsTerm("nn: wind energy")
			.hasNTerms(2506);
		assertNull(termindex.getTerms().get("n: energy").getContext());
	}

	@Test
	public void testTerminoEnBasicFromImportedTermino() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		assertThat(jsonPath.toFile()).doesNotExist();
		Path terminoPath = FunctionalTests.getPreprocessedCorpusWEShortPathAsTermino(Lang.EN);
		launch(String.format("--from-prepared-corpus %s -l %s --json %s",
				terminoPath,
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		assertThat(jsonPath.toFile()).exists();
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		assertThat(termindex)
			.containsTerm("nn: wind energy")
			.hasNTerms(2506);
		assertNull(termindex.getTerms().get("n: energy").getContext());
	}

	@Test
	public void testTerminoEnBasicFromCorpus() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		Path tbxPath = Paths.get(folder.getRoot().getAbsolutePath(), "tbx.json");
		Path tsvPath = Paths.get(folder.getRoot().getAbsolutePath(), "tsv.json");
		
		assertThat(jsonPath.toFile()).doesNotExist();
		assertThat(tbxPath.toFile()).doesNotExist();
		assertThat(tsvPath.toFile()).doesNotExist();
		
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
		assertThat(tsvPath.toFile()).exists();
		
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		assertThat(termindex)
			.containsTerm("nn: wind energy")
			.hasNTerms(2506);

		assertNull(termindex.getTerms().get("n: energy").getContext());
	}


	@Test
	public void testTerminoEnFromCorpusWithSpecificTsvExport() throws Exception {
		
		Path tsvPath = Paths.get(folder.getRoot().getAbsolutePath(), "tsv.json");
		
		assertThat(tsvPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --tsv %s "
				+ " --tsv-properties pilot,frequency,dfreq,pattern,spottingRule,graphSim"
				,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				tsvPath.toString()
			));
		
		assertThat(tsvPath.toFile()).exists();

		TermSuiteAssertions.assertThat(FileUtils.readFile(tsvPath.toString(), Charset.defaultCharset()))
			.tsvLineEquals(1, "#","type","pilot","f","dfreq","p", "rule", "graphSim")
			.tsvLineEquals(2, "1","T","rotor","282","2", "N", "n", "")
			;
				
	}

	@Test
	public void testTerminoEnWithFilterTh() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		assertThat(jsonPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --json %s --post-filter-property dfreq --post-filter-th 2" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		
		assertThat(termindex)
			.containsTerm("nn: wind energy")
			.containsVariation("nn: wind speed", VariationType.SYNTAGMATIC, "ann: average wind speed")
			.hasNTerms(499);
	}
	

	@Test
	public void testTerminoEnWithFilterTop100() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		assertThat(jsonPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --json %s --post-filter-property specificity --post-filter-top-n 100" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		assertThat(termindex).containsTerm("nn: wind turbine").hasNTerms(100);
	}

	@Test
	public void testTerminoEnFromCorpusDisablePostProc() throws Exception {
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino.json");
		assertThat(jsonPath.toFile()).doesNotExist();
		
		launch(String.format("-t %s -c %s -l %s --json %s --disable-post-processing" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		
		assertThat(termindex.getRelations()).isNotEmpty();
		for(Relation relation:termindex.getRelations()) {
			assertFalse(relation.isPropertySet(RelationProperty.AFFIX_ORTHOGRAPHIC_SCORE));
			assertFalse(relation.isPropertySet(RelationProperty.NORMALIZED_EXTENSION_SCORE));
		}
		
		assertThat(termindex.getTerms()).isNotEmpty();
		for(Term term:termindex.getTerms().values()) {
			assertFalse(term.isPropertySet(TermProperty.INDEPENDANCE));
			assertFalse(term.isPropertySet(TermProperty.ORTHOGRAPHIC_SCORE));
		}
	}

	@Test
	public void testTerminoEnContextualizeSWTOnly() throws Exception {
		
		Path jsonPath = Paths.get(folder.getRoot().getAbsolutePath(), "termino1.json");
		
		assertThat(jsonPath.toFile()).doesNotExist();
		assertTrue(jsonPath.toFile().getAbsoluteFile().getParentFile().canWrite());
		
		launch(String.format("-t %s -c %s -l %s --json %s "
				+ "--contextualize --context-scope 4" ,
				FunctionalTests.getTaggerPath(),
				FunctionalTests.getCorpusWEShortPath(Lang.EN),
				Lang.EN.getCode(),
				jsonPath.toString()
			));
		
		assertThat(jsonPath.toFile()).exists();
		
		Terminology termindex = IndexedCorpusIO.fromJson(jsonPath).getTerminology();
		assertThat(termindex).containsTerm("nn: wind energy");
		assertNull(termindex.getTerms().get("nn: wind energy").getContext());
		assertNotNull(termindex.getTerms().get("n: energy").getContext());
		Term term = termindex.getTerms().get("n: rotor");
		assertNotNull(term);
		assertNotNull(term.getContext());
	}
	
	private void launch(String args) throws Exception {
		List<String> argList = Splitter.on(" ").splitToList(args);
		TerminologyExtractorCLI.main(argList.toArray(new String[argList.size()]));
	}
}
