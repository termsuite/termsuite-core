package fr.univnantes.termsuite.framework;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;

public class LanguageModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(LanguageModule.class).in(Singleton.class);
	}
}
