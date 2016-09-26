package eu.project.ttc.api;

import eu.project.ttc.engines.desc.Lang;

public class Document {
	
	private Lang lang;
	private String url;
	private String text;
	
	
	public Document(Lang lang, String url, String text) {
		super();
		this.lang = lang;
		this.url = url;
		this.text = text;
	}

	public String getUrl() {
		return url;
	}
	
	public String getText() {
		return text;
	}
	
	public Lang getLang() {
		return lang;
	}
}
