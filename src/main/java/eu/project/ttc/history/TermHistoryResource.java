package eu.project.ttc.history;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import eu.project.ttc.tools.TermSuiteResourceManager;

public class TermHistoryResource implements SharedResourceObject {

	public static final String TERM_HISTORY = "TermHistory";
	
	private TermHistory history;
	
	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		this.history = (TermHistory)TermSuiteResourceManager
				.getInstance().get(aData.getUri().toString());
		
	}

	public TermHistory getHistory() {
		return history;
	}
}
