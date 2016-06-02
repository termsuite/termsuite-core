package eu.project.ttc.tools.actormodel;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.assertj.core.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.utils.JCasUtils;

public class ActorAnalysisEngine  {

	private AnalysisEngine engine;
	private ExecutorService executor;
	private ActorAnalysisEngine nextEngine;
	
	private class ProcessTask implements Runnable {
		
		private JCas cas;
		
		
		public ProcessTask(JCas cas) {
			super();
			this.cas = cas;
		}


		@Override
		public void run() {
			try {
				getLogger().debug("Entering #process({})", getCasName(cas));
				ActorAnalysisEngine.this.engine.process(cas);
//				getLogger().debug("Exiting #process");
				if(ActorAnalysisEngine.this.nextEngine != null) {
					getLogger().debug("Passing CAS {} to the next AE: {}", getCasName(cas), nextEngine.engine.getMetaData().getName());					
					ActorAnalysisEngine.this.nextEngine.process(this.cas);
				}
			} catch (AnalysisEngineProcessException e) {
				throw new RuntimeException(e);
			}
		} 
	}
	
	
	private static final Map<JCas, String> casNames = Maps.newHashMap();
	
	private String getCasName(JCas cas) {
		if(casNames.get(cas) ==  null) {
			casNames.put(cas, JCasUtils.getSourceDocumentAnnotation(cas).get().getUri());
		}
			
		return casNames.get(cas);
	}
	
	public ActorAnalysisEngine(AnalysisEngine engine, int nbThreads) {
		super();
		this.engine = engine;
		this.executor = Executors.newFixedThreadPool(nbThreads);
	}
	
	public void setNextEngine(ActorAnalysisEngine nextEngine) {
		this.nextEngine = nextEngine;
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		getLogger().debug("Posting the #process({}) task", getCasName(jCas));
		executor.execute(new ProcessTask(jCas));
	}

	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		engine.collectionProcessComplete();
	}

	public void destroy() {
		engine.destroy();
	}
	

	private Logger logger;
	private Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(this.engine.getAnalysisEngineMetaData().getName());
			((ch.qos.logback.classic.Logger)logger).setLevel(ch.qos.logback.classic.Level.INFO);
		}
		return logger;
		
	}
	public void awaitsAllEngines() throws InterruptedException {
		
		getLogger().info("Waiting for process termination");
		this.executor.shutdown();
		this.executor.awaitTermination(10, TimeUnit.SECONDS);
		getLogger().debug("Processed have terminated");
		if(this.nextEngine != null)
			this.nextEngine.awaitsAllEngines();
	}
}
