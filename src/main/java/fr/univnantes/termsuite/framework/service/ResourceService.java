package fr.univnantes.termsuite.framework.service;

import javax.inject.Inject;

import fr.univnantes.termsuite.model.Lang;

public class ResourceService {

	Lang lang;

	@Inject
	public ResourceService(Lang lang) {
		super();
		this.lang = lang;
	}
	
	
}
