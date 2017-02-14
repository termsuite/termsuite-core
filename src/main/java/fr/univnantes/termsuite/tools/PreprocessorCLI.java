package fr.univnantes.termsuite.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.io.json.JsonOptions;
import fr.univnantes.termsuite.io.json.JsonTerminologyIO;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.model.TextCorpus;

public class PreprocessorCLI extends CommandLineClient { // NO_UCD (public entry point)
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreprocessorCLI.class);

	/** Short usage description of the CLI */
	private static final String USAGE = "java [-DconfigFile=<file>] -cp termsuite-core-x.x.jar fr.univnantes.termsuite.tools.PreProcessor";

	@Override
	protected void configureOpts() {
		declareResourceOpts();
		declareOptional(CliOption.TAGGER);
		declareMandatory(CliOption.TAGGER_PATH);
		declareMandatory(CliOption.CORPUS_PATH);
		declareMandatory(CliOption.LANGUAGE);
		declareOptional(CliOption.ENCODING);
		declareAtLeastOneOf(
				CliOption.CAS_JSON, 
				CliOption.CAS_TSV,
				CliOption.CAS_XMI,
				CliOption.PREPARED_TERMINO_JSON);
		declareOptional(CliOption.WATCH);
		
	}

	@Override
	protected void run() throws IOException {
		fr.univnantes.termsuite.api.Preprocessor preprocessor = TermSuite.preprocessor();
		
		if(isSet(CliOption.TAGGER))
			preprocessor.setTagger(Tagger.forName(asString(CliOption.TAGGER)));
		
		preprocessor.setTaggerPath(asDir(CliOption.TAGGER_PATH));

		if(isSet(CliOption.WATCH)) 
			preprocessor.setHistory(createHistory());
		
		TextCorpus txtCorpus = toTxtCorpus();

		if(isSet(CliOption.CAS_XMI))
			preprocessor.toXMI(asDir(CliOption.CAS_XMI));

		if(isSet(CliOption.CAS_TSV))
			preprocessor.toTSV(asDir(CliOption.CAS_TSV));

		if(isSet(CliOption.CAS_JSON))
			preprocessor.toJSON(asDir(CliOption.CAS_JSON));

		if(isSet(CliOption.PREPARED_TERMINO_JSON)) {
			Path destJson = asPath(CliOption.PREPARED_TERMINO_JSON);
			try(FileWriter writer = new FileWriter(destJson.toFile())) {
				IndexedCorpus corpus = preprocessor.toIndexedCorpus(txtCorpus, 500000);
				JsonTerminologyIO.save(writer, corpus, new JsonOptions());
			}
		} else 
			// consume
			preprocessor.asStream(txtCorpus).count();
	}
	
	private TextCorpus toTxtCorpus() {
		Path ascorpusPath = asDir(CliOption.CORPUS_PATH);
		return new TextCorpus(getLang(), ascorpusPath);
	}

	public static void main(String[] args) {
		new PreprocessorCLI().launch(args);
	}
}
