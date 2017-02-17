package fr.univnantes.termsuite.framework.modules;

import org.apache.uima.analysis_engine.AnalysisEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import fr.univnantes.termsuite.framework.service.PreprocessorService;
import fr.univnantes.termsuite.model.Lang;

public class PreprocessingModule extends AbstractModule {
	
	private Lang lang;
	private AnalysisEngine engine;
	
	public PreprocessingModule(Lang lang, AnalysisEngine engine) {
		super();
		this.lang = lang;
		this.engine = engine;
	}

	@Override
	protected void configure() {
		bind(Lang.class).toInstance(lang);
		bind(AnalysisEngine.class).toInstance(engine);
		bind(PreprocessorService.class).in(Singleton.class);
	}
}
