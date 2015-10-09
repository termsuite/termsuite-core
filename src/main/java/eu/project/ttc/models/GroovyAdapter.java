package eu.project.ttc.models;

import java.util.Map;

import com.google.common.collect.Maps;

public class GroovyAdapter {

	private Map<TermWord, GroovyWord> groovyWords = Maps.newHashMap();
	private Map<Term, GroovyTerm> groovyTerms = Maps.newHashMap();
	private Map<Component, GroovyComponent> groovyComponents = Maps.newHashMap();

	public GroovyTerm asGroovyTerm(Term term) {
		if(this.groovyTerms.get(term) == null)
			this.groovyTerms.put(term, new GroovyTerm(term, this));
		return this.groovyTerms.get(term);
	}

	public GroovyComponent asGroovyComponent(Component component) {
		if(this.groovyComponents.get(component) == null)
			this.groovyComponents.put(component, new GroovyComponent(component));
		return this.groovyComponents.get(component);
	}
	
	public GroovyWord asGroovyWord(TermWord termWord) {
		if(this.groovyWords.get(termWord) == null)
			this.groovyWords.put(termWord, new GroovyWord(termWord, this));
		return groovyWords.get(termWord);
	}
	
	public void clear() {
		this.groovyComponents.clear();
		this.groovyWords.clear();
		this.groovyTerms.clear();
	}

}
