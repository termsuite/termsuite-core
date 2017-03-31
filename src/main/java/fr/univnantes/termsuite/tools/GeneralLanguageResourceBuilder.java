package fr.univnantes.termsuite.tools;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TXTCorpus;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

public class GeneralLanguageResourceBuilder {
		
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if(args.length!=4) {
			System.err.format("Require 4 arguments. Got %d arguments.%n", args.length);
			System.err.format("Usage: java -Xms1g -Xmx4g -cp termsuite-core-%s.jar %s"
					+ " [lang] [nb_lines] [/path/to/treetagger] [/path/to/general/language/corpus]",
					TermSuite.currentVersion(), GeneralLanguageResourceBuilder.class);
			System.exit(1);
		} else {
			Lang lang = Lang.forName(args[0]);
			int nbLines = Integer.parseInt(args[1]);
			Path treetaggerPath = Paths.get(args[2]);
			Path corpusPath = Paths.get(args[3]);
		
			System.out.format("Computing general corpus: %n\tLang: %s%n\tNb lines: %d%n\tTree Tagger: %s%n\tCorpus: %s%n",
					lang, 
					nbLines, 
					treetaggerPath, 
					corpusPath);
			
			IndexedCorpus corpus = TermSuite.preprocessor()
					.setTaggerPath(treetaggerPath)
					.toIndexedCorpus(
							new TXTCorpus(lang, corpusPath), 
							(int)(nbLines * 1.5),
							TermSuiteFactory.createEmptyOccurrenceStore(lang));
			
			List<Term> terms = Lists.newArrayList(corpus.getTerminology().getTerms().values());
			Collections.sort(terms, TermProperty.FREQUENCY.getComparator(true));

			File dest = Paths.get(lang.getName().toLowerCase() + "-general-language.txt").toFile();
			System.err.format("Exporting GeneralLanguage to file %s%n", dest);
			
			try(
				OutputStream stream = new FileOutputStream(dest);
				Writer writer = new OutputStreamWriter(stream)) {
				
				writer.write("__NB_CORPUS_WORDS__::option::" + corpus.getTerminology().getNbWordAnnotations().longValue());
				writer.write("\n");
				terms.stream()
					.limit(nbLines)
					.map(term -> String.format("%s::%s::%d", 
							term.getWords().stream().map(w->w.getWord().getLemma()).collect(joining(" ")),
							term.getWords().stream().map(tw -> tw.getSyntacticLabel()).collect(joining(" ")),
							term.getFrequency()
						))
					.forEach(line->{
						try {
							writer.write(line);
							writer.write("\n");
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
			}
		}
	}
}
