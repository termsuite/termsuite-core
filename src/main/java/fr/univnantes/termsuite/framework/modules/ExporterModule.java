package fr.univnantes.termsuite.framework.modules;

import java.io.Writer;

import com.google.inject.AbstractModule;

public class ExporterModule extends AbstractModule {

	private Writer writer;

	public ExporterModule(Writer writer) {
		super();
		this.writer = writer;
	}
	
	@Override
	protected void configure() {
		bind(Writer.class).toInstance(writer);
	}
}
