package fr.univnantes.termsuite.api;

import fr.univnantes.termsuite.model.Document;

public interface DocumentTextProvider {
	public String readText(Document document);
}
