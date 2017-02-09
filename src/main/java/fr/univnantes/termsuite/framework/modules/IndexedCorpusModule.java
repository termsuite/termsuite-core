package fr.univnantes.termsuite.framework.modules;

import java.lang.reflect.Field;
import java.util.Optional;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import fr.univnantes.termsuite.engines.gatherer.GroovyService;
import fr.univnantes.termsuite.framework.PipelineStats;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.PipelineService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.utils.TermHistory;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.filter.resources.FilterResource;

public class IndexedCorpusModule extends AbstractModule {
	private IndexedCorpus corpus;
	private Optional<TermHistory> history = Optional.empty();
	
	public IndexedCorpusModule(IndexedCorpus indexedCorpus) {
		Preconditions.checkNotNull(indexedCorpus, "Terminology cannot be null");
		this.corpus = indexedCorpus;
	}

	public IndexedCorpusModule(IndexedCorpus terminology, 
			TermHistory history) {
		this(terminology);
		this.history = Optional.ofNullable(history);
	}

	@Override
	protected void configure() {
		bind(new TypeLiteral<Optional<TermHistory>>(){}).toInstance(history);
		bind(FilterResource.class).to(DefaultFilterResource.class);
		bind(Terminology.class).toInstance(corpus.getTerminology());
		bind(IndexedCorpus.class).toInstance(corpus);
		bind(Lang.class).toInstance(corpus.getTerminology().getLang());
		bind(OccurrenceStore.class).toInstance(corpus.getOccurrenceStore());
		bind(TerminologyService.class).toInstance(new TerminologyService(corpus.getTerminology()));
		bind(PipelineService.class).in(Singleton.class);
		bind(IndexService.class).toInstance(new IndexService(corpus.getTerminology()));
		bind(GroovyService.class).in(Singleton.class);
		bind(PipelineStats.class).in(Singleton.class);
	    bindListener(Matchers.any(), new Slf4JTypeListener());
	}
	
	private static class Slf4JTypeListener implements TypeListener {
	    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
	        Class<?> clazz = typeLiteral.getRawType();
	        while (clazz != null) {
	          for (Field field : clazz.getDeclaredFields()) {
	            boolean injectAnnoPresent = 
	            		field.isAnnotationPresent(fr.univnantes.termsuite.framework.InjectLogger.class);
				if (field.getType() == Logger.class &&
	              injectAnnoPresent) {
	              typeEncounter.register(new Slf4JMembersInjector<T>(field));
	            }
	          }
	          clazz = clazz.getSuperclass();
	        }
	      }
	    }
	
	private static class Slf4JMembersInjector<T> implements MembersInjector<T> {
	    private final Field field;
	    private final Logger logger;

	    Slf4JMembersInjector(Field field) {
	      this.field = field;
	      this.logger = LoggerFactory.getLogger(field.getDeclaringClass());
	      field.setAccessible(true);
	    }

	    public void injectMembers(T t) {
	      try {
	        field.set(t, logger);
	      } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	      }
	    }
	  }	
	
}
