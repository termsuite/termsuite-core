package org.ttc.project.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;

import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.TermOccurrence;

public class ContextVectorSpec {

	private ContextVector vector;
	private Document doc;

	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		this.vector = new ContextVector(Fixtures.term1());
		this.doc = new Document("doc1");
		this.vector.addCooccurrence(new TermOccurrence(Fixtures.term1(), "text1", doc, 10, 15));
		this.vector.addCooccurrence(new TermOccurrence(Fixtures.term3(), "text2", doc, 30, 45));
		this.vector.addCooccurrence(new TermOccurrence(Fixtures.term2(), "text2", doc, 50, 65));
		this.vector.addCooccurrence(new TermOccurrence(Fixtures.term1(), "text1", doc, 70, 90));
		this.vector.addCooccurrence(new TermOccurrence(Fixtures.term3(), "text1", doc, 100, 115));
		this.vector.addCooccurrence(new TermOccurrence(Fixtures.term1(), "text2", doc, 200, 215));
	}
	
	@Test
	public void testGetEntries() {
		assertThat(this.vector.getEntries()).extracting("coTerm", "nbCooccs", "assocRate").containsExactly(
				tuple(Fixtures.term1(), 3, 0d),
				tuple(Fixtures.term3(), 2, 0d),
				tuple(Fixtures.term2(), 1, 0d)
			);
		this.vector.addCooccurrence(new TermOccurrence(Fixtures.term1(), "text2", doc, 64, 65));
		assertThat(this.vector.getEntries()).extracting("coTerm", "nbCooccs", "assocRate").containsExactly(
				tuple(Fixtures.term1(), 4, 0d),
				tuple(Fixtures.term3(), 2, 0d),
				tuple(Fixtures.term2(), 1, 0d)
			);
	}
}
