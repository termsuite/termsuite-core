package fr.univnantes.termsuite.utils;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.univnantes.termsuite.api.TermSuiteException;

public class JsonConfigObject {

	@Override
	public String toString() {
		try {
			StringWriter writer = new StringWriter();
			JsonGenerator jg = new JsonFactory().createGenerator(writer);
			jg.useDefaultPrettyPrinter();
			new ObjectMapper().writeValue(jg, this);
			return writer.getBuffer().toString();
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
