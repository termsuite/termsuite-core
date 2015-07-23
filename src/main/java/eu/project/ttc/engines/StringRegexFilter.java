package eu.project.ttc.engines;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.project.ttc.types.WordAnnotation;
import fr.univnantes.lina.UIMAProfiler;

public class StringRegexFilter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringRegexFilter.class);

	private static final Pattern[] PATTERNS = {
		// url
		Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
	};

	private int totalFiltered;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.totalFiltered = 0;
	}
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		List<WordAnnotation> rem = Lists.newArrayList();
		FSIterator<Annotation> it = cas.getAnnotationIndex(WordAnnotation.type).iterator();
		WordAnnotation word;
		while(it.hasNext()) {
			word = (WordAnnotation) it.next();
			for(Pattern p:PATTERNS) 
				if(p.matcher(word.getCoveredText()).matches())
					rem.add(word);
		}
		
		this.totalFiltered += rem.size();
		
		for(WordAnnotation wa:rem)
			wa.removeFromIndexes(cas);
		
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Number of terms filtered: {}", this.totalFiltered);
	}
}
