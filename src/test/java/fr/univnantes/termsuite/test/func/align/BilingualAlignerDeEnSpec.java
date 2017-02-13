package fr.univnantes.termsuite.test.func.align;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.alignment.AlignmentMethod;
import fr.univnantes.termsuite.alignment.BilingualAlignmentService;
import fr.univnantes.termsuite.alignment.TranslationCandidate;
import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.api.BilingualAligner;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class BilingualAlignerDeEnSpec {

	Terminology deTermino;
	Terminology enTermino;
	BilingualAlignmentService aligner;
	
	@Before
	public void setup() {
		deTermino = IndexedCorpusIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.DE)).getTerminology();
		enTermino = IndexedCorpusIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.EN)).getTerminology();
		aligner = new BilingualAligner()
				.setSourceTerminology(deTermino)
				.setTargetTerminology(enTermino)
				.setDicoPath(FunctionalTests.getDicoPath(Lang.DE, Lang.EN))
				.setDistanceCosine()
				.create();
	}
	
	@Test
	public void testAlignerMWTComp() {
		Term t1 = deTermino.getTerms().get("n: windenergie");

		List<TranslationCandidate> results = aligner.alignSize2(t1, 3, 2);
		assertThat(results)
			.extracting("term.groupingKey", "method")
			.contains(
					tuple("nn: wind energy", AlignmentMethod.COMPOSITIONAL)
					)
			.hasSize(3)
			;
	}
}
