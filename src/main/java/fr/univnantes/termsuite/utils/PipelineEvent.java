package fr.univnantes.termsuite.utils;

import java.util.Date;

public class PipelineEvent {
	
	private String term;
	private Class<?> source;
	private String message;
	private Date date;
	
	private PipelineEvent(String term, Class<?> source, String message) {
		super();
		this.term = term;
		this.source = source;
		this.message = message;
		this.date = new Date();
	}
	
	public static PipelineEvent create(String term, Class<?> source, String message) {
		return new PipelineEvent(term, source, message);
	}

	public String getTermString() {
		return term;
	}

	public Class<?> getSource() {
		return source;
	}

	public String getMessage() {
		return message;
	}

	public Date getDate() {
		return date;
	}
	
}
