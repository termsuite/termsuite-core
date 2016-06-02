package eu.project.ttc.tools.actormodel;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.CasCreationUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/*
 * TODO TreeTagger external process dies when several threads call it.
 * TODO Needs on AE instance per thread, otherwise it crashes (this may solve the TrreTagger issue)
 * TODO Implements the parallelism using AKKA, but requires Java 8
 */
public class ActorSimplePipeline {

	private static ActorAnalysisEngine firstEngine;
	

	public static void runPipeline(final CollectionReaderDescription crDescription, final AggregatedAEActorDescription desc)
			throws UIMAException, IOException {
		Preconditions.checkArgument(
				desc.getAeActorDescriptions().size() > 0,
				"You should provide at least 1 AE");
		ResourceManager resourceManager = ResourceManagerFactory.newResourceManager();
		List<ActorAnalysisEngine> engines = Lists.newArrayList();
		for (AEActorDescription d: desc.getAeActorDescriptions()) {
			engines.add(new ActorAnalysisEngine(
					UIMAFramework.produceAnalysisEngine(
							d.getAeDescription(), 
							resourceManager,
							null), 
					d.getNbThreads()));
		}
		
		// builds the chain
		firstEngine = engines.get(0);	
		for (int i = 1; i< engines.size(); i++) 
			engines.get(i-1).setNextEngine(engines.get(i));
		

		CollectionReader cr = UIMAFramework.produceCollectionReader(crDescription, resourceManager, null);

		// Create CAS from merged metadata

		try {
			// Process
			while (cr.hasNext()) {
				
				final CAS cas = CasCreationUtils.createCas(asList(cr.getMetaData()));
				cr.typeSystemInit(cas.getTypeSystem());
				cr.getNext(cas);
				firstEngine.process(cas.getJCas());
			}
			
			firstEngine.awaitsAllEngines();

			// Signal end of processing
			for(ActorAnalysisEngine engine:engines) 
				engine.collectionProcessComplete();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			for(ActorAnalysisEngine engine:engines) 
				engine.destroy();
		}
	}

}
