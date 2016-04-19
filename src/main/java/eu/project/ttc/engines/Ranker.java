package eu.project.ttc.engines;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.TermIndexResource;

public class Ranker extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(Ranker.class);
	public static final String TASK_NAME = "Ranking terms";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	public static final String RANKING_PROPERTY="RankingProperty";
	@ConfigurationParameter(name=RANKING_PROPERTY, mandatory=true)
	protected TermProperty rankingProperty;

	public static final String DESC="Desc";
	@ConfigurationParameter(name=DESC, mandatory=false, defaultValue="false")
	protected boolean reverse;

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		/*
		 * Do nothing
		 */
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.info("Starting " + TASK_NAME);
		List<Term> ranked = Lists.newArrayList(termIndexResource.getTermIndex().getTerms());
		Comparator<Term> comparator = rankingProperty.getComparator(termIndexResource.getTermIndex(), reverse);
		Collections.sort(ranked, comparator);
		for(int index = 0; index < ranked.size(); index++) 
			ranked.get(index).setRank(index + 1);
	}
}
