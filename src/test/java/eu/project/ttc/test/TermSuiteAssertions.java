package eu.project.ttc.test;

import org.apache.uima.jcas.JCas;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.test.func.TermAssert;
import eu.project.ttc.test.func.TermIndexAssert;
import eu.project.ttc.test.unit.CasAssert;

public class TermSuiteAssertions {

	public static CasAssert assertThat(JCas cas) {
		return new CasAssert(cas);
	}

	public static TermAssert assertThat(Term term) {
		return new TermAssert(term);
	}
	
	public static TermIndexAssert assertThat(TermIndex termIndex) {
		return new TermIndexAssert(termIndex);
	}
}
