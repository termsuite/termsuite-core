package eu.project.ttc.test.termino.export;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.api.TsvOptions;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.termino.export.TsvExporter;
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

		
		TermRelation tv = Mockito.mock(TermRelation.class);
		Mockito.when(tv.getTo()).thenReturn(term1);
		Mockito.when(termIndex.getOutboundRelations(term3)).thenReturn(Sets.newHashSet(tv));
		
		terms = Lists.newArrayList(
				term1,
				term2,
				term3
			);
		
		
		Mockito.when(termIndex.getTerms()).thenReturn(terms);
		
		writer = new StringWriter();
		
		defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
	}
	
	Locale defaultLocale;
	@After
	public void tearDown() {
		Locale.setDefault(defaultLocale);
	}
	
	@Test
	public void testTsvExportNoScore() {
		TsvExporter.export(termIndex, writer);
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
		
		TsvExporter.export(termIndex, writer, new TsvOptions().showScores(true));
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(5)
			.tsvLineEquals(1, "#","type", "gkey", "f")
			.tsvLineEquals(2, 1, "T[]", "t2", 2)
			.tsvLineEquals(3, 2, "T[]", "t3", 3)
			.tsvLineEquals(4, 2, "V[0.00]", "t1", 1)
			.tsvLineEquals(5, 3, "T[]", "t1", 1)
			;
	}

	@Test
	public void testTsvExportNoHeaders() {
		TsvExporter.export(termIndex, writer, new TsvOptions().showHeaders(false));
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
		TsvExporter.export(termIndex, writer, new TsvOptions().setShowVariants(false));
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(4)
			.tsvLineEquals(1, "#","type", "gkey", "f")
			.tsvLineEquals(2, 1, "T", "t2", 2)
			.tsvLineEquals(3, 2, "T", "t3", 3)
			.tsvLineEquals(4, 3, "T", "t1", 1)
			;
	}

}
