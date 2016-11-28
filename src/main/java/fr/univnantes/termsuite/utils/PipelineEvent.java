package fr.univnantes.termsuite.utils;

import java.util.Date;

public class PipelineEvent {
	
	private String termKey;
	private Class<?> source;
	private String message;
	private Date date;
	
	private PipelineEvent(String termKey, Class<?> source, String message) {
		super();
		this.termKey = termKey;
		this.source = source;
		this.message = message;
		this.date = new Date();
	}
	
	public static PipelineEvent create(String termKey, Class<?> source, String message) {
		return new PipelineEvent(termKey, source, message);
	}

	public String getTermKey() {
		return termKey;
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
