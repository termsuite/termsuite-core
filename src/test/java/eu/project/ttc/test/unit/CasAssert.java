package eu.project.ttc.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.util.Lists;

import com.google.common.base.Joiner;

public class CasAssert extends AbstractAssert<CasAssert, JCas> {

	public CasAssert(JCas actual) {
		super(actual, CasAssert.class);
	}
	
	public CasAssert containsAnnotation(Class<? extends Annotation> annotationClass, int begin, int end) {
		List<Annotation> closedAnnotations = Lists.newArrayList();
		for(Annotation a:getAnnotationList(annotationClass)) {
			if(annotationClass.isInstance(a)
					&& a.getBegin() == begin && a.getEnd() == end)
				return this;
			else {
				/*
				 * Adds this annotation to possible annotations if indexes are close
				 */
				if(a.getBegin() >= begin - 30 && a.getEnd() <= end + 30)
					closedAnnotations.add(a);
			}
		}
		failWithMessage("Expected to contain annotation <%s[%s,%s]> but does not contain it. Close annotations: <%s>",
				annotationClass.getSimpleName(),begin, end,
				toString(closedAnnotations)
			);
		return this;
	}

	private String toString(List<Annotation> closedAnnotations) {
		List<String> strings = new ArrayList<>();
		for(Annotation a:closedAnnotations)
			strings.add(toString(a));
		return Joiner.on(", ").join(strings);
	}

	private String toString(Annotation a) {
		return String.format("%s[%d,%d]{%s}", 
				a.getClass().getSimpleName(),
				a.getBegin(), a.getEnd(),
				a.getCoveredText());
	}

	public AbstractListAssert<?, ? extends List<? extends Annotation>, Annotation> getAnnotations(Class<? extends Annotation> annotationClass) {
		return assertThat(getAnnotationList(annotationClass));
	}

	private List<Annotation> getAnnotationList(Class<?>... annotationClasses) {
		List<Annotation> list = Lists.newArrayList();
		FSIterator<Annotation> it = actual.getAnnotationIndex().iterator();
		while(it.hasNext()) {
			Annotation a = it.next();
			if(annotationClasses.length == 0)
				list.add(a);
			else 
				for(Class<?> cls:annotationClasses)
					if(cls.isInstance(a))
						list.add(a);
		}
		return list;
	}

	public CasAssert doesNotContainAnnotation(Class<? extends Annotation> annotationClass, int begin, int end) {
		for(Annotation a:getAnnotationList(annotationClass)) 
			if(annotationClass.isInstance(a)
					&& a.getBegin() == begin && a.getEnd() == end)
				failWithMessage("Expected to not contain annotation <%s[%s,%s]> but actually contains it:  <%s>",
						annotationClass.getSimpleName(),begin, end,
						a.getCoveredText()
						);
		return this;
	}

	public CasAssert doesNotContainAnnotation(Class<? extends Annotation> annotationClass) {
		for(Annotation a:getAnnotationList(annotationClass)) 
			if(annotationClass.isInstance(a))
				failWithMessage("Expected to not contain any annotation of class <%s> but actually contains at least this one:  <%s>",
						annotationClass.getSimpleName(),
						a.getCoveredText()
						);
		return this;
	}

}
