package fr.univnantes.termsuite.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ExporterModule;
import fr.univnantes.termsuite.framework.modules.IndexedCorpusModule;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;

public class BaseIndexedCorpusExporter implements IndexedCorpusExporter {

	private Object exporter;
	
	public BaseIndexedCorpusExporter(Object exporter) {
		super();
		this.exporter = exporter;
	}

	@Override
	public void export(IndexedCorpus corpus, Writer writer) {
		Method exportMethod = getExportMethod();
		List<Object> parameters = new ArrayList<>();
		Injector injector = Guice.createInjector(new IndexedCorpusModule(corpus), new ExporterModule(writer));
		for(Parameter param:exportMethod.getParameters()) {
			parameters.add(injector.getInstance(param.getType()));
		}
		try {
			exportMethod.invoke(exporter, parameters.toArray());
		} catch (IllegalAccessException e) {
			throw new TermSuiteException("Export method should be accessible (public).", e);
		} catch (InvocationTargetException|IllegalArgumentException e) {
			throw new TermSuiteException(e);
		}
	}
	
	private Method getExportMethod() {
		List<Method> methods = new ArrayList<>();
		Class<?> cls = exporter.getClass();
		while(cls != null) {
			Arrays.stream(cls.getDeclaredMethods())
				.filter(m -> m.isAnnotationPresent(Export.class))
				.forEach(methods::add);
			cls = cls.getSuperclass();
		}
		Preconditions.checkArgument(methods.size()>0,"No method annotated with @%s found in %s class", Export.class.getSimpleName(), exporter.getClass());
		Preconditions.checkArgument(methods.size()<2,"Only one method annotated with @%s allowed in %s class", Export.class.getSimpleName(), exporter.getClass());
		return methods.get(0);
	}

	@Override
	public void export(Terminology termino, Writer writer) {
		export(toIndexedCorpus(termino), writer);
	}

	private IndexedCorpus toIndexedCorpus(Terminology termino) {
		return TermSuiteFactory.createIndexedCorpus(termino, TermSuiteFactory.createEmptyOccurrenceStore(termino.getLang()));
	}

	@Override
	public String exportToString(IndexedCorpus corpus) {
		StringWriter writer = new StringWriter();
		export(corpus, writer);
		return writer.toString();
	}

	@Override
	public String exportToString(Terminology termino) {
		return exportToString(toIndexedCorpus(termino));
	}

	@Override
	public void export(IndexedCorpus corpus, Path path) throws IOException {
		try(FileWriter writer = new FileWriter(path.toFile())) {
			export(corpus, writer);
		} 
	}
	
	@Override
	public void export(Terminology termino, Path path) throws IOException {
		export(toIndexedCorpus(termino), path);
	}
}
