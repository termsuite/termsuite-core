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
	private IndexedCorpus indexedterminology;
	private Optional<TermHistory> history = Optional.empty();
	
	public IndexedCorpusModule(IndexedCorpus terminology) {
		Preconditions.checkNotNull(terminology, "Terminology cannot be null");
		this.indexedterminology = terminology;
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
		bind(Terminology.class).toInstance(indexedterminology.getTerminology());
		bind(IndexedCorpus.class).toInstance(indexedterminology);
		bind(Lang.class).toInstance(indexedterminology.getTerminology().getLang());
		bind(OccurrenceStore.class).toInstance(indexedterminology.getOccurrenceStore());
		bind(TerminologyService.class).in(Singleton.class);
		bind(PipelineService.class).in(Singleton.class);
		bind(IndexService.class).in(Singleton.class);
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
