package fr.univnantes.termsuite.framework.injector;

import java.lang.reflect.Field;

import fr.univnantes.termsuite.framework.Index;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;

public class IndexInjector extends TermSuiteInjector {

	private IndexService indexService;

	public IndexInjector(IndexService indexService) {
		super();
		this.indexService = indexService;
	}
	
	public  void injectIndexes(Object object) {
		for(Field field:getAnnotatedFields(object, Index.class, TermIndex.class)) {
			Index annotation = field.getAnnotation(Index.class);
			TermIndexType indexType = annotation.type();
			injectField(field, object, indexService.getIndex(indexType));
		}
	}
}
