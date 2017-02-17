package fr.univnantes.termsuite.framework;

import java.io.IOException;
import java.io.Reader;

public interface TermSuiteResource {

	void load(Reader reader) throws IOException;
}
