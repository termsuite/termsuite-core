/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.engines;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import eu.project.ttc.types.WordAnnotation;

/**
 * Tagger for Latvian
 * 
 * @author Unknown
 *
 */
public class LatvianTildeTagger extends JCasAnnotator_ImplBase {
	
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		try {
			AnnotationIndex<Annotation> index = cas.getAnnotationIndex(WordAnnotation.type);
			FSIterator<Annotation> iterator = index.iterator();
			while (iterator.hasNext()) {
				WordAnnotation annotation = (WordAnnotation) iterator.next();
				String tag = annotation.getTag().toLowerCase();
				this.setCategory(annotation, tag);
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void setCategory(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(0);
		if (ch == 'n') {
			annotation.setCategory("noun");
			annotation.setSubCategory("common"); // FIXME
			this.setNumber(annotation, tag);
			this.setGender(annotation, tag);
			this.setCase(annotation, tag);
		} else if (ch == 'v') {
			annotation.setCategory("verb");
			this.setMood(annotation, tag);
			this.setTense(annotation, tag);
			this.setPerson(annotation, tag);
			this.setNumber(annotation, tag);
			this.setGender(annotation, tag);
			this.setCase(annotation, tag);
		} else if (ch == 'a') {
			annotation.setCategory("adjective");
			this.setNumber(annotation, tag);
			this.setGender(annotation, tag);
			this.setCase(annotation, tag);
		} else if (ch == 'p') {
			annotation.setCategory("pronoun");
			this.setPronoun(annotation, tag);
		} else if (ch == 'r') {
			annotation.setCategory("adverb");
			annotation.setSubCategory("general");
		} else if (ch == 'g') {
			annotation.setCategory("verb");
			annotation.setMood("participle"); 
			this.setTense(annotation, tag);
			this.setGender(annotation, tag);
			this.setNumber(annotation, tag);
			this.setCase(annotation, tag);
		} else if (ch == 'm') {
			annotation.setCategory("numeral");
			this.setNumeral(annotation, tag);
		} else if (ch == 'i') {
			annotation.setCategory("interjection");
		} else if (ch == 'q') {
			annotation.setCategory("adverb");
			annotation.setSubCategory("particle");
		} else if (ch == 's') {
			annotation.setCategory("adposition");
			annotation.setSubCategory("preposition");
		} else if (ch == 'c') {
			annotation.setCategory("conjunction");
			this.setConjunction(annotation, tag);
		} else if (ch == 'y') {
			annotation.setCategory("abbreviation");
		} else if (ch == 'd') {
			annotation.setCategory("numeral");
		} else if (ch == 't') {
			annotation.setCategory("punctuation");
		} else {
			annotation.setCategory("noun");
			annotation.setSubCategory("proper");
		}
		
	}

	private void setCase(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(4);
		if (ch == 'n') {
			annotation.setCase("nominative");
		} else if (ch == 'g') {
			annotation.setCase("genitive");
		} else if (ch == 'd') {
			annotation.setCase("dative");
		} else if (ch == 'a') {
			annotation.setCase("accusative");
		} else if (ch == 'l') {
		} else if (ch == 'v') {
		}
	}

	private void setGender(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(2);
		if (ch == 'm') {
			annotation.setGender("masculine");
		} else if (ch == 'f') {
			annotation.setGender("feminine");
		}
	}

	private void setNumber(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(3);
		if (ch == 's') {
			annotation.setNumber("singular");
		} else if (ch == 'p') {
			annotation.setNumber("plural");
		}
	}

	private void setPerson(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(6);
		if (ch == '1') {
			annotation.setPerson("first");
		} else if (ch == '2') {
			annotation.setPerson("second");
		} else if (ch == '3') {
			annotation.setPerson("third");
		}
	}

	private void setTense(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(1);
		if (ch == 'p') {
			annotation.setTense("present");
		} else if (ch == 's') {
			annotation.setTense("past");
		} else if (ch == 'f') {
			annotation.setTense("future");
		}
	}

	private void setMood(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(9);
		if (ch == 'i') {
			annotation.setMood("indicative");
		} else if (ch == 'm') {
			annotation.setMood("imperative");
		} else if (ch == 's') {
			annotation.setMood("subjunctive");
		} else if (ch == 'q') {
		} else if (ch == 'c') {
		} else if (ch == 'n') {
			annotation.setMood("infinitive");
		} 
	}

	private void setConjunction(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(22);
		if (ch == 'c') {
			annotation.setSubCategory("coordinating");
		} else if (ch == 's') {
			annotation.setSubCategory("subordinating");
		}
	}

	private void setNumeral(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(8);
		if (ch == 'c') {
			annotation.setSubCategory("cardinal");
		} else if (ch == 'o') {
			annotation.setSubCategory("ordinal");
		}
	}

	private void setPronoun(WordAnnotation annotation, String tag) {
		char ch = tag.charAt(12);
		if (ch == 'p') {
			annotation.setSubCategory("personal");
		} else if (ch == 'x') {
			annotation.setSubCategory("reflexive");
		} else if (ch == 's') {
			annotation.setSubCategory("possessive");
		} else if (ch == 'd') {
			annotation.setSubCategory("demonstrative");
		} else if (ch == 'i') {
			annotation.setSubCategory("indefinite");
		} else if (ch == 'q') {
			annotation.setSubCategory("interrogative");
		} else if (ch == 'r') {
			annotation.setSubCategory("relative");
		} else if (ch == 'g') {
		} else if (ch == 'm') {
		} else if (ch == 'z') {
		}
	}

}
