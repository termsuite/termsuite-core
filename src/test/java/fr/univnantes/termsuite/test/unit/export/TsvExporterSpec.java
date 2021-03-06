package fr.univnantes.termsuite.test.unit.export;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.io.tsv.TsvOptions;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.asserts.TermSuiteAssertions;
import fr.univnantes.termsuite.test.mock.TermFactory;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class TsvExporterSpec {

	Terminology termino;
	Collection<Term> terms;
	Term term1, term2, term3;
	StringWriter writer;
	
	@Before
	public void setup() {
		defaultLocale = Locale.getDefault();

		termino = new Terminology("", Lang.FR);

		term1 = TermFactory.termMock("t1", 1, 3, 0.8);
		term2 = TermFactory.termMock("t2", 2, 1, 0.8);
		term3 = TermFactory.termMock("t3", 3, 2, 1);
		
		Relation tv = new Relation(RelationType.VARIATION, term3, term1);
		tv.setProperty(RelationProperty.IS_MORPHOLOGICAL, true);
		UnitTests.addTerm(termino, term1);
		UnitTests.addTerm(termino, term2);
		UnitTests.addTerm(termino, term3);
		termino.getRelations().add(tv);

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
		TermSuiteFactory.createTsvExporter()
			.export(termino, writer);
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(5)
			.tsvLineEquals(1, "#","type", "key", "freq")
			.tsvLineEquals(2, 1, "T", "t2", 2)
			.tsvLineEquals(3, 2, "T", "t3", 3)
			.tsvLineEquals(4, 2, "V[m]", "t1", 1)
			.tsvLineEquals(5, 3, "T", "t1", 1)
			;
	}

	@Test
	public void testTsvExportNoHeaders() {
		TermSuiteFactory.createTsvExporter(new TsvOptions().showHeaders(false))
			.export(termino, writer);
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(4)
			.tsvLineEquals(1, 1, "T", "t2", 2)
			.tsvLineEquals(2, 2, "T", "t3", 3)
			.tsvLineEquals(3, 2, "V[m]", "t1", 1)
			.tsvLineEquals(4, 3, "T", "t1", 1)
			;
	}
	

	@Test
	public void testTsvExportWithSourceProperty() {
		TsvOptions tsvOptions = new TsvOptions();
		tsvOptions.baseOrVariationTargetProperty(TermProperty.GROUPING_KEY);
		tsvOptions.baseOrVariationTargetProperty(TermProperty.FREQUENCY);
		tsvOptions.sourceProperty(TermProperty.GROUPING_KEY);
		TermSuiteFactory.createTsvExporter(tsvOptions)
			.export(termino, writer);
		TermSuiteAssertions.assertThat(writer.toString())
			.hasLineCount(5)
			.tsvLineEquals(1, "#","type", "key", "freq", "source:key")
			.tsvLineEquals(2, 1, "T", "t2", 2, "")
			.tsvLineEquals(3, 2, "T", "t3", 3, "")
			.tsvLineEquals(4, 2, "V[m]", "t1", 1, "t3")
			.tsvLineEquals(5, 3, "T", "t1", 1, "")
			;
	}


	@Test
	public void testTsvExportNoVariant() {
		TermSuiteFactory.createTsvExporter(new TsvOptions().setShowVariants(false))
			.export(termino, writer);
		String string = writer.toString();
		TermSuiteAssertions.assertThat(string)
			.hasLineCount(4)
			.tsvLineEquals(1, "#","type", "key", "freq")
			.tsvLineEquals(2, 1, "T", "t2", 2)
			.tsvLineEquals(3, 2, "T", "t3", 3)
			.tsvLineEquals(4, 3, "T", "t1", 1)
			;
	}

}
