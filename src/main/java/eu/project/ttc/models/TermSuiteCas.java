package eu.project.ttc.models;

import java.util.Iterator;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.utils.IteratorUtils;
import eu.project.ttc.utils.JCasUtils;

/**
 * A wrapper for a {@link JCas} aiming at providing an easy access API
 * to TermSuite annotations for end-users.
 * 
 * @author Damien Cram
 *
 */
public class TermSuiteCas {
	
	private JCas uimaCas;
	
	public TermSuiteCas(JCas uimaCas) {
		super();
		this.uimaCas = uimaCas;
	}

	/**
	 * 
	 * @return the inner UIMA {@link JCas} object
	 */
	public JCas getUimaCas() {
		return this.uimaCas;
	}
	
	public SourceDocumentInformation getSourceDocumentInformation() {
		return JCasUtils.getSourceDocumentAnnotation(this.uimaCas).get();
	}
	
	public Iterable<WordAnnotation> getWordAnnotations() {
		return IteratorUtils.toIterable(wordAnnotationIt());
	}
	public Iterable<WordAnnotation> getSubWordAnnotations(TermOccAnnotation termOcc) {
		return JCasUtil.subiterate(getUimaCas(), WordAnnotation.class, termOcc, false, true);
	}

	public Iterator<WordAnnotation> wordAnnotationIt() {
		return annoIterator(WordAnnotation.type);
	}

	@SuppressWarnings("unchecked")
	private <T> Iterator<T> annoIterator(int type) {
		return (Iterator<T>)getUimaCas().getAnnotationIndex(type).iterator();
	}
	
	public Iterable<TermOccAnnotation> getTermOccAnnotations() {
		return IteratorUtils.toIterable(termOccAnnotationIt());
	}
	public Iterable<TermOccAnnotation> getSubTermOccAnnotations(TermOccAnnotation termOcc) {
		return JCasUtil.subiterate(getUimaCas(), TermOccAnnotation.class, termOcc, false, true);
	}
	
	public Iterator	<TermOccAnnotation> termOccAnnotationIt() {
		return annoIterator(TermOccAnnotation.type);		
	}

}
