package eu.project.ttc.test.func;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	FrenchWindEnergySpec.class
	})
public class FunctionalTests {
	public static final String CORPUS_WE_PATH="eu/project/ttc/test/corpus/we/";
	private static final String FUNCTION_TESTS_CONFIG = "termsuite-test.properties";
	private static final String PROP_RESOURCES_PATH = "resources.path";
	private static final String PROP_TREETAGGER_HOME_PATH = "treetagger.home.path";

	public static String getResourcePath() {
		return (String)getConfigProperty(PROP_RESOURCES_PATH);
	}

	private static Object getConfigProperty( String propName) {
		InputStream is = FunctionalTests.class.getClassLoader().getResourceAsStream(FUNCTION_TESTS_CONFIG);
		Properties properties = new Properties();
		try {
			properties.load(is);
			is.close();
			Preconditions.checkArgument(!properties.contains(propName), "No such property in config file %s: %s", FUNCTION_TESTS_CONFIG, propName);
			return properties.get(propName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCorpusWEPath(Lang lang) {
		return "src/test/resources/" + CORPUS_WE_PATH + lang.getName().toLowerCase() + "/txt/";
	}

	public static String getTaggerPath() {
		return (String)getConfigProperty(PROP_TREETAGGER_HOME_PATH);
	}
	
	public static List<Term> termsByProperty(TermIndex termIndex, TermProperty termProperty, boolean desc) {
		List<Term> terms = Lists.newArrayList(termIndex.getTerms());
		Collections.sort(terms, termProperty.getComparator(termIndex, desc));
		return terms;
	}
}
