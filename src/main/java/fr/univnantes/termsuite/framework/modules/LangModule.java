package fr.univnantes.termsuite.framework.modules;

import com.google.inject.AbstractModule;

import fr.univnantes.termsuite.model.Lang;

public class LangModule extends AbstractModule {

	private Lang lang;
	
	public LangModule(Lang lang) {
		this.lang = lang;
	}

	@Override
	protected void configure() {
		bind(Lang.class).toInstance(lang);
	}
}
