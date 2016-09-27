package eu.project.ttc.test.termino.export;

import java.io.StringWriter;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.api.TSVOptions;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.termino.export.TSVExporter;
import eu.project.ttc.test.TermSuiteAssertions;
import eu.project.ttc.test.unit.TermFactory;

public class TsvExporterSpec {

	TermIndex termIndex;
	Collection<Term> terms;
	Term term1, term2, term3;
	StringWriter writer;
	
	@Before
	public void setup() {
		termIndex = Mockito.mock(TermIndex.class);

		term1 = TermFactory.termMock("t1", 1, 3, 0.8);
		term2 = TermFactory.termMock("t2", 2, 1, 0.8);
		term3 = TermFactory.termMock("t3", 3, 2, 1);

		
		TermVariation tv = Mockito.mock(TermVariation.class);
		Mockito.when(tv.getVariant()).thenReturn(term1);
		Mockito.when(term3.getVariations()).thenReturn(Sets.newHashSet(tv));
		
		terms = Lists.newArrayList(
				term1,
				term2,
				term3
			);
		
		
		Mockito.when(termIndex.getTerms()).thenReturn(terms);
		
		writer = new StringWriter();
	}
	
	@Test
	public void testTsvExportNoScore() {
		TSVExporter.export(termIndex, writer);
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(5)
			.tsvLineEquals(1, "#","type", "gkey", "f")
			.tsvLineEquals(2, 1, "T", "t2", 2)
			.tsvLineEquals(3, 2, "T", "t3", 3)
			.tsvLineEquals(4, 2, "V", "t1", 1)
			.tsvLineEquals(5, 3, "T", "t1", 1)
			;
	}

	@Test
	public void testTsvExportWithScores() {
		TSVExporter.export(termIndex, writer, new TSVOptions().setShowScores(true));
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(5)
			.tsvLineEquals(1, "#","type", "gkey", "f")
			.tsvLineEquals(2, 1, "T[]", "t2", 2)
			.tsvLineEquals(3, 2, "T[]", "t3", 3)
			.tsvLineEquals(4, 2, "V[0,00]", "t1", 1)
			.tsvLineEquals(5, 3, "T[]", "t1", 1)
			;
	}

	@Test
	public void testTsvExportNoHeaders() {
		TSVExporter.export(termIndex, writer, new TSVOptions().setShowHeaders(false));
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(4)
			.tsvLineEquals(1, 1, "T", "t2", 2)
			.tsvLineEquals(2, 2, "T", "t3", 3)
			.tsvLineEquals(3, 2, "V", "t1", 1)
			.tsvLineEquals(4, 3, "T", "t1", 1)
			;
	}

	@Test
	public void testTsvExportNoVariant() {
		TSVExporter.export(termIndex, writer, new TSVOptions().setShowVariants(false));
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(4)
			.tsvLineEquals(1, "#","type", "gkey", "f")
			.tsvLineEquals(2, 1, "T", "t2", 2)
			.tsvLineEquals(3, 2, "T", "t3", 3)
			.tsvLineEquals(4, 3, "T", "t1", 1)
			;
	}

}
