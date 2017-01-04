package fr.univnantes.termsuite.model;

import java.util.Objects;

import com.google.common.collect.ComparisonChain;

/**
 * 
 * An occurrence form of a term
 * 
 * @author Damien Cram
 *
 */
public class Form implements Comparable<Form> {

	private String text;
	private int count;
	
	public Form(String text) {
		super();
		this.text = text;
		this.count = 0;
	}

	public String getText() {
		return text;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Form) {
			Form o = (Form) obj;
			return Objects.equals(this.text, o.text) && Objects.equals(this.count, o.count);
		} else
			return false;
	}
	
	@Override
	public int compareTo(Form o) {
		return ComparisonChain.start()
				.compare(o.count, this.count)
				.compare(this.text, o.text)
				.result();
	}
	
	@Override
	public int hashCode() {
		return text.hashCode();
	}
	
	public int getCount() {
		return count;
	}
	
	public Form setCount(int count) {
		this.count = count;
		return this;
	}
}
