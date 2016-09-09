package eu.project.ttc.tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ExternalResourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.tools.cli.TermSuiteCLIUtils;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.utils.StringUtils;
import fr.univnantes.lina.uima.engines.TreeTaggerWrapper;
import fr.univnantes.lina.uima.models.TreeTaggerParameter;
import uima.sandbox.lexer.engines.Lexer;
import uima.sandbox.lexer.resources.SegmentBank;
import uima.sandbox.lexer.resources.SegmentBankResource;

public class PreProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreProcessor.class);

	/** Short usage description of the CLI */
	private static final String USAGE = "java [-DconfigFile=<file>] -cp termsuite-core-x.x.jar eu.project.ttc.tools.PreProcessor";

	
	private Charset charset = Charset.forName("UTF-8");
	private Lang lang;
	private Path resourceJar;
	private Path inputFile;
	private Path outputFile;
	private Optional<Path> taggerPath = Optional.empty();

	
	public PreProcessor(Lang lang, Path resourceJar, Path inputFile, Path outputFile) {
		super();
		this.lang = lang;
		this.resourceJar = resourceJar;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	public void setTaggerPath(Path taggerPath) {
		this.taggerPath = Optional.of(taggerPath);
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}
	
	private static final char BLANK = ' ';
	private static final String INPUT_FILE = "input";
	private static final String OUTPUT_FILE = "output";
	private static final String RESOURCE_JAR = "resources";
	private static final String LANG = "lang";
	private static final String ENCODING = "encoding";
	private static final String TREE_TAGGER_PATH = "treetagger";
	
	private static final char UNDERSCORE = '_';
	private static final String TAG_UNKOWN = "UNK";
	
	public void run() {
		
		try {
			
			AggregateBuilder aggregateBuilder = new AggregateBuilder();
			
			/*
			 * Tokenizer AE
			 */
			AnalysisEngineDescription tokenizerAe = AnalysisEngineFactory.createEngineDescription(
					Lexer.class, 
					Lexer.PARAM_TYPE, "eu.project.ttc.types.WordAnnotation"
				);
		
			URI jarURI = new URI("jar:"+resourceJar.toUri()+"!/");
			String segmentBankURI = TermSuiteResource.SEGMENT_BANK.getUrl(jarURI, lang).toString();

			ExternalResourceDescription	segmentBank = ExternalResourceFactory.createExternalResourceDescription(
					SegmentBankResource.class, 
					segmentBankURI);
			ExternalResourceFactory.bindResource(tokenizerAe, SegmentBank.KEY_SEGMENT_BANK, segmentBank);
			aggregateBuilder.add(tokenizerAe);
			
			
			/*
			 * TreeTagger AE
			 */
			if(taggerPath.isPresent()) {
				String treeTaggerHome = this.taggerPath.get().toString();
				AnalysisEngineDescription taggerAe = AnalysisEngineFactory.createEngineDescription(
						TreeTaggerWrapper.class, 
						TreeTaggerWrapper.PARAM_ANNOTATION_TYPE, "eu.project.ttc.types.WordAnnotation",
						TreeTaggerWrapper.PARAM_TAG_FEATURE, "tag",
						TreeTaggerWrapper.PARAM_LEMMA_FEATURE, "lemma",
						TreeTaggerWrapper.PARAM_UPDATE_ANNOTATION_FEATURES, true,
						TreeTaggerWrapper.PARAM_TT_HOME_DIRECTORY, treeTaggerHome
					);
				
				URI taggerConfigURI = TermSuiteResource.TREETAGGER_CONFIG.getUrl(jarURI, lang, Tagger.TREE_TAGGER);
				ExternalResourceFactory.createDependencyAndBind(
						taggerAe,
						TreeTaggerParameter.KEY_TT_PARAMETER, 
						TreeTaggerParameter.class, 
						taggerConfigURI.toString());

				aggregateBuilder.add(taggerAe);
			}
			
			
			JCas cas = createCas();
			
			SimplePipeline.runPipeline(cas, aggregateBuilder.createAggregate());
			
			FSIterator<Annotation> it = cas.getAnnotationIndex(WordAnnotation.type).iterator();
			try(
					FileOutputStream fos = new FileOutputStream(outputFile.toFile());
					OutputStreamWriter w = new OutputStreamWriter(fos, charset)) {
				while(it.hasNext()) {
					WordAnnotation a = (WordAnnotation)it.next();
					w.write(a.getCoveredText());
					if(this.taggerPath.isPresent()) {
						w.write(UNDERSCORE);
						w.write(a.getTag() == null ? TAG_UNKOWN : a.getTag());
					}
					w.write(BLANK);
				}
				w.flush();
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private JCas createCas() throws UIMAException, IOException {
		JCas cas = JCasFactory.createJCas();
		cas.setDocumentLanguage(lang.getCode());
		LOGGER.info("Reading file {} with charset {}", inputFile, charset);
		String text = StringUtils.readFile(inputFile.toString(), charset);
		text = StringUtils.toOnelineSentences(text);
		cas.setDocumentText(text);
		return cas;
	}
	
	
	public static void main(String[] args) throws ParseException {
		
		CommandLine line = readArgs(args);
		TermSuiteCLIUtils.logCommandLineOptions(line);
		
		Lang lang = Lang.fromCode(line.getOptionValue(LANG));
		Path inputFile = Paths.get(line.getOptionValue(INPUT_FILE));
		Path outputFile = Paths.get(line.getOptionValue(OUTPUT_FILE));
		Path resourceJar = Paths.get(line.getOptionValue(RESOURCE_JAR));

		PreProcessor preProcessor = new PreProcessor(lang, resourceJar, inputFile, outputFile);
		
		if(line.hasOption(TREE_TAGGER_PATH))
			preProcessor.setTaggerPath(Paths.get(line.getOptionValue(TREE_TAGGER_PATH)));
		
		if(line.hasOption(ENCODING))
			preProcessor.setCharset(Charset.forName(line.getOptionValue(ENCODING)));
		
		preProcessor.run();
		
	}


	private static CommandLine readArgs(String[] args) throws ParseException {
		Options options = new Options();
		
		options.addOption(TermSuiteCLIUtils.createOption(
				"i", 
				INPUT_FILE, 
				true, 
				"Path to input file", 
				true));

		options.addOption(TermSuiteCLIUtils.createOption(
				"o", 
				OUTPUT_FILE, 
				true, 
				"Path to output file", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				"t", 
				TREE_TAGGER_PATH, 
				true, 
				"Path to TreeTagger home directory", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				"l", 
				LANG, 
				true, 
				"Language code (fr, en, ry, de, es, etc)", 
				false));

		options.addOption(TermSuiteCLIUtils.createOption(
				"r", 
				RESOURCE_JAR, 
				true, 
				"Path to TermSuite resource jar", 
				false));
		
		options.addOption(TermSuiteCLIUtils.createOption(
				"e", 
				ENCODING, 
				true, 
				"Encoding to use for input file", 
				false));

		try {
			PosixParser parser = new PosixParser();
	
			// Parse and set CL options
			CommandLine line = parser.parse(options, args, false);
			return line;
		} catch (ParseException e) {
			TermSuiteCLIUtils.printUsage(e, USAGE, options); 
			System.exit(1);
			return null;
		}
	}
}
