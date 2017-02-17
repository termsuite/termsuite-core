package fr.univnantes.termsuite.test.unit.tools;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.test.asserts.TermSuiteAssertions;
import fr.univnantes.termsuite.tools.CommandLineClient;
import fr.univnantes.termsuite.tools.opt.CliOption;

public class CommandLineClientSpec {

	private CommandLineClient client;
	
	private static class TataToto extends CommandLineClient {
		public TataToto() {
			super("My desc.");
		}
		protected void run() throws Exception {}
		public void configureOpts() {
			declareFacultative(CliOption.EXPLAIN);
			declareMandatory(CliOption.LANGUAGE);
		}
	};

	@Before
	public void setup() {
		client = new TataToto();
		client.configureOpts();
	}


	
	@Test
	public void testHelp() throws IOException {
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
			;
		System.out.println(output);
	}
}
