package fr.univnantes.termsuite.test.unit.tools;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import fr.univnantes.termsuite.test.asserts.TermSuiteAssertions;
import fr.univnantes.termsuite.tools.CommandLineClient;
import fr.univnantes.termsuite.tools.opt.TermSuiteCliOption;

public class CommandLineClientSpec {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private CommandLineClient client;
	
	private static class TataToto extends CommandLineClient {
		public TataToto() {
			super("My desc.");
		}
		protected void run() throws Exception {}
		public void configureOpts() {
			declareFacultative(TermSuiteCliOption.EXPLAIN);
			declareMandatory(TermSuiteCliOption.LANGUAGE);
		}
	};

	@Before
	public void setup() {
		client = new TataToto();
	}

	@Test
	public void testLoggingDebugToFile() throws IOException {
		Path logFile = initLogFile();
		client().launch("--debug", "--log-file", logFile.toString());
		mockLog();
		assertDebug(logFile);
	}

	private void assertDebug(Path logFile) throws IOException {
		TermSuiteAssertions.assertThat(Files.toString(logFile.toFile(), Charset.defaultCharset()))
			.atLine(1, containsString("with option: --debug"))
			.atLine(2, containsString("with option: --log-file"))
			.atLine(3, containsString("Hello DEBUG"))
			.atLine(4, containsString("Hello INFO"))
			.atLine(5, containsString("Hello WARN"))
			.hasLineCount(5)
			;
	}

	@Test
	public void testLoggingDefaultToFile() throws IOException {
		Path logFile = initLogFile();
		client().launch("--log-file", logFile.toString());
		mockLog();
		TermSuiteAssertions.assertThat(Files.toString(logFile.toFile(), Charset.defaultCharset()))
			.atLine(1, containsString("with option: --log-file"))
			.atLine(2, containsString("Hello INFO"))
			.atLine(3, containsString("Hello WARN"))
			.hasLineCount(3)
			;
	}

	@Test
	public void testLoggingInfoToFile() throws IOException {
		Path logFile = initLogFile();
		client().launch("--info", "--log-file", logFile.toString());
		mockLog();
		TermSuiteAssertions.assertThat(Files.toString(logFile.toFile(), Charset.defaultCharset()))
			.atLine(1, containsString("with option: --info"))
			.atLine(2, containsString("with option: --log-file"))
			.atLine(3, containsString("Hello INFO"))
			.atLine(4, containsString("Hello WARN"))
			.hasLineCount(4)
			;
	}

	private void mockLog() {
		LoggerFactory.getLogger("MyTestLogger").debug("Hello DEBUG");
		LoggerFactory.getLogger("MyTestLogger").info("Hello INFO");
		LoggerFactory.getLogger("MyTestLogger").warn("Hello WARN");
	}


	private Path initLogFile() {
		Path logFile = Paths.get(folder.getRoot().getAbsolutePath(), "termsuite.log");
		logFile.toFile().delete();
		return logFile;
	}


	private CommandLineClient client() {
		return new CommandLineClient("") {
			@Override
			protected void run() throws Exception {
			}
			@Override
			public void configureOpts() {
			}
		};
	}

	
	@Test
	public void testHelp() throws IOException {
		client.configureOpts();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		client.doHelp(new PrintStream(out));
		out.flush();
		String output = out.toString();
		
		TermSuiteAssertions.assertThat(output)
			.atLine(1, is("Usage:"))
			.atLine(2, containsString("java [-Xms256m -Xmx8g] -cp termsuite-core-"))
			.atLine(2, containsString(".jar fr.univnantes.termsuite.test.unit.tools.CommandLineClientSpec.TataToto OPTIONS"))
			.atLine(3, is(""))
			.atLine(4, is("Description:"))
			.atLine(5, containsString("My desc."))
			.atLine(6, is(""))
			.atLine(7, is("Mandatory options:"))
			.atLine(8, containsString("-l  --language LANG"))
			.atLine(9, is(""))
			.atLine(10, is("Other options:"))
			.atLine(11, containsString("--explain"))
//			.atLine(11, containsString("--info"))
//			.atLine(12, containsString("--log-file"))
//			.atLine(13, containsString("--debug"))
//			.atLine(14, containsString("--explain"))
			;
		System.out.println(output);
	}
}
