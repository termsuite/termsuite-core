package eu.project.ttc.resources;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import eu.project.ttc.tools.TermSuiteResourceManager;

/**
 * 
 * A UIMA Resource wrapper for java UIMA resource objects that are already 
 * in memory and stored in the TermSuiteResourceManager singleton.
 * 
 * @author Damien Cram
 *
 * @param <T> the type of the actual resource object to be loaded from memory
 * @see TermSuiteResourceManager
 */
public class TermSuiteMemoryUIMAResource<T> implements SharedResourceObject {

	private T resourceObject;
	
	@SuppressWarnings("unchecked")
	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		this.resourceObject = (T)TermSuiteResourceManager
				.getInstance().get(aData.getUri().toString());
	}
	
	public T getResourceObject() {
		return resourceObject;
	}
}
