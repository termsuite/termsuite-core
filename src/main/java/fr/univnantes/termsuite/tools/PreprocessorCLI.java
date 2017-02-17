package fr.univnantes.termsuite.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.io.json.JsonOptions;
import fr.univnantes.termsuite.io.json.JsonTerminologyIO;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.model.TextCorpus;
import fr.univnantes.termsuite.tools.opt.CliOption;

public class PreprocessorCLI extends CommandLineClient { // NO_UCD (public entry point)
	
	public PreprocessorCLI() {
		super("Applies TermSuite's preprocessings to given text corpus");
	}

	@Override
	public void configureOpts() {
		clientHelper.declareResourceOpts();
		clientHelper.declareHistory();
		declareFacultative(CliOption.TAGGER);
		declareMandatory(CliOption.TAGGER_PATH);
		declareMandatory(CliOption.FROM_TXT_CORPUS_PATH);
		declareMandatory(CliOption.LANGUAGE);
		declareFacultative(CliOption.ENCODING);
		declareAtLeastOneOf(
				CliOption.CAS_JSON, 
				CliOption.CAS_TSV,
				CliOption.CAS_XMI,
				CliOption.PREPARED_TERMINO_JSON);
	}


	@Override
	protected void run() throws IOException {
		fr.univnantes.termsuite.api.Preprocessor preprocessor = TermSuite.preprocessor();
		
		if(isSet(CliOption.TAGGER))
			preprocessor.setTagger(Tagger.forName(asString(CliOption.TAGGER)));
		
		preprocessor.setTaggerPath(asDir(CliOption.TAGGER_PATH));

		if(clientHelper.getHistory().isPresent()) 
			preprocessor.setHistory(clientHelper.getHistory().get());
		
		preprocessor.setResourceOptions(clientHelper.getResourceConfig());
		
		TextCorpus txtCorpus = clientHelper.getTxtCorpus();

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
	

	public static void main(String[] args) {
		new PreprocessorCLI().runClient(args);
	}
}
