package fr.univnantes.termsuite.framework.modules;

import java.util.Optional;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import fr.univnantes.termsuite.framework.SourceLanguage;
import fr.univnantes.termsuite.framework.TargetLanguage;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.resources.BilingualDictionary;

public class AlignerModule extends AbstractModule{

	private BilingualDictionary dico;
	private Optional<Class<? extends SimilarityDistance>> distance;
	private TerminologyService sourceTermino;
	private TerminologyService targetTermino;
	private IndexService sourceIndexes;
	private IndexService targetIndexes;
	
	public AlignerModule(Injector source, Injector target, BilingualDictionary dico, Class<? extends SimilarityDistance> distance) {
		super();
		this.sourceTermino = source.getInstance(TerminologyService.class);
		this.targetTermino = target.getInstance(TerminologyService.class);
		this.sourceIndexes = source.getInstance(IndexService.class);
		this.targetIndexes = target.getInstance(IndexService.class);
		this.distance = Optional.ofNullable(distance);
		this.dico = dico;
	}
	
	@Override
	protected void configure() {
		bind(IndexService.class).annotatedWith(SourceLanguage.class).toProvider(() ->{return AlignerModule.this.sourceIndexes;});
		bind(IndexService.class).annotatedWith(TargetLanguage.class).toProvider(() ->{return AlignerModule.this.targetIndexes;});
		bind(TerminologyService.class).annotatedWith(SourceLanguage.class).toProvider(() ->{return AlignerModule.this.sourceTermino;});
		bind(TerminologyService.class).annotatedWith(TargetLanguage.class).toProvider(() ->{return AlignerModule.this.targetTermino;});
		bind(Lang.class).annotatedWith(SourceLanguage.class).toInstance(sourceTermino.getLang());
		bind(Lang.class).annotatedWith(TargetLanguage.class).toInstance(targetTermino.getLang());
		bind(BilingualDictionary.class).toInstance(dico);
		if(distance.isPresent())
			bind(distance.get()).in(Singleton.class);
	}
}
