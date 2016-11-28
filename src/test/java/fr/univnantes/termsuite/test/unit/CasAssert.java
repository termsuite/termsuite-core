
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

package fr.univnantes.termsuite.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.util.Lists;

import com.google.common.base.Joiner;

import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.utils.JCasUtils;

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

	public CasAssert hasUrl(String expected) {
		Optional<SourceDocumentInformation> sdi = JCasUtils.getSourceDocumentAnnotation(actual);
		if(sdi.isPresent()) {
			if(sdi.get().getUri().equals(expected))
				return this;
			else 
				failWithMessage("Expected SDI uri <%s>, but got <%s>", expected, sdi.get().getUri());
		} else
			failWithMessage("Expected cas to have SourceDocumentInformation annotation, but does not have it.");
		return this;
	}

}
