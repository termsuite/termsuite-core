package eu.project.ttc.tools.builders;

import java.io.InputStreamReader;
import java.net.URL;

import com.google.common.base.Charsets;

import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.JSONTermIndexIO;
import eu.project.ttc.models.index.io.LoadOptions;

public class TermIndexIO {
	
	public static void toJson(TermIndex termIndex) {
		
	}
	
	public static void toJson(TermIndex termIndex, JSONOptions options) {
		
	}

	public static void toTbx(TermIndex termIndex) {
		
	}

	public static void toTsv(TermIndex termIndex) {
		
	}

	public static void toTsv(TermIndex termIndex, TSVOptions options) {
		
	}

	public static TermIndex fromJson(URL termIndexUrl) {
		return fromJson(termIndexUrl, new JSONOptions());
	}

	public static TermIndex fromJson(URL termIndexUrl, JSONOptions options) {
		LoadOptions options2 = new LoadOptions();
		try {
			return JSONTermIndexIO.load(
				new InputStreamReader(termIndexUrl.openStream(), Charsets.UTF_8),
				options2
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
}
