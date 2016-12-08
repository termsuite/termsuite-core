package fr.univnantes.termsuite.engines.gatherer;

import java.util.concurrent.ConcurrentMap;

import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

public class GroovyService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GroovyService.class);
	private static final String GROOVY_MATCH_METHOD_NAME = "match";
	private static final String GROOVY_SET_HELPER_METHOD_NAME = "setHelper";
	private static int CLASS_NUM = 0;

	private ConcurrentMap<TermWord, GroovyWord> groovyWords = Maps.newConcurrentMap();
	private ConcurrentMap<Term, GroovyTerm> groovyTerms = Maps.newConcurrentMap();
	private ConcurrentMap<Component, GroovyComponent> groovyComponents = Maps.newConcurrentMap();
	private ConcurrentMap<VariantRule, GroovyObject> groovyRules = Maps.newConcurrentMap();
	private GroovyHelper groovyHelper;
	
	public GroovyService(Terminology termino) {
		super();
		this.groovyHelper = new GroovyHelper();
		this.groovyHelper.setTerminology(termino);
	}

	public GroovyTerm asGroovyTerm(Term term) {
		if(!this.groovyTerms.containsKey(term))
			this.groovyTerms.put(term, new GroovyTerm(term, this));
		return this.groovyTerms.get(term);
	}
	
	private GroovyObject asGroovyRule(VariantRule rule) {
		if(!this.groovyRules.containsKey(rule)) {
			try {
				String script = String.format(""
						+ "class GroovyVariantRule%s {\n"
						+ "def helper;\n"
						+ "def setHelper(h) {this.helper = h;}\n"
						+ "def prefix(s,t){return this.helper.isPrefixOf(s,t);}\n"
						+ "def synonym(s,t){return this.helper.areSynonym(s,t);}\n"
						+ "def deriv(p,s,t){return this.helper.derivesInto(p,s,t);}\n"
						+ "def Boolean match(s, t) { %s }\n"
						+ "}", 
						newRuleClassName(rule.getName()),
						rule.getExpression());
				Class<?> groovyClass = getGroovyClassLoader().parseClass(script, rule.getName());
				GroovyObject groovyRule = (GroovyObject) groovyClass.newInstance();
				groovyRule.invokeMethod(
						GROOVY_SET_HELPER_METHOD_NAME, 
						new Object[] { groovyHelper });
				this.groovyRules.put(rule, groovyRule);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException("Could not load groovy expression as groovy object: " + rule.getExpression(), e);
			}
		}
		return this.groovyRules.get(rule);
	}


	public GroovyComponent asGroovyComponent(Component component) {
		if(!this.groovyComponents.containsKey(component))
			this.groovyComponents.put(component, new GroovyComponent(component));
		return this.groovyComponents.get(component);
	}
	
	public GroovyWord asGroovyWord(TermWord termWord) {
		if(!this.groovyWords.containsKey(termWord))
			this.groovyWords.put(termWord, new GroovyWord(termWord, this));
		return groovyWords.get(termWord);
	}
	
	public void clear() {
		this.groovyComponents.clear();
		this.groovyWords.clear();
		this.groovyTerms.clear();
	}

	public boolean matchesRule(VariantRule rule, Term source, Term target) {
		try {
			GroovyTerm s = asGroovyTerm(source);
			GroovyTerm t = asGroovyTerm(target);
			return (boolean) asGroovyRule(rule).invokeMethod(
				GROOVY_MATCH_METHOD_NAME, 
				new Object[] { s, t });
		} catch(IndexOutOfBoundsException e) {
			return false;
		} catch(InvokerInvocationException e) {
			LOGGER.error("An error occurred in groovy variant rule", e);
			throw new RuntimeException(e);
		} catch(Exception e) {
			LOGGER.warn("The variant rule {} throwed an exception: {}", rule.getName(), e.getClass());
			return false;
		}
	}

	private String newRuleClassName(String ruleName) {
		return CLASS_NUM++ + ruleName.replaceAll("-", "_").replaceAll("[^a-zA-Z]+", "");
	}
	
	private GroovyClassLoader groovyClassLoader;
	private GroovyClassLoader getGroovyClassLoader() {
		if(groovyClassLoader == null) {
			ClassLoader classLoader = VariantRule.class.getClassLoader();
			groovyClassLoader = new GroovyClassLoader(classLoader);
		}
		return groovyClassLoader;
	}

}
