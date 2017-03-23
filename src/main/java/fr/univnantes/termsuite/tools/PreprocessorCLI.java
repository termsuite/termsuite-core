package fr.univnantes.termsuite.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import fr.univnantes.termsuite.api.TXTCorpus;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.io.json.JsonOptions;
import fr.univnantes.termsuite.io.json.JsonTerminologyIO;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.tools.opt.TermSuiteCliOption;

public class PreprocessorCLI extends CommandLineClient { // NO_UCD (public entry point)
	
	public PreprocessorCLI() {
		super("Applies TermSuite's preprocessings to given text corpus.");
	}

	@Override
	public void configureOpts() {
		clientHelper.declareResourceOpts();
		clientHelper.declareHistory();
		declareFacultative(TermSuiteCliOption.TAGGER);
		declareMandatory(TermSuiteCliOption.TAGGER_PATH);
		declareMandatory(TermSuiteCliOption.FROM_TXT_CORPUS_PATH);
		declareMandatory(TermSuiteCliOption.LANGUAGE);
		declareFacultative(TermSuiteCliOption.ENCODING);
		declareAtLeastOneOf(
				TermSuiteCliOption.CAS_JSON, 
				TermSuiteCliOption.CAS_TSV,
				TermSuiteCliOption.CAS_XMI,
				TermSuiteCliOption.PREPARED_TERMINO_JSON);
	}


	@Override
	protected void run() throws IOException {
		fr.univnantes.termsuite.api.Preprocessor preprocessor = TermSuite.preprocessor();
		
		if(isSet(TermSuiteCliOption.TAGGER))
			preprocessor.setTagger(Tagger.forName(asString(TermSuiteCliOption.TAGGER)));
		
		preprocessor.setTaggerPath(asDir(TermSuiteCliOption.TAGGER_PATH));

		if(clientHelper.getHistory().isPresent()) 
			preprocessor.setHistory(clientHelper.getHistory().get());
		
		preprocessor.setResourceOptions(clientHelper.getResourceConfig());
		
		TXTCorpus txtCorpus = clientHelper.getTxtCorpus();

		if(isSet(TermSuiteCliOption.CAS_XMI))
			preprocessor.toXMI(asDir(TermSuiteCliOption.CAS_XMI));

		if(isSet(TermSuiteCliOption.CAS_TSV))
			preprocessor.toTSV(asDir(TermSuiteCliOption.CAS_TSV));

		if(isSet(TermSuiteCliOption.CAS_JSON))
			preprocessor.toJSON(asDir(TermSuiteCliOption.CAS_JSON));

		if(isSet(TermSuiteCliOption.PREPARED_TERMINO_JSON)) {
			Path destJson = asPath(TermSuiteCliOption.PREPARED_TERMINO_JSON);
			try(FileWriter writer = new FileWriter(destJson.toFile())) {
				IndexedCorpus corpus = preprocessor.toIndexedCorpus(txtCorpus, 500000);
				JsonTerminologyIO.save(writer, corpus, new JsonOptions());
			}
		} else 
			// consume
			preprocessor.asStream(txtCorpus).count();
	}
	

	public static void main(String[] args) {
		new PreprocessorCLI().runClient(args);
	}
}
