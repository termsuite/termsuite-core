package fr.univnantes.termsuite.test.termino.export;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TsvOptions;
import fr.univnantes.termsuite.export.TsvExporter;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;
import fr.univnantes.termsuite.test.TermSuiteAssertions;
import fr.univnantes.termsuite.test.unit.TermFactory;

public class TsvExporterSpec {

	Terminology termIndex;
	Collection<Term> terms;
	Term term1, term2, term3;
	StringWriter writer;
	
	@Before
	public void setup() {
		defaultLocale = Locale.getDefault();

		termIndex = new MemoryTerminology("", Lang.FR, new MemoryOccurrenceStore());

		term1 = TermFactory.termMock("t1", 1, 3, 0.8);
		term2 = TermFactory.termMock("t2", 2, 1, 0.8);
		term3 = TermFactory.termMock("t3", 3, 2, 1);
		
		TermRelation tv = new TermRelation(RelationType.MORPHOLOGICAL, term3, term1);
		termIndex.addTerm(term1);
		termIndex.addTerm(term2);
		termIndex.addTerm(term3);
		termIndex.addRelation(tv);
		
		terms = Lists.newArrayList(
				term1,
				term2,
				term3
			);
		
		
		writer = new StringWriter();
		
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
			.tsvLineEquals(4, 2, "V[M]", "t1", 1)
			.tsvLineEquals(5, 3, "T", "t1", 1)
			;
	}

	@Test
	public void testTsvExportNoHeaders() {
		TsvExporter.export(termIndex, writer, new TsvOptions().showHeaders(false));
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(4)
			.tsvLineEquals(1, 1, "T", "t2", 2)
			.tsvLineEquals(2, 2, "T", "t3", 3)
			.tsvLineEquals(3, 2, "V[M]", "t1", 1)
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
